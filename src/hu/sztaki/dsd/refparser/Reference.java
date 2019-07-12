package hu.sztaki.dsd.refparser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public abstract class Reference {

    @SerializedName("match-level")
    public double matchLevel;

    @SerializedName("ambiguity")
    public boolean ambiguity = false;

    @SerializedName("type")
    public String typeToString() {
        if (getClass() == ArticleReference.class)
            return "article-journal";
        else if (getClass() == BookReference.class)
            return "book";
        else if (getClass() == ChapterReference.class)
            return "chapter";
        else if (getClass() == StandardReference.class)
            return "standard";
        else if (getClass() == WebReference.class)
            return "website";
        else
            return "patent";
    }

    @SerializedName("title")
    public String title;

    @SerializedName("author")
    public ArrayList<Author> authors;

    @SerializedName("issued")
    public CSLDateStruct date;

    @SerializedName("note")
    public String restOfAPA; // If the given APA ends in unclassifiable tokens, this field will contain them.

    public String toJSON(boolean formatted) {
        Gson serializer;
        if (formatted)
            serializer = new GsonBuilder().setPrettyPrinting().create();
        else
            serializer = new GsonBuilder().create();
        String json = serializer.toJson(this);
        return json;
    }
}

class ArticleReference extends Reference {

    @SerializedName("container-title")
    public String journalTitle;

    @SerializedName("volume")
    public String volume;

    @SerializedName("page")
    public String pageRange;

    @SerializedName("DOI")
    public String DOI;

    @SerializedName("number-of-pages")
    public String numOfPages; // TODO: non-string

}

class BookReference extends Reference {

    @SerializedName("publisher")
    public String publisher;

    @SerializedName("DOI")
    public String DOI;

    @SerializedName("URL")
    public String URL;

    @SerializedName("publisher-name")
    public String publisherName;

    @SerializedName("number-of-pages")
    public String numOfPages; // TODO: non-string

}

class WebReference extends Reference {

    @SerializedName("URL")
    public String URL;

    @SerializedName("article-title")
    public String articleTitle;

}

class ChapterReference extends Reference {

    @SerializedName("publisher")
    public String publisher;

    @SerializedName("URL")
    public String URL;

    @SerializedName("DOI")
    public String DOI;

    @SerializedName("number-of-pages")
    public String numOfPages; // TODO: non-string

    @SerializedName("book-title") // TODO: name
    public String bookTitle;

}

class PatentReference extends Reference {

    @SerializedName("jurisdiction")
    public String Jurisdiction;

    @SerializedName("number")
    public String patentNumber;

    @SerializedName("URL")
    public String URL;

    @SerializedName("publisher-place")
    public String publisherPlace;

}

class StandardReference extends Reference {

    @SerializedName("number")
    public String standardNumber;

    @SerializedName("URL")
    public String URL;

    @SerializedName("publisher") // TODO: name
    public String publisher;

}