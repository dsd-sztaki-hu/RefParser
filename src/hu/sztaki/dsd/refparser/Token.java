package hu.sztaki.dsd.refparser;

public class Token {

    public enum TokenType {
        Word, Comma, Dot, LBracket, RBracket, WhSpace, Integer, DOI, Hyphen, Colon, Unknown, TripleDots, Slash, ExclaMark, QuestionMark, And
    }; // 'Unknown' is an extremal value.

    public TokenType type;
    public String lexeme;

    public Token() {
        type = TokenType.Unknown;
        lexeme = "";
    }

    public Token(TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }

    public String toString() {
        return type + " = \"" + lexeme + "\"";
    }

}