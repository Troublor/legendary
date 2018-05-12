package model;

import dfa.DFA;

import java.util.Map;
import java.util.Stack;

public class Parser {
    private static Parser ourInstance = new Parser();

    public static Parser getInstance() {
        return ourInstance;
    }

    private DFA dfa;

    private Parser() {

    }

    /**
     * 根据TokenManager进行语法分析
     */
    public void parse() {

    }
}
