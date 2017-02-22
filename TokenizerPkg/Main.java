package TokenizerPkg;

import java.io.IOException;

/**
 * Quazi Irfan
 * Compiler
 * Assignment 1
 * Lexical analyzer for a subset of Ada
 */

public class Main {
    public static void main(String[] args) throws IOException{
        if(args.length < 1){
            System.out.println("Enter input file name as 2nd argument.");
            return;
        }

        Tokenizer tokenizer = new Tokenizer(args[0]);
        System.out.println("Number of tokens " + tokenizer.getTokenList().size());
        System.out.println("LineNumber          Tokens              Lexeme               Attributes");
        for(int i=0; i<tokenizer.getTokenList().size(); ) {
            // todo import and use Utility.enterToContinue()

            System.out.println(tokenizer.getTokenList().get(i));
            i++; // i in cremented later since we have to print the previous value first.
        }
    }
}
