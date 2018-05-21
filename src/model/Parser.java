package model;

import com.sun.corba.se.impl.oa.toa.TOA;
import dfa.DFA;
import dfa.InvalidTransformationException;
import dfa.NoSuchTransformationException;
import dfa.Node;

import java.awt.*;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

public class Parser {
    private static Parser ourInstance = null;

    public static Parser getInstance() throws InvalidTransformationException {
        if (ourInstance == null) {
            ourInstance = new Parser();
        }
        return ourInstance;
    }

    private DFA dfa;

    private Parser() throws InvalidTransformationException {
        Node start = new ParseNode("start");
        Node i = new ParseNode("instruction");
        Node o = new ParseNode("operation");
        Node b1 = new ParseNode("border1");
        Node l1 = new ParseNode("label1");
        Node dt = new ParseNode("data_type");
        Node c = new ParseNode("constant");
        Node b3 = new ParseNode("border3");
        Node fi = new ParseNode("fake_instruction");
        Node r = new ParseNode("register");
        Node l2 = new ParseNode("label2");
        Node b2 = new ParseNode("border2");
        dfa = new DFA(start, null);
        dfa.addTransform(
                start,
                start,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.ENDLINE)
        );
        dfa.addTransform(
                start,
                i,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.INSTRUCTION)
        );
        dfa.addTransform(
                i,
                o,
                (from, input) -> {
                    Token token = (Token) input;
                    return token.getTokenType().equals(TokenType.CONSTANT)
                            || token.getTokenType().equals(TokenType.LABEL)
                            || token.getTokenType().equals(TokenType.REGISTER);
                },
                (destNode, input) -> ((ParseNode) destNode).setToken((Token) input)
        );
        dfa.addTransform(
                o,
                b1,
                (from, input) -> {
                    Token token = (Token) input;
                    return token.getTokenType().equals(TokenType.BORDER) && token.getLabel().equals(",");
                }
        );
        dfa.addTransform(
                b1,
                o,
                (from, input) -> {
                    Token token = (Token) input;
                    return token.getTokenType().equals(TokenType.CONSTANT)
                            || token.getTokenType().equals(TokenType.LABEL)
                            || token.getTokenType().equals(TokenType.REGISTER);
                },
                (destNode, input) -> ((ParseNode) destNode).setToken((Token) input)
        );
        dfa.addTransform(
                o,
                start,
                (from, input) -> {
                    Token token = (Token) input;
                    return token.getTokenType().equals(TokenType.COMMENT)
                            || token.getTokenType().equals(TokenType.ENDLINE);
                }
        );
        dfa.addTransform(
                start,
                l1,
                (from, input) -> {
                    Token token = (Token) input;
                    return token.getTokenType().equals(TokenType.LABEL);
                }
        );
        dfa.addTransform(
                l1,
                start,
                (from, input) -> {
                    Token token = (Token) input;
                    return token.getTokenType().equals(TokenType.FAKE_INSTRUCTION)
                            || token.getTokenType().equals(TokenType.BORDER) && token.getLabel().equals(":");
                }
        );
        dfa.addTransform(
                l1,
                dt,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.DATA_TYPE)
        );
        dfa.addTransform(
                dt,
                c,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.CONSTANT),
                (destNode, input) -> ((ParseNode) destNode).setToken((Token) input)
        );
        dfa.addTransform(
                c,
                start,
                (from, input) -> {
                    Token token = (Token) input;
                    return token.getTokenType().equals(TokenType.COMMENT)
                            || token.getTokenType().equals(TokenType.ENDLINE);
                }
        );
        dfa.addTransform(
                c,
                b3,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.BORDER)
                        && ((Token) input).getLabel().equals(",")
        );
        dfa.addTransform(
                b3,
                c,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.CONSTANT),
                (destNode, input) -> ((ParseNode) destNode).setToken((Token) input)
        );
        dfa.addTransform(
                start,
                fi,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.FAKE_INSTRUCTION)
        );
        dfa.addTransform(
                fi,
                start,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.LABEL)
        );
        dfa.addTransform(
                fi,
                r,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.REGISTER)
        );
        dfa.addTransform(
                r,
                l2,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.BORDER)
        );
        dfa.addTransform(
                l2,
                b2,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.BORDER)
                        && ((Token) input).getLabel().equals(":")
        );
        dfa.addTransform(
                b2,
                r,
                (from, input) -> ((Token) input).getTokenType().equals(TokenType.REGISTER)
        );
    }

    /**
     * 根据TokenManager进行语法分析
     */
    public void parse() {
        dfa.reset();
        List<Token> tokenList = TokenManager.getInstance().getTokenList();
        for (Token token :
                tokenList) {
            try {
                dfa.run(token);
            } catch (NoSuchTransformationException e) {
                token.setError(true);

                System.out.println("parse error: unexpected token, " + ((Token) e.getInput()).getLabel() + ";" +
                        " at line " + (((Token) e.getInput()).getLineNum() + 1));
                System.out.println("-----------------------------------------");
                break;
            }
        }
    }

    public class ParseNode extends Node {
        private Token token = null;

        public void setToken(Token token) {
            this.token = token;
        }

        public Token getToken() {
            return token;
        }

        public ParseNode(String name) {
            super(name);
        }
    }
}


