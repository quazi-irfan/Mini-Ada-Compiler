import ParserPkg.Parser;

/**
 * Quazi Irfan
 * Compiler
 * Assignment 5
 * Add symbol to symbol table during recursive descent parsing
 */

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.out.println("Enter input file name as 2nd argument.");
            return;
        }

        Parser parser = new Parser(args[0]);
        if(parser.isParsingSuccessful())
            System.out.println("Parsing successful.");
    }
}
