package ParserPkg;

/**
 * Quazi Irfan
 * Compiler
 * Assignment 3 :   Create a recursive descent parser for the CFG given in the previous assignment 2
 * Assignment 5 :   Add the appropriate semantic actions to the parser to insert -
 *                  all constants, variables and procedures into your symbol table.
 * Assignment 6 :   Add SeqOfStatments rules to the parser
 * Assignment 7 :   Add the appropriate actions to your parser to translate a Ada source program into Three Address Code.
 */

public class Main_A3_A5_A6_A7 {
    public static void main(String[] args) throws Exception{
        if(args.length < 1){
            System.out.println("Enter input file name as 2nd argument.");
            return;
        }

        Parser parser = new Parser(args[0]);
        if(parser.isParsingSuccessful())
            System.out.println("Parsing successful.");

    }
}
