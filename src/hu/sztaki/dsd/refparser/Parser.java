package hu.sztaki.dsd.refparser;

import hu.sztaki.dsd.refparser.Token.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Parser {

    private enum AuthorsMode {
        AtLeastOne, MoreThanOne, JustOne
    };

    private static ArrayList<Function<Boolean, ParsingResult<Reference>>> parserFunctions;

    private ArrayList<Token> input;
    private int index = 0;

    public Parser(ArrayList<Token> input) {
        parserFunctions = new ArrayList<Function<Boolean, ParsingResult<Reference>>>(7);

        parserFunctions.add((Boolean s) -> {
            return getReferenceArticle(s);
        });
        parserFunctions.add((Boolean s) -> {
            return getReferenceBook(s);
        });
        parserFunctions.add((Boolean s) -> {
            return getReferenceChapter(s);
        });
        parserFunctions.add((Boolean s) -> {
            return getReferencePatent(s);
        });
        parserFunctions.add((Boolean s) -> {
            return getReferenceStandard(s);
        });
        parserFunctions.add((Boolean s) -> {
            return getReferenceThesis(s);
        });
        parserFunctions.add((Boolean s) -> {
            return getReferenceWebsite(s);
        });

        this.input = input;
    }

    // region Token handling
    private Token peekNext() {
        return index < input.size() ? input.get(index) : new Token();
    }

    /// Returns the next N non-whitespace Tokens from the input without advancing
    /// the
    /// index. It seriously violates the LL(1) principles.
    private ArrayList<Token> peekAhead(int count) {
        ArrayList<Token> tokens = new ArrayList<Token>(count);
        int prevIndex = index, i = 0;
        while (canRead() && i < count) {
            if (peekNext().type != TokenType.WhSpace) {
                tokens.add(getNext());
                i++;
            } else // Simply ignore whitespaces.
                getNext();
        }
        index = prevIndex;
        return tokens;
    }

    private Token getNext() {
        Token next = peekNext();
        // if (next.type != TokenType.Unknown) index++;
        index++;
        return next;
    }

    /// Ignores the next N non-whitespace Tokens.
    private void ignoreAhead(int count) {
        int i = 0;
        while (canRead() && i < count) {
            if (peekNext().type != TokenType.WhSpace)
                i++;
            getNext();
        }
    }

    private boolean canRead() {
        return index < input.size();
    }

    private void ignoreSpace() {
        while (peekNext().type == Token.TokenType.WhSpace) // An 'if' would be enough in this revision.
            getNext();
    }

    private void ignoreDot() {
        if (peekNext().type == Token.TokenType.Dot)
            getNext();
    }
    // endregion

    // region Main parsers
    /// Parses the input string with a specialized LL(1) parser, and returns the
    /// first best match.
    public Reference getReference(ParsingMode mode) {
        if (mode == ParsingMode.Automatic) {
            ArrayList<ParsingResult<Reference>> references1 = new ArrayList<ParsingResult<Reference>>(7);
            ArrayList<ParsingResult<Reference>> references2 = new ArrayList<ParsingResult<Reference>>(7);

            for (int i = 0; i < parserFunctions.size(); i++) { // Iterate through every parser function.
                references1.add(parserFunctions.get(i).apply(false));
                index = 0; // Go back to the first Token.

                if (references1.get(i).result.matchLevel >= 1f) {
                    if (references1.get(i).ambiguous) // Add a warning for ambiguity.
                        ParserWarnings.addWarning(references1.get(i).result, ParserWarnings.AMBIGUOUS);
                    HelperFunctions.normalizeMatchLevel(references1.get(i).result);
                    return references1.get(i).result; // If we have a perfect match, there's no need for more parsing.
                }

                if (references1.get(i).ambiguous) // If the parsed reference is ambiguous, parse it strictly...
                {
                    ParserWarnings.addWarning(references1.get(i).result, ParserWarnings.AMBIGUOUS);
                    ParsingResult<Reference> strictVersion = parserFunctions.get(i).apply(true);
                    references2.add(strictVersion); // ...and add it to the list.
                    index = 0; // Go back to the first Token.

                    if (strictVersion.result.matchLevel >= 1f) {
                        HelperFunctions.normalizeMatchLevel(strictVersion.result);
                        return strictVersion.result;
                    }
                }
            }

            references1.addAll(references2); // Put all References into the first list.

            int maxInd = 0; // A maximum search for the best fit.
            for (int i = 1; i < references1.size(); i++) {
                if (references1.get(i).result.matchLevel > references1.get(maxInd).result.matchLevel)
                    maxInd = i;
            }

            return references1.get(maxInd).result; // Unpack the parsed Reference from the ParsingResult object.
        } else {
            Function<Boolean, ParsingResult<Reference>> selectedParser = null;
            switch (mode) { // Select the parser function.
            case Article:
                selectedParser = parserFunctions.get(0);
                break;
            case Book:
                selectedParser = parserFunctions.get(1);
                break;
            case Chapter:
                selectedParser = parserFunctions.get(2);
                break;
            case Patent:
                selectedParser = parserFunctions.get(3);
                break;
            case Standard:
                selectedParser = parserFunctions.get(4);
                break;
            case Theses:
                selectedParser = parserFunctions.get(5);
                break;
            default: // Only Web is possible at this point.
                selectedParser = parserFunctions.get(6);
                break;
            }
            ParsingResult<Reference> heuristicResult = selectedParser.apply(false);
            if (heuristicResult.ambiguous) {
                ParserWarnings.addWarning(heuristicResult.result, ParserWarnings.AMBIGUOUS);
                ParsingResult<Reference> strictVersion = selectedParser.apply(true);
                if (strictVersion.result.matchLevel > heuristicResult.result.matchLevel) {
                    HelperFunctions.normalizeMatchLevel(strictVersion.result);
                    return strictVersion.result;
                }
            }
            HelperFunctions.normalizeMatchLevel(heuristicResult.result);
            return heuristicResult.result;
        }
    }

    /// Parses the input string as an ArticleReference.
    private ParsingResult<Reference> getReferenceArticle(boolean strict) {
        ArticleReference ref = new ArticleReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        boolean zeroMatch = false; // True if the input cannot be an article reference.
        boolean exclusiveMatch = false; // True if the input MUST be an article reference.
        boolean shortArticle = false; // True if the input is a short article reference.

        ref.matchLevel = parsePrefixInto(ref, AuthorsMode.AtLeastOne);

        if (!strict && ref.authors.get(0).isOnlyOneNamePart()) { // isOnlyOneNamePart() implies that size() == 1.
            index = 0; // Go back to the first Token.
            ref = new ArticleReference();
            result = new ParsingResult<Reference>(ref);
            shortArticle = true;

            ParserWarnings.addWarning(ref, "References should start with the authors.");

            ref.title = HelperFunctions.withoutDot(parseUntilLBr(false));
            if (ref.title != null)
                ref.matchLevel++;

            ref.date = parseDate();
            if (ref.date != null)
                ref.matchLevel++;
        } else {
            ParsingResult<String> title = parseTitle(strict);
            ref.title = title.result;
            result.ambiguous = title.ambiguous; // The result is ambiguous if the title is ambiguous.
            if (ref.title != null)
                ref.matchLevel++;
        }

        ref.journalTitle = parseUntil(TokenType.Comma, TokenType.Dot);
        getNext(); // Get the next comma or dot.
        if (ref.journalTitle != null)
            ref.matchLevel++;

        ref.volume = parseUntil(TokenType.LBracket, TokenType.Comma);
        if (ref.volume != null) {
            ref.matchLevel++;
            if (peekNext().type == TokenType.LBracket) {
                getNext();
                ref.issue = parseUntil(TokenType.RBracket);
                if (ref.issue == null)
                    zeroMatch = true;
                else {
                    getNext(); // Get the RBracket.
                    exclusiveMatch = true;
                }
            } else
                ParserWarnings.addWarning(ref, "A journal article should contain an issue number.");
        } else { // An article ref. must contain two fields in the format 'number(number)''.
            zeroMatch = true;
            ParserWarnings.addWarning(ref, "A journal article must contain a volume number.");
        }

        if (peekNext().type == TokenType.Comma) // The comma is officially NOT optional.
            getNext();

        ref.pageRange = parsePageRange();
        if (ref.pageRange != null)
            ref.matchLevel++;
        else {
            zeroMatch = true;
            ParserWarnings.addWarning(ref, "A journal article must contain a page range.");
        }

        ignoreDot(); // The dot is officially NOT optional.

        ref.DOI = parseDOI(ref);
        if (ref.DOI != null)
            ref.matchLevel++;
        ref.URL = parseRetrievedFrom();
        if (ref.URL != null && ref.DOI == null)
            ref.matchLevel++; // If both URL and DOI is present that's one point.

        parseRemainderWithWarning(ref); // If there are remaining tokens, register them.

        if (shortArticle) // A short article (without authors) contains less fields.
            ref.matchLevel /= 5f;
        else
            ref.matchLevel /= 7f;

        if (exclusiveMatch)
            ref.matchLevel = 2f;
        else if (zeroMatch)
            ref.matchLevel = -1f;

        return result;
    }

    /// Parses the input string as a BookReference.
    private ParsingResult<Reference> getReferenceBook(boolean strict) {
        BookReference ref = new BookReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        ref.matchLevel = 0f;

        ref.matchLevel = parsePrefixInto(ref, AuthorsMode.AtLeastOne);

        ref.title = parseUntil(TokenType.LBracket, TokenType.Dot);
        if (ref.title != null)
            ref.matchLevel++;

        if (peekNext().type == TokenType.LBracket) { // title (edition)
            getNext();
            ref.edition = parseUntil(TokenType.RBracket);
            getNext();
            ignoreDot();
        } else if (peekNext().type == TokenType.Dot) // title.
            getNext();

        ref.URL = parseRetrievedFrom();
        if (ref.URL != null) // No publisher or place, just a URL; probably an e-book.
            ref.matchLevel /= 3f;
        else {
            ref.publisherPlace = parseUntil(TokenType.Colon, TokenType.Dot);
            if (ref.publisherPlace == null)
                ParserWarnings.addWarning(ref, "The publisher's place is not defined.");
            else
                ref.matchLevel++;

            if (getNext().type == TokenType.Colon) { // place: publisher
                getNext();
                ref.publisher = parseUntil(TokenType.Dot);
                if (ref.publisherPlace == null)
                    ParserWarnings.addWarning(ref, "There's no publisher defined.");
                else
                    ref.matchLevel++;
                ignoreDot();
            } else if (getNext().type == TokenType.Dot)
                getNext();

            ref.DOI = parseDOI(ref);
            ref.URL = parseRetrievedFrom();
            ref.matchLevel /= 5f;
        }

        parseRemainderWithWarning(ref);

        return result;
    }

    /// Parses the input string as a ChapterReference.
    private ParsingResult<Reference> getReferenceChapter(boolean strict) {
        ChapterReference ref = new ChapterReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);

        ref.matchLevel = parsePrefixInto(ref, AuthorsMode.AtLeastOne);

        ref.date = parseDate();
        if (ref.date != null)
            ref.matchLevel++;
        else
            ParserWarnings.addDateWarning(ref);

        ParsingResult<String> title = parseTitle(strict); // This is the title of the CHAPTER, not the book.
        ref.title = title.result;
        result.ambiguous = title.ambiguous; // The result is ambiguous if the title is ambiguous.
        if (ref.title != null && !ref.title.isEmpty())
            ref.matchLevel++;

        ignoreSpace();
        if (peekNext().lexeme.equalsIgnoreCase("In")) {
            getNext();
            ref.bookTitle = parseUntilDot(true);
            if (ref.bookTitle != null)
                ref.matchLevel++;
        }

        if (canRead()) { // If there are remaining tokens, register them.
            ref.restOfAPA = parseUntilEnd();
            ParserWarnings.addRemainderWarning(ref);
        }

        ref.matchLevel /= 4f; // TODO
        return result;
    }

    /// Parses the input string as a WebReference.
    private ParsingResult<Reference> getReferenceWebsite(boolean strict) {
        WebReference ref = new WebReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        boolean zeroMatch = false; // True if the input cannot be a web reference.
        ref.matchLevel = 0f;

        ref.matchLevel = parsePrefixInto(ref, AuthorsMode.AtLeastOne);

        ParsingResult<String> title = parseTitle(strict);
        ref.title = title.result;
        result.ambiguous = title.ambiguous; // The result is ambiguous if the title is ambiguous.
        if (ref.title != null && !ref.title.isEmpty())
            ref.matchLevel++;

        ignoreSpace();
        ref.URL = parseRetrievedFrom();
        if (ref.URL == null) {
            ParserWarnings.addWarning(ref, ParserWarnings.URL_MISSING);
            zeroMatch = true;
        } else
            ref.matchLevel++;

        if (canRead()) { // If there are remaining tokens, register them.
            ref.restOfAPA = parseUntilEnd();
            ParserWarnings.addRemainderWarning(ref);
        }

        if (zeroMatch)
            ref.matchLevel = -1f;
        else
            ref.matchLevel /= 4f;
        return result;
    }

    /// Parses the input string as a PatentReference.
    private ParsingResult<Reference> getReferencePatent(boolean strict) {
        PatentReference ref = new PatentReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        ref.matchLevel = 0f;

        ref.matchLevel = parsePrefixInto(ref, AuthorsMode.AtLeastOne);

        ignoreSpace();
        ref.patentNumber = parsePatentNumber();
        if (ref.patentNumber == null)
            ref.matchLevel = 0f; // The next field MUST be the patent number.
        else
            ref.matchLevel++;

        ignoreSpace();
        ref.URL = parseRetrievedFrom(); // optional

        ignoreSpace();
        ref.publisherPlace = HelperFunctions.withoutDot(parseUntilEnd()); // optional

        ref.matchLevel /= 3f;
        return result;
    }

    /// Parses the input string as a StandardReference.
    private ParsingResult<Reference> getReferenceStandard(boolean strict) {
        StandardReference ref = new StandardReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        boolean zeroMatch = false; // True if the input cannot be a standard reference.
        ref.matchLevel = 0f;

        ref.matchLevel = parsePrefixInto(ref, AuthorsMode.AtLeastOne);

        ref.title = parseUntilLBr(false);
        if (ref.title != null && !ref.title.isEmpty())
            ref.matchLevel++;

        ref.standardNumber = parseStandardNumber();
        if (ref.standardNumber == null) {
            ParserWarnings.addWarning(ref, "A standard number must be specified.");
            zeroMatch = true;
        } else
            ref.matchLevel++;

        ignoreSpace();
        ref.URL = parseRetrievedFrom();
        if (ref.URL == null) {
            ignoreSpace();
            ref.publisher = HelperFunctions.withoutDot(parseUntilEnd());
            if (ref.publisher != null)
                ref.matchLevel++;
        } else
            ref.matchLevel++;

        if (canRead()) { // If there are remaining tokens, register them.
            ref.restOfAPA = parseUntilEnd();
            ParserWarnings.addRemainderWarning(ref);
        }

        if (zeroMatch)
            ref.matchLevel = -1f;
        else
            ref.matchLevel /= 5f;
        return result;
    }

    /// Parses the input string as a ThesisReference.
    private ParsingResult<Reference> getReferenceThesis(boolean strict) {
        ThesisReference ref = new ThesisReference();
        ParsingResult<Reference> result = new ParsingResult<Reference>(ref);
        boolean zeroMatch = false; // True if the input cannot be a theses reference.
        ref.matchLevel = 0f;

        ref.matchLevel = parsePrefixInto(ref, AuthorsMode.JustOne);
        if (ref.authors != null && ref.authors.size() > 1)
            zeroMatch = true;

        ref.title = parseUntilLBr(false);
        if (ref.title != null && !ref.title.isEmpty())
            ref.matchLevel++;

        if (peekNext().type != TokenType.LBracket) { // Missing genre.
            zeroMatch = true;
            ParserWarnings.addWarning(ref, "The genre of the thesis is missing.");
            parseRemainderWithWarning(ref);
        } else { // Genre NOT missing.
            getNext();

            String next = parseUntil(TokenType.RBracket, TokenType.Comma);
            if (peekNext().type == TokenType.RBracket) { // (genre) *
                ref.genre = next;
                getNext();

                ignoreDot();
                ref.URL = parseRetrievedFrom();
                if (ref.URL != null) {
                    zeroMatch = true;
                    ParserWarnings.addIllegalFormatWarning(ref);
                }

                ref.publisher = HelperFunctions.withoutDot(parseUntilComma(true));
                if (ref.publisher != null)
                    ref.matchLevel++;
                else
                    ParserWarnings.addWarning(ref, ParserWarnings.PUBLISHER_MISSING);

                ref.publisherPlace = HelperFunctions.withoutDot(parseUntilEnd());
                if (ref.publisherPlace != null)
                    ref.matchLevel++;
                else
                    ParserWarnings.addWarning(ref, ParserWarnings.PUBLISHING_PLACE_MISSING);

                ref.matchLevel /= 4f; // There are 4 key fields on this path.
            } else if (peekNext().type == TokenType.Comma) { // (genre, *) Retrieved from URL
                ref.genre = next;
                getNext();

                ref.publisher = parseUntil(TokenType.RBracket, TokenType.Comma);
                if (ref.publisher != null)
                    ref.matchLevel++;
                else
                    ParserWarnings.addWarning(ref, ParserWarnings.PUBLISHER_MISSING);

                if (peekNext().type == TokenType.Comma) { // (genre, pub, pubpl) Retrieved from URL
                    getNext();

                    ref.publisherPlace = parseUntil(TokenType.RBracket);
                    if (ref.publisherPlace != null)
                        ref.matchLevel++;
                    else
                        ParserWarnings.addWarning(ref, ParserWarnings.PUBLISHING_PLACE_MISSING);

                    getNext(); // Get the closing bracket.
                } else if (peekNext().type == TokenType.RBracket) { // (genre, pub) Retrieved from URL
                    ParserWarnings.addWarning(ref, ParserWarnings.PUBLISHING_PLACE_MISSING);
                    getNext();
                } else // error
                    parseRemainderWithWarning(ref);

                ignoreDot();
                ref.URL = parseRetrievedFrom();
                if (ref.URL != null)
                    ref.matchLevel++;
                else
                    ParserWarnings.addWarning(ref, ParserWarnings.URL_MISSING);

                ref.matchLevel /= 6f; // There are 6 key fields on this path.
            } else // error
                parseRemainderWithWarning(ref);
        }

        parseRemainderWithWarning(ref);

        if (zeroMatch)
            ref.matchLevel = -1f;
        return result;
    }
    // endregion

    /// Parses the remaining tokens into the 'note' field, and adds a warning.
    private void parseRemainderWithWarning(Reference ref) {
        ref.restOfAPA = parseUntilEnd();
        if (ref.restOfAPA != null)
            ParserWarnings.addRemainderWarning(ref);
    }

    /// Parses authors and date into the given Reference, and returns the a
    /// 'match level' value from { 0, 1, 2 }.
    private float parsePrefixInto(Reference ref, AuthorsMode mode) {
        float matchLevel = 0f;

        ref.authors = parseAuthors(ref);
        if (ref.authors == null)
            ParserWarnings.addNoAuthorsWarning(ref);
        else {
            switch (mode) {
            case AtLeastOne:
                if (ref.authors.size() == 0)
                    ParserWarnings.addWarning(ref, "At least one author must be specified.");
                else
                    matchLevel++;
                break;
            case MoreThanOne:
                if (ref.authors.size() <= 1)
                    ParserWarnings.addWarning(ref, "At least two authors must be specified.");
                else
                    matchLevel++;
                break;
            case JustOne:
                if (ref.authors.size() != 1)
                    ParserWarnings.addWarning(ref, "Exactly one author must be specified.");
                else
                    matchLevel++;
                break;
            }
        }

        ref.date = parseDate();
        if (ref.date != null) {
            matchLevel++;
            if (ref.date.dateParts.size() == 0) {
                ref.date = null;
                ParserWarnings.addWarning(ref, "The publication date is not specified (n.d.).");
            }
        } else
            ParserWarnings.addDateWarning(ref);

        return matchLevel;
    }

    /// Parses the 'retrieved from' (or just 'from') field and returns if without
    /// the
    /// string "retrieved from" (or just "from").
    /// If the initial words are found, all remaining token are read.
    private String parseRetrievedFrom() {
        ignoreSpace();
        if (peekNext().lexeme.equalsIgnoreCase("Retrieved")) {
            getNext();
            ignoreSpace();
        }
        if (peekNext().lexeme.equalsIgnoreCase("from")) {
            getNext();
            ignoreSpace();
            return parseURL();
        }
        return null;
    }

    /// Parses a patent number that ends with a number and a dot after it.
    private String parsePatentNumber() {
        StringBuilder sb = new StringBuilder();
        boolean containsNo = false;

        while (canRead() && !HelperFunctions.containsDigit(peekNext().lexeme)) {
            if (!containsNo && peekNext().type == TokenType.Word && peekNext().lexeme.equalsIgnoreCase("No"))
                containsNo = true;
            sb.append(getNext().lexeme);
        }
        if (!canRead() || !containsNo) // EOF denotes an invalid patent number.
            return null;

        sb.append(parseUntilDot(true));

        return sb.length() == 0 ? null : sb.toString();
    }

    /// Parses the title Tokens into a String.
    /// In strict mode, the parsing will stop at the first dot.
    /// In non-strict mode, the parser may consider some dots to be part of
    /// the title.
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

            if (brDepth == 0 && isTitleTerminator(next.type)) {
                if (strict || shouldBreakInNonStrict(last, next)) { // If we are parsing it strictly, break on the
                    if (next.type != TokenType.Dot) // 'title terminator'; or in non-strict mode,
                        sb.append(next.lexeme); // use shouldBreakInNonStrict() to detemine...
                    break;
                } else // If the title terminator is considered a part of the title in non-strict mode,
                       // the input is ambiguous.
                    ambiguity = true;
            }
            sb.append(next.lexeme);
        }

        return new ParsingResult<String>(sb.toString().trim(), ambiguity); // The second parameter is meaningless.
    }

    private boolean isTitleTerminator(TokenType token) {
        return token == TokenType.QuestionMark || token == TokenType.ExclaMark || token == TokenType.Dot;
    }

    /// Determines if the parser should break on a dot with the specified token
    /// before it.
    private boolean shouldBreakInNonStrict(Token last, Token next) {
        return isTitleTerminator(next.type) || next.type == Token.TokenType.Dot && last.type != Token.TokenType.Integer
                && last.lexeme.toLowerCase() != "vol" && !HelperFunctions.isRomanNumeral(last.lexeme);
    }

    /// Parses a page range in the format 'number-number' or just 'number'. Returns
    /// null on error.
    private String parsePageRange() {
        String begin;

        ignoreSpace();
        if (peekNext().type != Token.TokenType.Integer)
            return null;
        else
            begin = getNext().lexeme;

        ignoreSpace();
        if (peekNext().type == Token.TokenType.Hyphen) { // There's an 'end'.
            String end;
            getNext();
            ignoreSpace();
            if (peekNext().type != Token.TokenType.Integer)
                return null;
            else
                end = getNext().lexeme;
            ignoreSpace();
            if (peekNext().type != Token.TokenType.Dot)
                return null;
            else
                getNext();
            return begin + "-" + end;
        } else if (peekNext().type == Token.TokenType.Dot) { // There's no 'end', only 'begin'.
            getNext();
            return begin;
        } else
            return null;
    }

    /// Parses a DOI field. It must start with 'doi:'. Invalid DOIs may be accepted
    /// too.
    private String parseDOI(Reference ref) {
        ignoreSpace();
        if (!peekNext().lexeme.equalsIgnoreCase("doi"))
            return null;
        else
            getNext();

        ignoreSpace();
        if (peekNext().type != TokenType.Colon)
            return null;
        else
            getNext();

        ignoreSpace();
        if (peekNext().type != TokenType.DOI) {
            ParserWarnings.addWarning(ref, "Invalid DOI.");
            return parseWhile(TokenType.Integer, TokenType.Dot, TokenType.DOI, TokenType.Hyphen, TokenType.Slash);
        } else
            return getNext().lexeme;
    }

    private String parseURL() {
        return parseUntil(TokenType.WhSpace); // WhSpace cannot be in a URL.
    }

    /// Parses a list of name parts into an ArrayList of Authors.
    private ArrayList<CSLAuthorStruct> parseAuthors(Reference ref) {
        ArrayList<String> authors0 = new ArrayList<String>();
        ArrayList<CSLAuthorStruct> authors1 = new ArrayList<CSLAuthorStruct>();

        authors0.add(parseUntilNameDelimiter());
        if (authors0.get(0) == null)
            return null;
        while (peekNext().type == Token.TokenType.Comma || peekNext().type == Token.TokenType.TripleDots
                || peekNext().type == TokenType.And) {
            getNext();
            String nextName = parseUntilNameDelimiter();
            if (nextName != null && !nextName.isEmpty())
                authors0.add(nextName);
            if (authors0.get(authors0.size() - 1) == null)
                return null;
        }

        for (int i = 0; i < authors0.size() - 1; i += 2) {
            authors0.set(i, HelperFunctions.trimAll(authors0.get(i)));
            authors0.set(i + 1, HelperFunctions.trimAll(authors0.get(i + 1)));
            authors1.add(new CSLAuthorStruct(authors0.get(i), authors0.get(i + 1)));
        }
        if (authors0.size() % 2 == 1) { // In case the list of names ends with a rogue name.
            authors0.set(authors0.size() - 1, HelperFunctions.trimAll(authors0.get(authors0.size() - 1)));
            authors1.add(new CSLAuthorStruct(authors0.get(authors0.size() - 1)));
            ParserWarnings.addWarning(ref,
                    "The last author (" + authors0.get(authors0.size() - 1) + ") has only one name.");
        }

        return authors1;
    }

    /// Parses the date or date range tokens into a CSLDateStruct.
    private CSLDateStruct parseDate() {
        ArrayList<String> serialized = new ArrayList<String>(1);

        ignoreSpace();
        if (peekNext().type != Token.TokenType.LBracket) // Expect a left bracket.
            return null;
        else
            getNext();

        while (canRead() && peekNext().type != TokenType.RBracket) { // Read date parts.
            String next = parseUntilDateDelimiter();
            if (next != null)
                serialized.add(next);
            if (peekNext().type != TokenType.RBracket) {
                if (peekNext().type == TokenType.Hyphen || peekNext().type == TokenType.Slash)
                    serialized.add(getNext().lexeme);
                else
                    getNext();
            }
        }
        if (canRead()) // If the input is correct, read the next RBracket.
            getNext();
        else
            return null;

        ignoreSpace();
        if (peekNext().type != Token.TokenType.Dot)
            return null;
        else
            getNext();

        return HelperFunctions.datePartsToCSLDateStruct(serialized);
    }

    /// Parses the standard number into a String.
    private String parseStandardNumber() {
        StringBuilder standard = new StringBuilder();
        boolean lastIsNum = false;
        String firstLexeme = null;

        ignoreSpace();
        if (peekNext().type != Token.TokenType.LBracket) // Expect a left bracket.
            return null;
        else
            getNext();

        while (canRead() && peekNext().type != TokenType.RBracket) { // Read standard number parts.
            if (firstLexeme == null)
                firstLexeme = peekNext().lexeme;
            lastIsNum = peekNext().type == TokenType.Integer;
            standard.append(getNext().lexeme);
        }
        if (canRead()) // If the input is correct, read the next RBracket.
            getNext();
        else
            return null;

        ignoreSpace();
        if (peekNext().type != Token.TokenType.Dot)
            return null;
        else
            getNext();

        boolean correct = lastIsNum && !firstLexeme.equalsIgnoreCase("vol");

        return correct ? standard.toString() : null;
    }

    /// Calls parseUntil with Dot.
    private String parseUntilDot(boolean skipLast) {
        String result = parseUntil(Token.TokenType.Dot);
        if (skipLast && canRead())
            getNext();
        return result;
    }

    /// Calls parseUntil with Comma.
    private String parseUntilComma(boolean skipLast) {
        String result = parseUntil(Token.TokenType.Comma);
        if (skipLast && canRead())
            getNext();
        return result;
    }

    /// Calls parseUntil with Comma and RBracket.
    private String parseUntilDateDelimiter() {
        String result = parseUntil(Token.TokenType.Comma, Token.TokenType.RBracket, Token.TokenType.Hyphen,
                Token.TokenType.WhSpace, TokenType.Slash);
        return result;
    }

    /// Calls parseUntil with LBracket.
    private String parseUntilLBr(boolean skipLast) {
        String result = parseUntil(Token.TokenType.LBracket);
        if (skipLast && canRead())
            getNext();
        return result;
    }

    /// Calls parseUntil with Comma, LBracket and TripleDots; with !skipLast.
    private String parseUntilNameDelimiter() {
        String result = parseUntil(TokenType.Comma, TokenType.LBracket, TokenType.TripleDots, TokenType.And);
        if (peekNext().type == TokenType.LBracket && editorTokenAhead()) {
            getNext();
            result += " (" + parseUntil(TokenType.RBracket) + ")";
            getNext();
            ignoreDot(); // There may be a dot after "(ed.)".
        }
        return result;
    }

    /// Parses the input until a Token with a specified TokenType is met.
    /// (Except if they are inside brackets.)
    /// Leading and trailing whitespaces are ignored.
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

    /// Returns the remaining tokens as a string.
    private String parseUntilEnd() {
        StringBuilder sb = new StringBuilder();
        while (canRead())
            sb.append(getNext().lexeme);
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    /// Returns the next consecutive tokens of the specified type(s).
    private String parseWhile(Token.TokenType... valid) {
        StringBuilder sb = new StringBuilder();
        List<Token.TokenType> valids = Arrays.asList(valid);
        while (canRead() && valids.contains(peekNext().type))
            sb.append(getNext().lexeme);
        return sb.length() == 0 ? null : sb.toString().trim();
    }

    /// Determines if the next Tokens are "(ed)" or "(ed.)" or "(eds)" or "(eds.)".
    private boolean editorTokenAhead() {
        ArrayList<Token> next = peekAhead(4);
        boolean goodLexeme = next.get(1).lexeme.equalsIgnoreCase("ed") || next.get(1).lexeme.equalsIgnoreCase("eds");
        return next.get(0).type == TokenType.LBracket && goodLexeme && (next.get(2).type == TokenType.RBracket
                || next.get(2).type == TokenType.Dot && next.get(3).type == TokenType.RBracket);
    }

}