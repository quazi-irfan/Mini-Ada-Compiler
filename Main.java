import ParserPkg.Parser;
import SymbolTablePkg.SymbolTable;
import TACx86Pkg.x86Translator;

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

        String adaFileName = args[0];
        Parser parser = new Parser(adaFileName);
        if(!parser.isParsingSuccessful()){
            System.out.println("Parsing " + adaFileName + " failed.");
            System.exit(1);
        }

        SymbolTable symbolTable = parser.getSymbolTable();
        String tacFileName = adaFileName.substring(0, adaFileName.length()-4).concat(".tac");
        x86Translator x86Translator = new x86Translator(tacFileName, symbolTable);
        if(x86Translator.isSuccessfullyTranslated()){
            System.out.println("Sucessfully created ASM file.");
        }
    }
}
