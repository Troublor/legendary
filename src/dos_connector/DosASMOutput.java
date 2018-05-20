package dos_connector;

public class DosASMOutput {
    /*
    一个典型的 asm 语句:
    24DF:0003 8ED8              MOV     DS,AX

    address:
    24DF:0003

    instruct:
    MOV     DS,AX

    instruct_raw:
    8ED8
     */
    String address, instruct, instruct_raw;
}
