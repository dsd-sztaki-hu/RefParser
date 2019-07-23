package hu.sztaki.dsd.refparser;

import java.util.ArrayList;

final class ParserWarnings {

    public static final String PUBLISHING_PLACE_MISSING = "The publishing place is missing.";
    public static final String PUBLISHER_MISSING = "The publisher is missing.";
    public static final String URL_MISSING = "No URL was specified.";
    
    public static final String AMBIGUOUS = "There is ambiguity in the title.";

    public static void addNoAuthorsWarning(Reference ref) {
        addWarning(ref, "There are no authors specified.");
    }

    public static void addRemainderWarning(Reference ref) {
        addWarning(ref, "The reference has fields that cannot be classified.");
    }

    public static void addDateWarning(Reference ref) {
        addWarning(ref, "The date field is missing or syntactically incorrect.");
    }

    public static void addIllegalFormatWarning(Reference ref) {
        addWarning(ref, "Illegal " + ref.typeToString() + " reference format.");
    }

    public static void addWarning(Reference ref, String warning) {
        if (ref.warnings == null)
            ref.warnings = new ArrayList<String>(1);
        ref.warnings.add(warning);
    }

}