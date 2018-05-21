import json
import os
import pprint
import re
import select
import socket
import time
import traceback

from pty_process import PtyProcess


# HOST = 'localhost'
# PORT = 12346
#
# listen_sock =  socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# listen_sock.bind((HOST, PORT))
# listen_sock.listen(1)
# command_conn, addr = listen_sock.accept()

class DosOnAir:
    """
    调用 debug() 等方法可以对 dos进行相应操作，但是大部分函数本身并不没有返回值，无法直接得知操作引起的影响
    所有操作结果都会被保存在 command_out 中，
    程序本身的输出会保存在 result_out 中
    """
    trace_pat = re.compile(
        r'''
        (?P<registers>  # registers
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
        (?P<instruct_raw>\w+) \s+
        (?P<instruct>\w+\s+[\w,]+)\s*''',
        flags=re.DOTALL | re.VERBOSE)

    asm_pat = re.compile(
        r'''
        (?P<address>\w{4}:\w{4}) \s+ 
        (?P<instrcuct_raw>\w+)	\s+ # raw instruction in hex
        (?P<instruct>\w+\s+[\w,]+) \s*
        ''',
        flags=re.DOTALL | re.VERBOSE
    )

    # todo 没办法关闭回显，原因可能是回显是 dos 在控制
    def __init__(self, dos_files: str, dos_disk: str, delay=0.05, qemu_path=None, cwd=None) -> None:
        super().__init__()
        self.cwd = cwd or os.getcwd()
        self.qemu_path = qemu_path or "qemu-system-i386"
        self.dos_files = os.path.join(self.cwd, dos_files)
        self.dos_disk = os.path.join(self.cwd, dos_disk)
        command = "{} -hda {} -m 16 -k en-us -rtc base=localtime -drive file=fat:rw:{} -boot order=c -nographic".format(
            self.qemu_path, self.dos_disk, self.dos_files)
        self.dos = PtyProcess.spawn(command.split())
        self.fd = self.dos.fd
        self.delay = delay
        self.init_dos()

        self.debug_state = False
        self.command_out = []
        self.std_out = ''

    def init_dos(self):
        # 不能用 not self.dos.expect() 如果匹配到第一个 pattern，会返回0，零 在 not 0 返回的还是 True,会陷入死循环
        while self.dos.expect_exact('C:\>') is None:
            time.sleep(self.delay)
            self.dos.read()

        self.dos.send_one_by_one('d:\r')
        while self.dos.expect_exact('D:\>') is None:
            time.sleep(self.delay)
            self.dos.read()

    def debug(self, exe_file):
        assert not self.debug_state
        exe_file = os.path.split(exe_file)[1]
        if not os.path.exists(os.path.join(self.dos_files, exe_file)):
            raise FileNotFoundError(exe_file + ' not found in ', self.dos_files)
        self.dos.send_one_by_one('bin\debug.com {}\r'.format(exe_file))
        self.debug_state = True

    def step(self, n=None):
        assert self.debug_state
        if not n:
            n = ''
        else:
            assert int(n) > 0
        self.dos.send_one_by_one('t {} \r'.format(n))
        # todo 假如 step 过程中汇编程序有要求输入并阻塞，会造成死锁（expect 过程中不读取键盘输入数据）

    def show_register(self):
        """
        show current registers state
        :return:
        """
        assert self.debug_state
        self.dos.send_one_by_one('r \r')

    # todo 可支持修改特定寄存器

    def display_data(self, from_=None, to=None):
        """
        display data in memery
        :param from_: hex
        :param to: hex
        :return:
        """
        assert self.debug_state
        if not from_:
            from_ = ''
        if not to:
            to = ''
        self.dos.send_one_by_one('d {} {}'.format(from_, to))

    def display_asm(self, from_=None, to=None):
        """
        display data in memery
        :param from_:
        :param to:
        :return:
        """
        assert self.debug_state
        if not from_:
            from_ = ''
        if not to:
            to = ''
        self.dos.send_one_by_one('u {} {}'.format(from_, to))

    def check_output(self):
        # 从 buffer 中找出所有的 trace 信息和 asm 信息，并把结果放到 self.command_out 中
        # 并将这些信息从 buffer 中删除
        while True:
            #  在 DOS 返回字符串中匹配 trace,asm pattern
            # 如果没有匹配，将输出放置在 self.std_out 中
            trace_match = re.search(self.trace_pat, self.dos.buff)
            asm_match = re.search(self.asm_pat, self.dos.buff)
            span = None
            if not trace_match and not asm_match:
                break
            elif not trace_match and asm_match:
                asm_dict = asm_match.groupdict()
                span = asm_match.span()
                self.command_out.append(asm_dict)
            else:
                # 因为 trace_pat 是包含 asm_pat 的，所以 trace_pat 匹配成功时 asm_pat 也一定匹配成功:
                trace_dict = trace_match.groupdict()
                span = trace_match.span()
                self.command_out.append(trace_dict)
            if span:
                self.dos.buff = self.dos.buff[0:span[0]] + self.dos.buff[span[1]:]
        self.std_out = process_from_stdout(self.dos.buff[:])
        # todo 很多意外字符
        # Program terminated 表示 dos 一个程序在 debug.exe 中正常结束
        if self.std_out.find('Program terminated') != -1:
            self.debug_state = False
        self.dos.buff = ""

    def check_commands(self, data: str or bytes):
        """
        command 结构；
        {"args": ["sample.exe"], "command": "debug"}
        {"args": [user_input], "command":"std_input"}
        :param data:
        :return:
        """
        if isinstance(data, bytes):
            data = data.decode()
        commands = re.findall('{.*?}', data)
        if not commands:
            # todo 这里调试用，以后返回错误
            data = data.replace('\n', '\r')
            self.dos.send_one_by_one(data)
            return
        commands = [json.loads(command) for command in commands]
        for command in commands:
            func = getattr(self, command['command'])
            args = tuple(command.get('args', None))
            func(*args)

    def std_input(self, user_input):
        if isinstance(user_input, bytes):
            user_input = user_input.decode()
        user_input = user_input.replace('\n', '\r')
        self.dos.send_one_by_one(user_input)

    def import_file(self, file_path: str):
        with open(file_path) as inputf, open(os.path.join(self.dos_files, os.path.split(file_path)[1])) as outputf:
            for line in inputf:
                if line.endswith('\n') and not line.endswith('\r'):
                    outputf.write(line.replace('\n', '\r'))
                else:
                    outputf.write(line)

    def masm(self, asm_file: str):
        asm_file = os.path.split(asm_file)[1]
        if not os.path.exists(os.path.join(self.dos_files, asm_file)):
            raise FileNotFoundError(asm_file + ' not found in ', self.dos_files)
        self.dos.send_one_by_one('bin\masm.exe {}\r'.format(asm_file))
        self.dos.send_one_by_one('\r')
        self.dos.send_one_by_one('\r')
        self.dos.send_one_by_one('\r')
        self.dos.read()
        assert self.dos.expect_exact('D:\>') is not None
        return self.dos.before

    def link(self, obj_file: str):
        obj_file = os.path.split(obj_file)[1]
        if not os.path.exists(os.path.join(self.dos_files, obj_file)):
            raise FileNotFoundError(obj_file + ' not found in ', self.dos_files)
        self.dos.send_one_by_one('bin\link.exe {}\r'.format(obj_file))
        self.dos.send_one_by_one('\r')
        self.dos.send_one_by_one('\r')
        self.dos.send_one_by_one('\r')
        assert self.dos.expect_exact('D:\>') is not None
        return self.dos.before

    def close(self):
        self.dos.close()


def process_from_stdout(output: str):
    result = output.replace('\r\r\n', '\n')
    return result


def dos_loop(command_fd, dos_files: str, dos_disk: str, cwd: str, qemu_path):
    """
    command json format:
    debug: {"command": "debug", "args":["exe_file"]}
    step: {"command": "step", "args":[1]} or {""command": "step", "args":[]}
    register: {"command": "register", "args":[]}
    .....
    :param command_fd:
    :param std_fd:
    :param dos_files:
    :param dos_disk:
    :param cwd:
    :return:
    """
    vir = DosOnAir(dos_files, dos_disk, cwd=cwd, qemu_path=qemu_path)
    print('dos started')
    os.write(command_fd, b'<start>')
    try:
        while True:
            # todo bug! 不挂起一小段时间的话 开机会阻塞一下， select 的 bug?
            r, w, x = select.select([vir.fd, command_fd], [], [], None)
            time.sleep(0.1)
            if command_fd in r:
                data = os.read(command_fd, 1000)
                vir.check_commands(data)

            elif vir.fd in r:
                # todo bug! 开机第一个提示符总是读不出来 'C:\>', 加上 挂起 0.05 秒后可以解决
                vir.dos.read()
                vir.check_output()
                # 传输 数据
                if vir.command_out:
                    pprint.pprint(vir.command_out)  # debug
                    for command in vir.command_out:
                        os.write(command_fd, json.dumps(command).encode())
                    vir.command_out = []
                if vir.std_out:
                    result = dict(stdout=vir.std_out)
                    print(result)  # debug
                    os.write(command_fd, json.dumps(result).encode())
                    vir.std_out = ''
            else:
                pass
                # data = command_conn.recv(1000)
                # data = data.replace(b'\n', b'\r')
                # # os.write(process.fd, data)
                # write_one_by_one(process.fd, data)
    finally:
        vir.close()


import argparse

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='running dos in background')
    parser.add_argument('host', type=str)
    parser.add_argument('command_port', type=int, help='command port')
    parser.add_argument('cwd', type=str, help='director where dos files located (including dos.disk)')
    parser.add_argument('--qemupath', help='set qemu path, if not set, the program will find qemu in $PATH')
    args = parser.parse_args()
    print(args)

    host = args.host
    dir = args.cwd
    qemu_path = args.qemupath

    command_sock = socket.socket()
    command_sock.bind((host, args.command_port))
    print("waiting for connecting...")
    command_sock.listen(1)
    command_conn, command_addr = command_sock.accept()
    print("connected with a client: {}".format(command_addr))
    try:
        dos_loop(command_conn.fileno(), 'dosfiles', 'dos.disk', dir, qemu_path)
    finally:
        traces = traceback.format_exc()
        command_conn.send(traces.encode())
        traceback.print_exc()
        command_conn.close()
        command_sock.close()
