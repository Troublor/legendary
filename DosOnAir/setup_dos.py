import pexpect
import subprocess

# 为 dos 分配磁盘
import sys

subprocess.run("qemu-img create -f qcow dos.disk 128M".split())
# 从磁盘启动，格式化C 盘
with pexpect.spawn("qemu-system-i386 -hda dos.disk -m 16 -k en-us -rtc base=localtime -fda Dos6.22.img -boot a -nographic", logfile=sys.stdout, encoding='utf-8') as qemu:
    qemu.expect_exact('A:\>') # 开机
    # 建立 C 盘
    qemu.send('fdisk\r')
    qemu.expect_exact('Enter choice')
    qemu.send('\r')
    qemu.send('\r')
    qemu.send('\r')
    qemu.send('\r')
    qemu.send('\r')
    qemu.expect_exact('A:\>')
    # 格式化 C 盘
    qemu.send('FORMAT C: /S\r')
    qemu.expect(pexpect.TIMEOUT, timeout=1)
    # print(qemu.before.decode())
    qemu.send('y\r')
    qemu.expect(pexpect.TIMEOUT, timeout=1)
    # print(qemu.before.decode())

    qemu.send('\r')
    qemu.expect(pexpect.TIMEOUT, timeout=1)
    # print(qemu.before.decode())

    qemu.expect_exact('A:\>')
    # 确认
    qemu.send("c:\r")
    qemu.expect_exact('C:\>')
    qemu.send('dir\r')
    qemu.expect_exact('Volume Serial Number')
    qemu.close()

# 配置
with pexpect.spawn("qemu-system-i386 -hda dos.disk -m 16 -k en-us -rtc base=localtime -fda Dos6.22.img -boot a -nographic", logfile=sys.stdout, encoding='utf-8') as qemu:
    qemu.expect(pexpect.TIMEOUT, timeout=8)
    configs = [
        'C:',
        'MD MSDOS',
        'COPY A:\*.* .\MSDOS /v',
        # 'COPY A:\ *.*.\M',
        'COPY A:\CONFIG.SYS',
        'COPY A:\AUTOEXEC.BAT',
        'COPY A:\HIMEM.SYS',
        'COPY A:\CD1.SYS'
    ]
    for command in configs:
        # bug?   COPY A:\*.* .\MSDOS /v 发送到 dos 命令行会变成 COPY A:\*.* .\M
        # COPY A:\CONFIG.SYS -> COPY A:\CONFIG.

        # 这里把命令用空格间隔，分开发送是为了避免dos 命令接收到（其实是回显）的字符和发送的不同的问题，怀疑是个 bug,分开发送没有问题
        # e.g. COPY A:\*.* .\MSDOS /v 发送到 dos 命令行会变成 COPY A:\*.* .\M, M 后面的内容被忽略了
        # coms = command.split(' .')
        # for com in coms:
        # 	qemu.send(com+' ')
        for alphabet in command:
            qemu.send(alphabet)
        qemu.send('\r')
        qemu.expect_exact('C:\>')

    qemu.send('\r')

    qemu.close()

# 以后就可以从C盘启动了
with pexpect.spawn("qemu-system-i386 -hda dos.disk -m 16 -k en-us -rtc base=localtime -drive file=fat:rw:dosfiles -boot order=c -nographic", logfile=sys.stdout, encoding='utf-8') as qemu:
    try:
        qemu.expect_exact('C:\>', timeout=10)
        # print('can\'t launch with C:\\, please try again', file=sys.stderr)
        qemu.send('dir MSDOS\r')
        qemu.expect_exact('42 file(s)', timeout=1)
        # print('MSDOS files not found', file=sys.stderr)
        qemu.send('d:\r')
        qemu.expect_exact('D:\>', timeout=1)
        # 	print('can\'t launch with D:\\', file=sys.stderr)
        qemu.close()
    except pexpect.TIMEOUT as e:
        print(str(qemu), file=sys.stderr)



