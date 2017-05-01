import ParserPkg.Parser;

import java.io.PrintWriter;

/**
 * Quazi Irfan
 * Compiler
 * Assignment 8 : Translate your Three Address Code into 8086 assembly language.
 */

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length < 1){
            System.out.println("Enter input file name as 2nd argument.");
            return;
        }

        Parser parser = new Parser(args[0]);
        if(!parser.isParsingSuccessful()){
            System.out.println("Parsing " + args[0] + " failed.");
            System.exit(1);
        }

//        String asmFileName = args[0].substring(0, args[0].length()-4).concat(".asm");
//        PrintWriter asmWriter = new PrintWriter(asmFileName);
    }
}
