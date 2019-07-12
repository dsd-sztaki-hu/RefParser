package hu.sztaki.dsd.refparser;

import hu.sztaki.dsd.refparser.Token.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

    private ArrayList<Token> input;
    private int index = 0;

    public Parser(ArrayList<Token> input) {
        this.input = input;
    }

    // region Token handling
    private Token peekNext() {
        return index < input.size() ? input.get(index) : new Token();
    }

    private Token getNext() {
        Token next = peekNext();
        // if (next.type != TokenType.Unknown) index++;
        index++;
        return next;
    }

    private boolean canRead() {
        return index < input.size();
    }

    private void ignoreSpace() {
        while (peekNext().type == Token.TokenType.WhSpace) // An 'if' would be enough in this revision.
            getNext();
    }
    // endregion

    // region Main parsers
    /// Parses the input string with a specialized LL(1) parser, and returns the
    /// first best match.
    public Reference getReference() {
        ArrayList<ParsingResult<Reference>> references = new ArrayList<ParsingResult<Reference>>(6);

        references.add(getReferenceArticle(false));
        if (references.get(0).ambiguous) // If the parsed reference is ambiguous, parse it strictly...
        {
            ParsingResult<Reference> strictVersion = getReferenceArticle(true);
            if (strictVersion.matchLevel > references.get(0).matchLevel) // ...and see if it is more correct.
                references.set(0, strictVersion);
            references.get(0).result.ambiguity = true;
        }

        references.add(getReferenceBook(false));
        if (references.get(1).ambiguous) // If the parsed reference is ambiguous, parse it strictly...
        {
            ParsingResult<Reference> strictVersion = getReferenceBook(true);
            if (strictVersion.matchLevel > references.get(1).matchLevel) // ...and see if it is more correct.
                references.set(1, strictVersion);
            references.get(1).result.ambiguity = true;
        }

        references.add(getReferenceChapter(false));
        if (references.get(2).ambiguous) // If the parsed reference is ambiguous, parse it strictly...
        {
            ParsingResult<Reference> strictVersion = getReferenceChapter(true);
            if (strictVersion.matchLevel > references.get(2).matchLevel) // ...and see if it is more correct.
                references.set(2, strictVersion);
            references.get(2).result.ambiguity = true;
        }

        references.add(getReferenceStandard(false));
        if (references.get(3).ambiguous) // If the parsed reference is ambiguous, parse it strictly...
        {
            ParsingResult<Reference> strictVersion = getReferenceStandard(true);
            if (strictVersion.matchLevel > references.get(3).matchLevel) // ...and see if it is more correct.
                references.set(3, strictVersion);
            references.get(3).result.ambiguity = true;
        }

        references.add(getReferencePatent(false));
        if (references.get(4).ambiguous) // If the parsed reference is ambiguous, parse it strictly...
        {
            ParsingResult<Reference> strictVersion = getReferencePatent(true);
            if (strictVersion.matchLevel > references.get(4).matchLevel) // ...and see if it is more correct.
                references.set(4, strictVersion);
            references.get(4).result.ambiguity = true;
        }

        references.add(getReferenceWebsite(false));
        if (references.get(5).ambiguous) // If the parsed reference is ambiguous, parse it strictly...
        {
            ParsingResult<Reference> strictVersion = getReferenceWebsite(true);
            if (strictVersion.matchLevel > references.get(5).matchLevel) // ...and see if it is more correct.
                references.set(5, strictVersion);
            references.get(5).result.ambiguity = true;
        }

        int maxInd = 0; // A simple maximum search for the Reference with the highest match value.
        for (int i = 1; i < references.size(); i++) {
            if (references.get(i).matchLevel > references.get(maxInd).matchLevel)
                maxInd = i;
        }

        return references.get(maxInd).result; // Unpack the parsed Reference from the ParsingResult object.
    }

    /// Parses the input string as an ArticleReference.
    private ParsingResult<Reference> getReferenceArticle(boolean strict) {
        ArticleReference ref = new ArticleReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        result.matchLevel = 0f;

        ref.authors = parseAuthors();
        if (ref.authors != null && ref.authors.size() > 0)
            result.matchLevel++;

        ref.date = parseDate();
        if (ref.date != null)
            result.matchLevel++;

        ParsingResult<String> title = null;
        title = parseTitle(strict);
        ref.title = title.result;
        result.ambiguous = title.ambiguous; // The result is ambiguous if the title is ambiguous.
        if (ref.title != null && !ref.title.isEmpty())
            result.matchLevel++;

        ref.journalTitle = parseUntilComma(true);
        if (ref.journalTitle != null && !ref.journalTitle.isEmpty())
            result.matchLevel++;

        ref.volume = parseUntilComma(true);
        if (ref.volume != null && !ref.volume.isEmpty()) {
            result.matchLevel++;
            String numOfPages = HelperFunctions.parseNumOfPages(ref.volume);
            if (numOfPages != null) { // If ref.volume seems like '\num\(\num(-\num)?\)'...
                ref.volume = ref.volume.substring(0, ref.volume.indexOf('(')).trim();
                ref.numOfPages = numOfPages;
                // result.matchLevel++;
            }
        }

        ref.pageRange = parsePageRange();
        getNext();
        if (ref.pageRange != null && !ref.pageRange.isEmpty())
            result.matchLevel++;

        ref.DOI = parseDOI();
        if (ref.DOI != null && !ref.DOI.isEmpty())
            result.matchLevel++;

        if (canRead()) // If there are remaining tokens, register them.
            ref.restOfAPA = parseUntilEnd();

        result.matchLevel /= 6f;
        index = 0; // Go back to the first Token.
        ref.matchLevel = result.matchLevel;
        return result;
    }

    /// Parses the input string as a BookReference.
    private ParsingResult<Reference> getReferenceBook(boolean strict) {

        BookReference ref = new BookReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        result.matchLevel = 0f;

        ref.authors = parseAuthors();
        if (ref.authors != null && ref.authors.size() > 0)
            result.matchLevel++;

        ref.date = parseDate();
        if (ref.date != null)
            result.matchLevel++;

        ParsingResult<String> title = null;
        title = parseTitle(strict);
        ref.title = title.result;
        result.ambiguous = title.ambiguous; // The result is ambiguous if the title is ambiguous.
        if (ref.title != null && !ref.title.isEmpty())
            result.matchLevel++;

        int indexBefore = index; // The next part can be one of three different types (DOI, URL or publisher).
        ref.DOI = parseDOI();
        if (ref.DOI == null) {
            index = indexBefore;
            ref.URL = parseURL();
        } else
            ref.matchLevel++;
        if (ref.URL == null) {
            index = indexBefore;
            ref.publisher = parseUntilComma(true);
        } else
            ref.matchLevel++;
        if (ref.publisher != null)
            ref.matchLevel++;

        ignoreSpace();
        // If there is publisher, and the next char. is a colon, there may be a
        // publisher name part after it.
        if (ref.publisher != null || peekNext().type == Token.TokenType.Colon) {
            getNext();
            ref.publisherName = parseUntilDot(true);
            if (ref.publisherName != null && !ref.publisherName.isEmpty())
                ref.matchLevel++;
        }

        if (canRead()) // If there are remaining tokens, register them.
            ref.restOfAPA = parseUntilEnd();

        result.matchLevel /= 5f;
        index = 0; // Go back to the first Token.
        ref.matchLevel = result.matchLevel;
        return result;
    }

    /// Parses the input string as a ChapterReference.
    private ParsingResult<Reference> getReferenceChapter(boolean strict) {

        ChapterReference ref = new ChapterReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        result.matchLevel = 0f;

        ref.authors = parseAuthors();
        if (ref.authors != null && ref.authors.size() > 0)
            result.matchLevel++;

        ref.date = parseDate();
        if (ref.date != null)
            result.matchLevel++;

        ParsingResult<String> title = null;
        title = parseTitle(strict); // This is the title of the CHAPTER, not the book.
        ref.title = title.result;
        result.ambiguous = title.ambiguous; // The result is ambiguous if the title is ambiguous.
        if (ref.title != null && !ref.title.isEmpty())
            result.matchLevel++;

        ignoreSpace();
        if (peekNext().lexeme.equalsIgnoreCase("In")) {
            getNext();
            ref.bookTitle = parseUntilDot(true);
            if (ref.bookTitle != null)
                result.matchLevel++;
        }

        if (canRead()) // If there are remaining tokens, register them.
            ref.restOfAPA = parseUntilEnd();

        result.matchLevel /= 4f; // TODO
        index = 0; // Go back to the first Token.
        ref.matchLevel = result.matchLevel;
        return result;
    }

    /// Parses the input string as a WebReference.
    private ParsingResult<Reference> getReferenceWebsite(boolean strict) {

        WebReference ref = new WebReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        result.matchLevel = 0f;

        ref.authors = parseAuthors();
        if (ref.authors != null && ref.authors.size() > 0)
            result.matchLevel++;

        ref.date = parseDate();
        if (ref.date != null)
            result.matchLevel++;

        ParsingResult<String> title = null;
        title = parseTitle(strict);
        ref.title = title.result;
        result.ambiguous = title.ambiguous; // The result is ambiguous if the title is ambiguous.
        if (ref.title != null && !ref.title.isEmpty())
            result.matchLevel++;

        ignoreSpace();
        ref.URL = parseRetrievedFrom();
        if (ref.URL == null)
            result.matchLevel = 0f; // The next field MUST be 'retrieved from'.
        else
            result.matchLevel++;

        if (canRead()) // If there are remaining tokens, register them.
            ref.restOfAPA = parseUntilEnd();

        result.matchLevel /= 4f;
        index = 0; // Go back to the first Token.
        ref.matchLevel = result.matchLevel;
        return result;
    }

    /// Parses the input string as a PatentReference.
    private ParsingResult<Reference> getReferencePatent(boolean strict) {

        PatentReference ref = new PatentReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        result.matchLevel = 0f;

        ref.authors = parseAuthors();
        if (ref.authors != null && ref.authors.size() > 0)
            result.matchLevel++;

        ref.date = parseDate();
        if (ref.date != null)
            result.matchLevel++;

        ignoreSpace();
        ref.patentNumber = parsePatentNumber();
        if (ref.patentNumber == null)
            result.matchLevel = 0f; // The next field MUST be the patent number.
        else
            result.matchLevel++;

        ignoreSpace();
        ref.URL = parseRetrievedFrom(); // optional

        ignoreSpace();
        ref.publisherPlace = HelperFunctions.withoutDot(parseUntilEnd()); // optional

        result.matchLevel /= 3f;
        index = 0; // Go back to the first Token.
        ref.matchLevel = result.matchLevel;
        return result;
    }

    /// Parses the input string as a StandardReference.
    private ParsingResult<Reference> getReferenceStandard(boolean strict) {

        StandardReference ref = new StandardReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        result.matchLevel = 0f;

        ref.authors = parseAuthors();
        if (ref.authors != null && ref.authors.size() > 0)
            result.matchLevel++;

        ref.date = parseDate();
        if (ref.date != null)
            result.matchLevel++;

        ref.title = parseUntilLBr(false);
        if (ref.title != null && !ref.title.isEmpty())
            result.matchLevel++;

        ref.standardNumber = parseStandardNumber();
        if (ref.standardNumber == null)
            result.matchLevel = 0; // A standard MUST have a standard number.
        else
            result.matchLevel++;

        ignoreSpace();
        ref.URL = parseRetrievedFrom();
        if (ref.URL == null) {
            ignoreSpace();
            ref.publisher = HelperFunctions.withoutDot(parseUntilEnd());
            if (ref.publisher != null)
                result.matchLevel++;
        } else
            result.matchLevel++;

        result.matchLevel /= 5f;
        index = 0; // Go back to the first Token.
        ref.matchLevel = result.matchLevel;
        return result;
    }
    // endregion

    private String parseRetrievedFrom() {
        if (peekNext().lexeme.equalsIgnoreCase("Retrieved")) {
            getNext();
            ignoreSpace();
        }
        if (peekNext().lexeme.equalsIgnoreCase("from")) {
            getNext();
            ignoreSpace();
            return parseUntilEnd();
        }
        return null;
    }

    // Parses a patent number that ends with a number and a dot after it.
    private String parsePatentNumber() {
        StringBuilder sb = new StringBuilder();

        while (canRead() && !containsDigit(peekNext().lexeme))
            sb.append(getNext().lexeme);
        if (!canRead()) // EOF denotes an invalid patent number.
            return null;

        sb.append(parseUntilDot(true));

        return sb.length() == 0 ? null : sb.toString();
    }

    // Determines if the given string contains any digits.
    private boolean containsDigit(String str) {
        for (char ch : str.toCharArray()) {
            if (ch >= '0' && ch <= '9')
                return true;
        }
        return false;
    }

    // Parses the title Tokens into a String.
    // In strict mode, the parsing will stop at the first full stop.
    // In non-strict mode, the parser can consider some full stops to be part of
    // the title.
    private ParsingResult<String> parseTitle(boolean strict) {
        StringBuilder sb = new StringBuilder();
        boolean ambiguity = false;
        int brDepth = 0;
        Token last = new Token(), next = new Token();

        while (canRead()) {
            last = next;
            next = getNext();

            if (next.type == Token.TokenType.LBracket)
                brDepth++;
            else if (next.type == Token.TokenType.RBracket)
                brDepth--;

            if (brDepth == 0 && next.type == Token.TokenType.FullStop) {
                if (strict) // If we are parsing it strictly, break on full stop.
                    break;

                if (last.type != Token.TokenType.Integer && last.lexeme.toLowerCase() != "vol"
                        && !HelperFunctions.isRomanNumeral(last.lexeme))
                    break;
                else // If the full stop is considered a part of the title in non-strict mode, the
                     // input is ambiguous.
                    ambiguity = true;
            }
            sb.append(next.lexeme);
        }

        return new ParsingResult<String>(sb.toString().trim(), 0f, ambiguity); // The second parameter is meaningless.
    }

    // Parses the next part that looks like '\n-\n\.'.
    private String parsePageRange() {
        Integer begin, end;

        ignoreSpace();
        begin = parseInt();

        if (peekNext().type != Token.TokenType.Hyphen)
            return null;
        else
            getNext();

        end = parseInt();

        if (peekNext().type != Token.TokenType.FullStop)
            return null;
        else
            getNext();

        if (begin == null || end == null)
            return null;
        else
            return begin + "-" + end;
    }

    private String parseDOI() {
        if (!peekNext().lexeme.equalsIgnoreCase("doi"))
            return null;
        else
            getNext();

        if (peekNext().type != TokenType.Colon)
            return null;
        else
            getNext();

        return parseUntilEnd();
    }

    private String parseURL() {
        ignoreSpace();
        return null; /// TODO
    }

    // Parses a list of name parts into an ArrayList of Authors.
    private ArrayList<Author> parseAuthors() {
        ArrayList<String> authors0 = new ArrayList<String>();
        ArrayList<Author> authors1 = new ArrayList<Author>();

        authors0.add(parseUntilCommaOrLBr(false));
        if (authors0.get(0) == null)
            return null;
        while (peekNext().type == Token.TokenType.Comma) {
            getNext();
            authors0.add(parseUntilCommaOrLBr(false));
            if (authors0.get(authors0.size() - 1) == null)
                return null;
        }

        for (int i = 0; i < authors0.size() - 1; i += 2) {
            authors0.set(i, HelperFunctions.trimAll(authors0.get(i)));
            authors0.set(i + 1, HelperFunctions.trimAll(authors0.get(i + 1)));
            authors1.add(new Author(authors0.get(i), authors0.get(i + 1)));
        }
        if (authors0.size() % 2 == 1) { // In case the list of names ends with a rogue name.
            authors0.set(authors0.size() - 1, HelperFunctions.trimAll(authors0.get(authors0.size() - 1)));
            authors1.add(new Author(authors0.get(authors0.size() - 1)));
        }

        return authors1;
    }

    private Integer parseInt() {
        Integer num = null;

        ignoreSpace();
        if (peekNext().type != Token.TokenType.Integer)
            return null;
        else
            num = Integer.parseInt(getNext().lexeme);

        return num;
    }

    // Parses the date tokens into a String.
    private CSLDateStruct parseDate() { /// TODO: intervals
        ArrayList<String> serialized = new ArrayList<String>(1);

        ignoreSpace();
        if (peekNext().type != Token.TokenType.LBracket) // Expect a left bracket.
            return null;
        else
            getNext();

        while (canRead() && peekNext().type != TokenType.RBracket) { // Read date parts.
            serialized.add(parseUntilCommaOrRBr(false));
            if (peekNext().type != TokenType.RBracket)
                getNext();
        }
        if (canRead()) // If the input is correct, read the next RBracket.
            getNext();
        else
            return null;

        ignoreSpace();
        if (peekNext().type != Token.TokenType.FullStop)
            return null;
        else
            getNext();

        return HelperFunctions.datePartsToCSLDateStruct(serialized);
    }

    // Parses the standard number into a String.
    private String parseStandardNumber() {
        StringBuilder standard = new StringBuilder();

        ignoreSpace();
        if (peekNext().type != Token.TokenType.LBracket) // Expect a left bracket.
            return null;
        else
            getNext();

        while (canRead() && peekNext().type != TokenType.RBracket) // Read standard number parts.
            standard.append(getNext().lexeme);
        if (canRead()) // If the input is correct, read the next RBracket.
            getNext();
        else
            return null;

        ignoreSpace();
        if (peekNext().type != Token.TokenType.FullStop)
            return null;
        else
            getNext();

        return standard.toString();
    }

    // Calls parseUntil with FullStop.
    private String parseUntilDot(boolean skipLast) {
        String result = parseUntil(Token.TokenType.FullStop);
        if (skipLast && canRead())
            getNext();
        return result;
    }

    // Calls parseUntil with Comma.
    private String parseUntilComma(boolean skipLast) {
        String result = parseUntil(Token.TokenType.Comma);
        if (skipLast && canRead())
            getNext();
        return result;
    }

    // Calls parseUntil with Comma and LBracket.
    private String parseUntilCommaOrLBr(boolean skipLast) {
        String result = parseUntil(Token.TokenType.Comma, Token.TokenType.LBracket);
        if (skipLast && canRead())
            getNext();
        return result;
    }

    // Calls parseUntil with Comma and RBracket.
    private String parseUntilCommaOrRBr(boolean skipLast) {
        String result = parseUntil(Token.TokenType.Comma, Token.TokenType.RBracket);
        if (skipLast && canRead())
            getNext();
        return result;
    }

    // Calls parseUntil with LBracket.
    private String parseUntilLBr(boolean skipLast) {
        String result = parseUntil(Token.TokenType.LBracket);
        if (skipLast && canRead())
            getNext();
        return result;
    }

    // Parses the input until a Token with a specified TokenType is met.
    // (Except if they are inside brackets.)
    // Leading and trailing whitespaces are ignored.
    private String parseUntil(Token.TokenType... terminators) {
        StringBuilder sb = new StringBuilder();
        int brDepth = 0;
        Token next = new Token(Token.TokenType.Unknown, "");
        List<Token.TokenType> termins = Arrays.asList(terminators);
        while (canRead() && (brDepth > 0 || !termins.contains(peekNext().type))) {
            next = getNext();
            if (next.type == Token.TokenType.LBracket)
                brDepth++;
            else if (next.type == Token.TokenType.RBracket)
                brDepth--;
            sb.append(next.lexeme);
        }
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    private String parseUntilEnd() {
        StringBuilder sb = new StringBuilder();
        while (canRead())
            sb.append(getNext().lexeme);
        return sb.length() == 0 ? null : sb.toString().trim();
    }

}