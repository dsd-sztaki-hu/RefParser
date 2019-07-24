package hu.sztaki.dsd.refparser;

import com.google.gson.annotations.SerializedName;

public class CSLAuthorStruct {
    
    @SerializedName("given")
    public String given;

    @SerializedName("family")
    public String family;

    public CSLAuthorStruct(String family, String given) {
        this.family = family;
        this.given = given;
    }

    public CSLAuthorStruct(String given) {
        family = null;
        this.given = given;
    }

    public String toString() {
        return family == null ? given : family + ", " + given;
    }

    public boolean isOnlyOneNamePart() {
        return family == null && given != null;
    }

}