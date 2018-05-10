package src.controller;

import java.util.ArrayList;
import java.util.List;

public class TokenManager {
    private static TokenManager instance = new TokenManager();

    private List<Token> token_list = new ArrayList<>();
    private int id_cnter = 0;

    private TokenManager() {

    }


    public static TokenManager getInstance() {
        return instance;
    }

    public void createToken(String label, int line_num) {
        if (label.length() == 0)
            return;
        TokenType type = Lexer.getInstance().getCompound(label);
        token_list.add(new Token(label, type, id_cnter, line_num));
        id_cnter++;
    }

    public void createCommentSection(String content, int line_num) {
        token_list.add(new Token(content, TokenType.COMMENT, id_cnter, line_num));
        id_cnter++;
    }

    public void reset() {
        token_list.clear();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (Token t : token_list) {
            res.append(t.toString());
            res.append("\n-------------\n");
        }
        return res.toString();
    }
}
