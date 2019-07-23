package hu.sztaki.dsd.refparser;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/// Converts an ArrayList of Tokens into a Reference object.
public final class RefParser {

    private static ArrayList<String> examples = new ArrayList<String>();

    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            loadFile(args[0]);
        else
            loadFile("tests/theses.txt");

        for (String apa : examples) {
            printParsedAPA(apa, ParsingMode.Theses);
            System.out.println();
            System.out.println();
            System.out.println();
        }
    }

    /// Prints the specified APA string and its JSON representation to System.out.
    public static void printParsedAPA(String apa, ParsingMode mode) {
        String csl = parseAPAtoCSL(apa, mode);
        System.out.println(csl); // Print out the APA string.
    }

    /// Converts an APA string into a Reference object.
    public static String parseAPAtoCSL(String apa, ParsingMode mode) {
        Reference ref = parseAPA(apa, mode);
        System.out.println(apa); // Write the APA string.
        GsonBuilder serializer = new GsonBuilder();
        serializer.setPrettyPrinting();
        JsonObject tree = (JsonObject) serializer.create().toJsonTree(ref);
        tree.addProperty("type", ref.typeToString()); // Add type information to the JSON tree.
        return serializer.create().toJson(tree);
    }

    /// Converts an APA string into a Reference in the specified mode.
    public static Reference parseAPA(String apa, ParsingMode mode) {
        Tokenizer tokenizer = new Tokenizer(apa);
        Parser parser = new Parser(tokenizer.tokenize());
        return parser.getReference(mode);
    }

    /// Converts an APA string into a Reference object. The type of the APA is
    /// determined automatically.
    public static Reference parseAPA(String apa) {
        return parseAPA(apa, ParsingMode.Automatic);
    }

    // Loads APA strings into the local field examples.
    // If a line starts with a hashtag, it is ignored.
    // If a line starts with an excl. mark, all the other lines are ignored.
    public static void loadFile(String path) throws IOException {
        examples.clear();
        FileReader stream = new FileReader(path); // Charset.forName("utf8")
        Scanner scanner = new Scanner(stream);
        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            if (nextLine.startsWith("#"))
                continue;
            else if (nextLine.startsWith("!")) {
                examples.clear();
                examples.add(nextLine.substring(1).trim());
                break;
            } else
                examples.add(nextLine.trim());
        }
        scanner.close();
        stream.close();
    }

}
