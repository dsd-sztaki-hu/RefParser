# RefParser - *APA to CSL JSON (Citation Style Language) parser*

**RefParser** is a Java project with the main task of extracting information from APA (http://libraryguides.vu.edu.au/apa-referencing/theses) style citations. The result is a CSL object in JSON format (https://citeproc-js.readthedocs.io/en/latest/csl-json/markup.html).


## Usage

The software can be used either as a library or a command line application. It can work in multiple parsing modes (defined in *ParsingMode.java*). *Automatic* tries to figure out the exact type (e.g. article, thesis) of the input.

As a command line app, it must have one argument that is a .txt file format with the APA style references one-per-line. If a line in this file starts with a hashmark (#), it is ignored. If a line starts with an exclamation mark (!), the previous lines are discarded. Empty lines are ignored too. The parsing mode is *Automatic*.

As a library, the *RefParser* class is used. It's a static class that can work in multiple ways. As a static class it holds a number of lines from the specified .txt file, loaded the following way:
> RefParser.loadFile("path to file")
> RefParser.parseAndPrint()

...or it can be used directly on a string containing an APA reference. The second argument can be omitted to set the parsing mode to *Automatic*. If returns a *Reference* object.
> RefParser.parseAPA("an APA style citation of an article", ParsingMode.Article)

To get a serialized JSON string, use *parseAPAtoCSL()* with the same parameters (the second arg. is non-optional). Use *printParsedAPA()* with the same args to print the result to *System.out*. There's no return value in this case.


## Parsing method

To parse strings, the code contains a custom recursive descent parser in *Parser.java*. The class *Parser* can be instantiated with an *ArrayList<Token\>*. The function *getReference(parsingMode)* returns the parsed *Reference* object. All other functions are private. Each type of references (such as article, book etc.) has a parser function. Each of these parser functions work in either *strict* or *ambiguous* mode. Ambiguous mode is requred to handle cases where parts of the citation contain tokens that are also used as delimiters of these parts, thereby causing ambiguity.


## Structure

| Folder | Description |
| - | - |
| /src | The source code of the application |
| /target | The compiled code |
| /tests | Textfiles containing examples (both correct and incorrect) for semi-automatic testing |

| Files within /src/hu/sztaki/dsd/refparser | |
| - | - |
| CSLAuthorStruct.java | Defines *CSLAuthorStruct*, which is a JSON-serializable class to represent an author in a *Reference* object. |
| CSLDateStruct.java | Defines *CSLDateStruct*, which is a JSON-serializable class to represent a date object in a *Reference* object. |
| HelperFunctions.java | A static class with a bunch of helper functions. |
| Pair.java | A simple tuple of a *Pattern* object and a *Token.TokenType* defining a token type with a regex pattern. |
| Parser.java | Implements *Parser*, the essential part of this software - described above. |
| ParserWarnings.java | Defines *ParserWarnings*, a static class that can be used to add (or remove) string warning messages to (or from) *Reference* objects. |
| ParsingMode.java | Defines *ParsingMode*, an enum type. |
| ParsingResult.java | Defines *ParsingResult<T\>*, which encapsulates an object of type *T* and a *boolean* field with it. |
| Reference.java | Defines *Reference* objects that are the output types of the parser. They can be serialized into JSON using *com.google.gson*. There are multiple types of references (e.g. *PatentReference*) that implement *Reference*. The class partially complies the standard described at https://github.com/citation-style-language/schema. |
| RefParser.java | The entry point of the software. Its usage is described above. |
| Token.java | Defines the *Token* class that represents a token with its *TokenType* type and lexeme. |
| Tokenizer.java | Defines *Tokenizer*, which is a class that converts an APA reference into an *ArrayList* of *Token*s. |