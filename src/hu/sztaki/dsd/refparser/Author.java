package hu.sztaki.dsd.refparser;

import com.google.gson.annotations.SerializedName;

public class Author {
    
    @SerializedName("given")
    public String given;

    @SerializedName("family")
    public String family;

    public Author(String family, String given) {
        this.family = family;
        this.given = given;
    }

    public Author(String given) {
        family = null;
        this.given = given;
    }

    public String toString() {
        return family == null ? given : family + ", " + given;
    }

}