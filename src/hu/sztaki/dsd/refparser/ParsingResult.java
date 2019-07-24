package hu.sztaki.dsd.refparser;

/// Encapsulates an object with meta-information about it.
public class ParsingResult<T> {

    T result;
    float matchLevel;
    boolean ambiguous;

    public ParsingResult(T result) {
        this.result = result;
        matchLevel = 0f;
        ambiguous = false;
    }

    public ParsingResult(T result, float matchLevel, boolean ambiguous) {
        this.result = result;
        this.matchLevel = matchLevel;
        this.ambiguous = ambiguous;
    }

}