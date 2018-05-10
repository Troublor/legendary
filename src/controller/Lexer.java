package src.controller;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
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
        lex_patterns.put(TokenType.BORDER, Pattern.compile("([,\\:\\+\\-\\*\\/])"));
        lex_patterns.put(TokenType.CONSTANT, Pattern.compile("([0-9][0-9a-fA-F]*[Hh])|([01]+[Bb])|([0-9]+)"));
        lex_patterns.put(TokenType.REMAIN_SETTLE, Pattern.compile("([a-zA-Z_]+[0-9a-zA-Z_]*)"));
        reserve_table.put(TokenType.INSTRUCTION, Arrays.asList(
                "mov,sub,add,inc,dec,jmp,add,and,or".split(",")
        ));
        reserve_table.put(TokenType.REGISTER, Arrays.asList(
                "ax,al,ah,bx,bl,bh,cx,cl,ch,dx,dl,dh,cs,ss,ds,ei,si,di,".split(",")
        ));
        reserve_table.put(TokenType.FAKE_INSTRUCTION, Arrays.asList(
                "equ,org,end,assume,db,dd".split(",")
        ));


    }

    public static Lexer getInstance() {
        return lexer;
    }


    public void generateToken(String raw_code) {
        TokenManager.getInstance().reset();
        String[] code_lines = raw_code.split("[\\n]+");
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

            String[] splitted = each_line.split("[ ]");
            //spilt by blank things
//            拆分后获得只含有界符和一般标识符的token块
            for (String each_token_block : splitted) {
//              divided by border things
                Pattern border_pattern = lex_patterns.get(TokenType.BORDER);
                String remain_block = each_token_block;
//              note : Pattern类match的时候是类似迭代的 每次match，find之后的结果都不一样
                Matcher curr_matcher = border_pattern.matcher(remain_block);
                while (true) {
//                    思路 通过pattern 一步步拆分每个token block 拆分成border和一般标识符
                    boolean find_res = curr_matcher.find();
//                    当发现当前块存在界符 且不仅有界符时 进行切分迭代
                    if (find_res && remain_block.length() > 1) {
//                        根据界符所在位置将block进行切分 界符，被切分的部分 和后继部分
                        int border_pos = curr_matcher.start();
                        TokenManager.getInstance().createToken(remain_block.substring(0, border_pos), line_num);
                        TokenManager.getInstance().createToken(remain_block.substring(border_pos, border_pos + 1), line_num);
                        if (border_pos + 2 < remain_block.length()) {
                            remain_block = remain_block.substring(border_pos + 1);
                            curr_matcher = border_pattern.matcher(remain_block);
                        } else
                            break;
                    } else {
//                        仅有界符或者一般标识符直接存储
                        TokenManager.getInstance().createToken(remain_block, line_num);
                        break;
                    }

                }

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
                        s = s.toLowerCase();
                        if (s.equals(label))
                            return remain_type;
                    }
                }
                return TokenType.LABEL;
            }
        }
        return TokenType.ERROR_TOKEN;
    }


}
