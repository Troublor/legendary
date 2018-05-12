import model.Lexer;
import model.TokenManager;

public class UnitTest {
    public static void main(String[] args) {
        String raw_code = "add ax,3423h\n  mov ax  ,bx\n";
        Lexer.getInstance().generateToken(raw_code);
        System.out.println(raw_code);
        System.out.println(TokenManager.getInstance().toString());

    }
}
