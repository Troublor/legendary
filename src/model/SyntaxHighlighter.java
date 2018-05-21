package model;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 读取token list 生成一行行带高亮的html文本
 */
public class SyntaxHighlighter {
    private static SyntaxHighlighter highlighter = new SyntaxHighlighter();
    private Map<TokenType, String> TOKEN_COLOR_MAP = new Hashtable<>();

    private SyntaxHighlighter() {
        TOKEN_COLOR_MAP.put(TokenType.ERROR_TOKEN, "#990000");
        TOKEN_COLOR_MAP.put(TokenType.INSTRUCTION, "#1a3399");
        TOKEN_COLOR_MAP.put(TokenType.CONSTANT, "#999933");
        TOKEN_COLOR_MAP.put(TokenType.REGISTER, "#663366");
        TOKEN_COLOR_MAP.put(TokenType.CONSTANT_STRING, "#999933");
        TOKEN_COLOR_MAP.put(TokenType.COMMENT, "#999999");
        TOKEN_COLOR_MAP.put(TokenType.BORDER, "#000000");
        TOKEN_COLOR_MAP.put(TokenType.FAKE_INSTRUCTION, "#b3b31a");
    }

    public static SyntaxHighlighter getInstance() {
        return highlighter;
    }

    public String startHighlighting() {
        List<Token> tokens = TokenManager.getInstance().getTokenList();
        List<Token> same_line_tokens = new ArrayList<>();
        Document document = Jsoup.parse("<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>");
        int curr_line = 0;
        for (int token_iter = 0; token_iter <= tokens.size() && tokens.size() != 0; token_iter++) {
            Token token = null;
            if (token_iter < tokens.size())
                token = tokens.get(token_iter);
            if (token != null && token.getLineNum() != curr_line || token_iter == tokens.size()) {
                Element line = document.body().appendElement("p");
                for (Token each_token : same_line_tokens) {
                    line.appendElement("font")
                            .appendText(each_token.getRawText())
                            .attr("color", TOKEN_COLOR_MAP.get(each_token.getTokenType()));
                }
                if (token != null) {
                    curr_line = token.getLineNum();
                    same_line_tokens.clear();
                }
            }
            if (token_iter < tokens.size())
                same_line_tokens.add(token);

        }
        return document.toString();
    }

}
