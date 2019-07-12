package hu.sztaki.dsd.refparser;

public class ParsingResult<T> {

    T result;
    float matchLevel;
    boolean ambiguous;

    public ParsingResult(T result) {
        this.result = result;
    }

    public ParsingResult(T result, float matchLevel, boolean ambiguous) {
        this.result = result;
        this.matchLevel = matchLevel;
        this.ambiguous = ambiguous;
    }

}