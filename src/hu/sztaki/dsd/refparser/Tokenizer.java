package hu.sztaki.dsd.refparser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    private ArrayList<Pair> Regexes = new ArrayList<Pair>();
    private String text;

    public Tokenizer(String text) {
        Regexes.add(new Pair(Pattern.compile("^\\s+"), Token.TokenType.WhSpace));
        Regexes.add(new Pair(Pattern.compile("^,"), Token.TokenType.Comma));
        Regexes.add(new Pair(Pattern.compile("^\\."), Token.TokenType.FullStop));
        Regexes.add(new Pair(Pattern.compile("^\\("), Token.TokenType.LBracket));
        Regexes.add(new Pair(Pattern.compile("^\\)"), Token.TokenType.RBracket));
        Regexes.add(new Pair(Pattern.compile("^-"), Token.TokenType.Hyphen));
        Regexes.add(new Pair(Pattern.compile("^:"), Token.TokenType.Colon));
        Regexes.add(new Pair(Pattern.compile("^(0|[1-9][0-9]*)"), Token.TokenType.Integer));
        Regexes.add(new Pair(Pattern.compile("^[^,.:()\\s]+"), Token.TokenType.Word));
        this.text = text;
    }

    public ArrayList<Token> tokenize() {
        ArrayList<Token> list = new ArrayList<Token>();
        Token prev = new Token(Token.TokenType.Unknown, "");
        while (text.length() > 0) {
            Token next = getNextToken();
            if (next != null) {
                if (next.type == Token.TokenType.WhSpace && prev.type == Token.TokenType.WhSpace)
                    continue;
                list.add(next);
                prev = next;
            } else if (text.length() != 0) {
                throw new RuntimeException("Text length was not 0 after token: "+prev);
            }
        }
        return list;
    }

    private Token getNextToken() {
        String match;
        for (Pair reg : Regexes) {
            Matcher result = reg.regex.matcher(text);
            if (result.find()) {
                match = result.group();
                text = text.substring(match.length());
                return new Token(reg.type, match);
            }
        }
        match = text;
        text = "";
        return new Token(Token.TokenType.Unknown, match);
    }
}
