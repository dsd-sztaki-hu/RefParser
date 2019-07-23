package hu.sztaki.dsd.refparser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public abstract class Reference {

    @SerializedName("match-level")
    public double matchLevel;

    // @SerializedName("title-ambiguity")
    // public boolean ambiguity = false;

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
        else if (getClass() == PatentReference.class)
            return "patent";
        else
            return "thesis";
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

    @SerializedName("warnings")
    public ArrayList<String> warnings;
}

class ArticleReference extends Reference {

    @SerializedName("container-title")
    public String journalTitle;

    @SerializedName("volume")
    public String volume;

    @SerializedName("issue")
    public String issue;

    @SerializedName("DOI")
    public String DOI;

    @SerializedName("URL")
    public String URL;

    @SerializedName("page")
    public String pageRange;

}

class BookReference extends Reference {

    @SerializedName("publisher") // TODO: name
    public String publisher;

    @SerializedName("edition") // TODO: name
    public String edition;

    @SerializedName("DOI")
    public String DOI;

    @SerializedName("URL")
    public String URL;

    @SerializedName("publisher-name") // TODO: name
    public String publisherName;

    @SerializedName("publisher-place") // TODO: name
    public String publisherPlace;

}

class ChapterReference extends BookReference {

    @SerializedName("book-title") // TODO: name
    public String bookTitle;

    @SerializedName("page") // TODO: name
    public String pageRange;

}

class WebReference extends Reference {

    @SerializedName("URL")
    public String URL;

    @SerializedName("article-title")
    public String articleTitle;

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

class ThesisReference extends Reference {

    @SerializedName("genre")
    public String genre;

    @SerializedName("publisher-place")
    public String publisherPlace;

    @SerializedName("publisher")
    public String publisher;

    @SerializedName("URL")
    public String URL;

}