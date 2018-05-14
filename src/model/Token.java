package model;


public class Token {

    private TokenType token_type;
    private String label;
    private String raw_text;
    private int ID;
    private int line_num;

    //语法分析结果
    private boolean error;
    private String error_msg;

    public Token(String label, String raw_text, TokenType tokenType, int ID, int line_num) {
        this.line_num = line_num;
        this.label = label;
        this.raw_text = raw_text;
        this.ID = ID;
        token_type = tokenType;
        error = false;
        error_msg = "";
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

    public String getRawText() {
        return raw_text;
    }

    public boolean isError() {
        return error;
    }

    public String getError_msg() {
        return error_msg;
    }

    @Override
    public String toString() {
        return String.format("label: %8s  TokenType: %14s  ID: %4d  line num: %4d  error_status: %s  raw text: '%s' ",
                label, token_type, ID, line_num, error, raw_text);
    }
}
