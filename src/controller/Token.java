package src.controller;

enum TokenType {
    INSTRUCTION, REGISTER, CONSTANT, REMAIN_SETTLE,
    FAKE_INSTRUCTION, LABEL, BORDER, ERROR_TOKEN, COMMENT;

    @Override
    public String toString() {
        return name();
    }
}

public class Token {

    private TokenType token_type;
    private String label;
    private int ID;
    private int line_num;

    public Token(String label, TokenType tokenType, int ID, int line_num) {
        this.line_num = line_num;
        this.label = label;
        this.ID = ID;
        token_type = tokenType;

    }

    public String getLabel() {
        return label;
    }

    public TokenType getTokenType() {
        return token_type;
    }

    public int getID() {
        return ID;
    }

    public int getLineNum() {
        return line_num;
    }

    @Override
    public String toString() {
        return String.format("label: %s\nTokenType: %s\nID: %d\nline num: %d",
                label, token_type, ID, line_num);
    }
}
