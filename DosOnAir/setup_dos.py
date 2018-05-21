import pexpect
import subprocess


import sys

subprocess.run("qemu-img create -f qcow dos.disk 128M".split())

with pexpect.spawn(
        "qemu-system-i386 -hda dos.disk -m 16 -k en-us -rtc base=localtime -fda Dos6.22.img -boot a -nographic",
        logfile=sys.stdout, encoding='utf-8') as qemu:
    qemu.expect_exact('A:\>')

    qemu.send('fdisk\r')
    qemu.expect_exact('Enter choice')
    qemu.send('\r')
    qemu.send('\r')
    qemu.send('\r')
    qemu.send('\r')
    qemu.send('\r')
    qemu.expect_exact('A:\>')

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

    qemu.send("c:\r")
    qemu.expect_exact('C:\>')
    qemu.send('dir\r')
    qemu.expect_exact('Volume Serial Number')
    qemu.close()

with pexpect.spawn(
        "qemu-system-i386 -hda dos.disk -m 16 -k en-us -rtc base=localtime -fda Dos6.22.img -boot a -nographic",
        logfile=sys.stdout, encoding='utf-8') as qemu:
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
        for alphabet in command:
            qemu.send(alphabet)
        qemu.send('\r')
        qemu.expect_exact('C:\>')

    qemu.send('\r')

    qemu.close()

with pexpect.spawn(
        "qemu-system-i386 -hda dos.disk -m 16 -k en-us -rtc base=localtime -drive file=fat:rw:dosfiles -boot order=c -nographic",
        logfile=sys.stdout, encoding='utf-8') as qemu:
    try:
        qemu.expect_exact('C:\>', timeout=10)

        qemu.send('dir MSDOS\r')
        qemu.expect_exact('42 file(s)', timeout=1)

        qemu.send('d:\r')
        qemu.expect_exact('D:\>', timeout=1)

        qemu.close()
    except pexpect.TIMEOUT as e:
        print(str(qemu), file=sys.stderr)
