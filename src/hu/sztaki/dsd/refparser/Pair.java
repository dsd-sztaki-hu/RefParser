package hu.sztaki.dsd.refparser;

import java.util.regex.Pattern;

public class Pair {

    public Pattern regex;
    public Token.TokenType type;

    public Pair(Pattern regex, Token.TokenType type) {
        this.regex = regex;
        this.type = type;
    }

}