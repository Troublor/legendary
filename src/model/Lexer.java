package model;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private static Lexer lexer = new Lexer();

    /**
     * 基本逻辑 ：
     * 先用界符对文本进行切分 分析出各种词法成分
     */

    private Map<TokenType, Pattern> lex_patterns = new Hashtable<>();
    private Map<TokenType, List<String>> reserve_table = new Hashtable<>();

    private Lexer() {
        lex_patterns.put(TokenType.BORDER, Pattern.compile("([,\\:\\+\\-\\*\\/\\)\\(])"));
//        lex_patterns.put(TokenType.CONSTANT_STRING, Pattern.compile("'[^\\n]*'"));
        lex_patterns.put(TokenType.CONSTANT, Pattern.compile("([0-9][0-9a-fA-F]*[Hh])|([01]+[Bb])|([0-9]+)"));
        lex_patterns.put(TokenType.REMAIN_SETTLE, Pattern.compile("(\\b[a-zA-Z_]+[0-9a-zA-Z_])"));
        lex_patterns.put(TokenType.ERROR_TOKEN, Pattern.compile("[^ 0-9a-zA-Z\\,\\:\\+\\-\\*\\/\\)\\(']+"));
        reserve_table.put(TokenType.INSTRUCTION, Arrays.asList(
                "mov,sub,add,inc,dec,jmp,add,and,or,lea,int,xlat,shr,or,and,xor,mul,div,shl,cmp,jb,ja,jbe,jae".split(",")
        ));
        reserve_table.put(TokenType.REGISTER, Arrays.asList(
                "ax,al,ah,bx,bl,bh,cx,cl,ch,dx,dl,dh,cs,ss,ds,ei,si,di".split(",")
        ));
        reserve_table.put(TokenType.FAKE_INSTRUCTION, Arrays.asList(
                "equ,org,end,assume,db,dd,segment,ends,end".split(",")
        ));


    }

    public static Lexer getInstance() {
        return lexer;
    }


    public void generateToken(String raw_code) {
        TokenManager.getInstance().reset();
        String[] code_lines = raw_code.split("\\n");
        //reval each line
        for (int line_num = 0; line_num < code_lines.length; line_num++) {
            String each_line = code_lines[line_num];
            Pattern comment_pattern = Pattern.compile(";[^\\n]*");
            Matcher comment_matcher = comment_pattern.matcher(each_line);
            String comment_section = null;
            if (comment_matcher.find()) {
                int start_pos = comment_matcher.start();
                comment_section = each_line.substring(start_pos);
                each_line = each_line.substring(0, start_pos);
            }

            List<TokenRange> token_range_list = new ArrayList<>();
            for (TokenType type : lex_patterns.keySet()) {
                Matcher matcher = lex_patterns.get(type).matcher(each_line);
                while (matcher.find()) {
                    int start_pos = matcher.start();
                    int end_pos = matcher.end();

                    token_range_list.add(new TokenRange(type, start_pos, end_pos));
                }
            }

//          note : Pattern类match的时候是类似迭代的 每次match，find之后的结果都不一样

            Collections.sort(token_range_list);
//            根据token 识别出来的位置 提取每个toke文字及其周围的空余符号进行高亮
//           从而保证原格式不变
            for (int each_token_range = 0; each_token_range < token_range_list.size(); each_token_range++) {
                TokenRange curr = token_range_list.get(each_token_range);
                int raw_text_index;
                if (each_token_range + 1 < token_range_list.size()) {
                    TokenRange next = token_range_list.get(each_token_range + 1);
                    raw_text_index = next.start_pos;
                    if (each_token_range == 0 && curr.start_pos != 0)
                        curr.start_pos = 0;
                } else {
                    raw_text_index = each_line.length();
                }
                String label = each_line.substring(curr.start_pos, curr.end_pos);
                String raw_text = each_line.substring(curr.start_pos, raw_text_index);
                TokenManager.getInstance()
                        .createToken(label, raw_text, line_num);
            }


            if (comment_section != null) {
                TokenManager.getInstance().createCommentSection(comment_section, line_num);

            }
        }
    }

    public TokenType getCompound(String label) {
        for (TokenType type : lex_patterns.keySet()) {
            if (lex_patterns.get(type).matcher(label).find()) {
                if (type != TokenType.REMAIN_SETTLE)
                    return type;
                for (TokenType remain_type : reserve_table.keySet()) {
                    for (String s : reserve_table.get(remain_type)) {
                        label = label.toLowerCase();
                        if (s.equals(label))
                            return remain_type;
                    }
                }
                return TokenType.LABEL;
            }
        }
        return TokenType.ERROR_TOKEN;
    }

    class TokenRange implements Comparable {
        public int start_pos, end_pos;
        public TokenType tokenType;

        public TokenRange(TokenType tokenType, int start_pos, int end_pos) {
            this.tokenType = tokenType;
            this.start_pos = start_pos;
            this.end_pos = end_pos;

        }

        @Override
        public int compareTo(Object o) {
            TokenRange other = (TokenRange) o;
            return start_pos - other.start_pos;
        }
    }


}
