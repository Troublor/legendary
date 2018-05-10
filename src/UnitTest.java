package src;


import src.controller.Lexer;
import src.controller.TokenManager;

public class UnitTest {
    public static void main(String[] args) {
        String raw_code = "cs:cseg\nmov ax  ,bx\nsub ;;;145665;;65\n   cx,  8\n\ninc";
        Lexer.getInstance().generateToken(raw_code);
        System.out.println(raw_code);
        System.out.println(TokenManager.getInstance().toString());

    }
}
