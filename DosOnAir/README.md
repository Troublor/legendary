安装：
1. 先安装 qemu, python
2. Python 安装依赖库 pexpect
3. `cd`  到 当前 `DosOnAir`  文件夹下 运行 `setup_dos` 脚本
出现 `dos.disk`, 安装完成




# Overview
`DosOnAir` is a project focusing on making leaning (16 bits real mode)assembly language easier.

It supplies potential functionality of one-click-install-dos, one-click-run, one-click-compile, one-click-debug.

It gives gui programmer a simple interface to make use of dos system.

# Installation
## setup Dos:

1. install Qemu(qemu-i385 and qemu-img is specially required) and Python 3.5(or higher)
2. install `pexpect` using pip (type `pip install pexpect` in terminal)
3. run `setup_dos.py`(type `python setup_dos.py` in terminal)

notes:
Module `pexpect` is only required by `setup_dos.py`.

So you can choose to install dos by yourself without the `setup_dos.py`, then you need not install `pexpect`.

follow this [instructions](https://imzhwk.com/2018/03/run-dos-inside-qemu/).

## start dos-on-air
run `dos_on_air.py`

## todo
   * support atom or sublime or intelliJ IDEA as a plug-in
    