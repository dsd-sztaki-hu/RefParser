package hu.sztaki.dsd.refparser;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public final class RefParser {

    private static ArrayList<String> examples = new ArrayList<String>();

    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            loadFile(args[0]);
        else
            loadFile("tests/standards.txt");

        for (String apa : examples) {
            printParsedAPA(apa);
            System.out.println();
            System.out.println();
        }
    }

    // Prints the specified APA string and its JSON representation to System.out.
    public static void printParsedAPA(String apa) {
        Reference ref = parseAPA(apa);
        System.out.println(apa); // Write the APA string.
        GsonBuilder serializer = new GsonBuilder();
        serializer.setPrettyPrinting();
        JsonObject tree = (JsonObject) serializer.create().toJsonTree(ref);
        tree.addProperty("type", ref.typeToString()); // Add type information to the JSON tree.
        System.out.println(serializer.create().toJson(tree));
        System.out.println();
    }

    // Converts an APA string into a Reference object.
    public static String parseAPAtoCSL(String apa) {
        Reference ref = parseAPA(apa);
        System.out.println(apa); // Write the APA string.
        GsonBuilder serializer = new GsonBuilder();
        serializer.setPrettyPrinting();
        JsonObject tree = (JsonObject) serializer.create().toJsonTree(ref);
        tree.addProperty("type", ref.typeToString()); // Add type information to the JSON tree.
        return serializer.create().toJson(tree);
    }

    // Converts an APA string into a Reference object.
    public static Reference parseAPA(String apa) {
        Tokenizer tokenizer = new Tokenizer(apa);
        Parser parser = new Parser(tokenizer.tokenize());
        return parser.getReference();
    }

    // Loads APA strings into the local field examples.
    // If a line starts with a hashtag, it is ignored.
    public static void loadFile(String path) throws IOException {
        examples.clear();
        FileReader stream = new FileReader(path); // Charset.forName("utf8")
        Scanner scanner = new Scanner(stream);
        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            if (!nextLine.startsWith("#")) {
                examples.add(nextLine);
            }
        }
        scanner.close();
        stream.close();
    }

    public static String parseNumOfPages(String volumeString) {
        StringBuilder numOfPages = null;
        for (Character ch : volumeString.toCharArray()) {
            if (ch == '(')
                numOfPages = new StringBuilder();
            else if (ch != ')' && numOfPages != null)
                numOfPages.append(ch);
        }
        return numOfPages == null ? null : numOfPages.toString();
    }

}
