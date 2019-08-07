package hu.sztaki.dsd.refparser;

/// Encapsulates an object with meta-information about it.
public class ParsingResult<T> {

    T result;
    boolean ambiguous;

    public ParsingResult(T result) {
        this.result = result;
        ambiguous = false;
    }

    public ParsingResult(T result, boolean ambiguous) {
        this.result = result;
        this.ambiguous = ambiguous;
    }

}