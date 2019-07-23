package hu.sztaki.dsd.refparser;

public enum ParsingMode {
    Automatic, // In this mode, the parser tries to determine the type of the reference.
    Article, Book, Web, Chapter, Patent, Standard, Theses;
}