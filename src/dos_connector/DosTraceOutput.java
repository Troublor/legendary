package dos_connector;

public class DosTraceOutput {
    /*
    一个典型的 Trace 返回结果：
    AX=0166 BX=0000 CX=0078 DX=0000 SP=0000 BP=0000 SI=0000 DI=0000
    DS=24DD ES=24CD SS=24DD CS=24DF IP=0009 NV UP EI PL NZ NA PO NC
    24DF:0009 2C30              SUB     AL,30

    registers:
    AX=0166 BX=0000 CX=0078 DX=0000 SP=0000 BP=0000 SI=0000 DI=0000
    DS=24DD ES=24CD SS=24DD CS=24DF IP=0009

    flags:
    NV UP EI PL NZ NA PO NC

    address:
    24DF:0009

    instruct_raw:
    2C30

    instruct:
    SUB     AL,30

    AX, BX, CX, DX, SP, BP, SI, DI, DS, ED, SS, CS, IP:
    对应的寄存器值

     */
    String AX, BX, CX, DX, SP, BP, SI, DI, DS, ED, SS, CS, IP;
    String flags, address, instruct, instruct_raw;
    String registers;
}
