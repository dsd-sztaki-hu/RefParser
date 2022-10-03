package hu.sztaki.dsd.refparser;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// Converts an APA reference into an ArrayList of Tokens.
public class Tokenizer {

    private ArrayList<Pair> Regexes = new ArrayList<Pair>();
    private String text;

    public Tokenizer(String text) {
        if (text == null)
            text = "";

        Regexes.add(new Pair(Pattern.compile("^\\s+"), Token.TokenType.WhSpace));
        Regexes.add(new Pair(Pattern.compile("^,"), Token.TokenType.Comma));
        Regexes.add(new Pair(Pattern.compile("^\\.{3}"), Token.TokenType.TripleDots));
        Regexes.add(new Pair(Pattern.compile("^\\."), Token.TokenType.Dot));
        Regexes.add(new Pair(Pattern.compile("^\\?"), Token.TokenType.QuestionMark));
        Regexes.add(new Pair(Pattern.compile("^!"), Token.TokenType.ExclaMark));
        Regexes.add(new Pair(Pattern.compile("^&"), Token.TokenType.And));
        Regexes.add(new Pair(Pattern.compile("^\\("), Token.TokenType.LBracket));
        Regexes.add(new Pair(Pattern.compile("^\\)"), Token.TokenType.RBracket));
        Regexes.add(new Pair(Pattern.compile("^-"), Token.TokenType.Hyphen));
        Regexes.add(new Pair(Pattern.compile("^:"), Token.TokenType.Colon));
        Regexes.add(new Pair(Pattern.compile("^[\\/]"), Token.TokenType.Slash));
        Regexes.add(new Pair(Pattern.compile("^10\\.\\d{4,9}/[^\\s]+"), Token.TokenType.DOI));
        Regexes.add(new Pair(Pattern.compile("^[0-9]+"), Token.TokenType.Integer)); // Includes invalid integers.
        Regexes.add(new Pair(Pattern.compile("^[^,.:()?!&\\s]+"), Token.TokenType.Word));
        
        this.text = text;
    }

    public ArrayList<Token> tokenize() {
        ArrayList<Token> list = new ArrayList<Token>();
        while (!text.isEmpty())
            list.add(getNextToken());
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
