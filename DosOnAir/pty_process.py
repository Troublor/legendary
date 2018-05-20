import re
import signal
import termios
import os
import struct
import fcntl
import errno
import pty
import sys
from shutil import which
from pty import STDIN_FILENO, CHILD

import time


class PtyProcess:
    trace_pat = re.compile(
        r'''.*?  # match unnecessary chars
        (?P<regesters>  # registers
        AX=(?P<AX>\w{4}) \s+
        BX=(?P<BX>\w{4}) \s+
        CX=(?P<CX>\w{4}) \s+
        DX=(?P<DX>\w{4}) \s+
        SP=(?P<SP>\w{4}) \s+
        BP=(?P<BP>\w{4}) \s+
        SI=(?P<SI>\w{4}) \s+
        DI=(?P<DI>\w{4}) \s+
        DS=(?P<DS>\w{4}) \s+
        ES=(?P<ES>\w{4}) \s+
        SS=(?P<SS>\w{4}) \s+
        CS=(?P<CS>\w{4}) \s+
        IP=(?P<IP>\w{4})) \s+
        (?P<flags>\w{2}\s\w{2}\s\w{2}\s\w{2}\s\w{2}\s\w{2}\s\w{2}\s\w{2})\s+   #flags
        (?P<address>\w{4}:\w{4}) \s
        (?P<instruct_raw>\w{4}) \s+
        (?P<instruct>\w+\s+[\w,\[\]]+)\s*
        (?P<DS_change>DS:\w{4}=\w{2})? \s*
        ''',
        flags=re.DOTALL | re.VERBOSE)
    def __init__(self, pid, fd) -> None:
        super().__init__()
        self.pid = pid
        self.fd = fd
        self.buff = ''
        self.before = None
        self.match = None

    def read(self):
        """
         读之前需要确认 self.fd 是否可读，否则会一直阻塞
        :return:
        """
        data = os.read(self.fd, 1000)
        self.buff += data.decode()

    def expect_exact(self, pattern_list:list or str):
        """
        注意 expect_exact 并不更新 buffer, 需要在 read()之后使用
        :param pattern_list:
        :return:
        """
        if isinstance(pattern_list, str):
            pattern_list = [pattern_list]
        for i, pattern in enumerate(pattern_list):
            index = self.buff.find(pattern)
            if index != -1:
                self.match = self.buff[index:index + len(pattern)]
                self.before = self.buff[0:index]
                self.buff = self.buff[index+len(pattern):]
                return i
        return None

    def send_one_by_one(self, data: str or bytes):
        if isinstance(data, str):
            data = data.encode()
        for alphabet in data:
            time.sleep(0.05)
            os.write(self.fd, bytes([alphabet]))

    @classmethod
    def spawn(cls, argv:list, echo=False):
        executable = which(argv[0])
        if not executable:
            raise FileNotFoundError('Command not found, executable: {}'.format(argv[0]))
        argv[0] = executable

        pid, fd = pty.fork()
        # child 需要运行命令
        if pid == CHILD:
            # 一般的窗口大小是80*24
            # 注意在 pty child 中 返回的 fd 是无效的，见 ptyfork() doc。
            # pty child 的标准输入\输出 连接父进程的 fd
            _setwinsize(STDIN_FILENO, 24, 80)
            if not echo:
                _setecho(STDIN_FILENO, False)
            try:
                os.execv(executable, argv)
            except OSError as e:
                print("fork error!", sys.stderr)
                # doc :   EX_OSERR
                # Exit code that means an operating system error was detected, such as the inability to fork or create a pipe.
                os._exit(os.EX_OSERR)
        # 实际需要一些 error handle，比如说假如子进程 fork() 失败，父进程应该得到提示
        inst = cls(pid, fd)
        return inst

    def terminate(self):
        os.kill(self.pid, signal.SIGKILL)

    # todo close 需要做其他事情吗
    def close(self):
        self.terminate()


def _setwinsize(fd, rows, cols):
    TIOCSWINSZ = getattr(termios, 'TIOCSWINSZ', -2146929561)
    # Note, assume ws_xpixel and ws_ypixel are zero.
    s = struct.pack('HHHH', rows, cols, 0, 0)
    fcntl.ioctl(fd, TIOCSWINSZ, s)



def _setecho(fd, state):
    errmsg = 'setecho() not suitable'

    try:
        attr = termios.tcgetattr(fd)
    except termios.error as err:
        if err.args[0] == errno.EINVAL:
            raise IOError(err.args[0], '%s: %s.' % (err.args[1], errmsg))
        raise

    if state:
        attr[3] = attr[3] | termios.ECHO
    else:
        attr[3] = attr[3] & ~termios.ECHO

    try:
        termios.tcsetattr(fd, termios.TCSANOW, attr)
    except IOError as err:
        if err.args[0] == errno.EINVAL:
            raise IOError(err.args[0], '%s: %s.' % (err.args[1], errmsg))
        raise