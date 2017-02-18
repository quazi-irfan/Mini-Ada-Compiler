import ParserPkg.Parser;
import TokenizerPkg.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Quazi Irfan
 * Compiler
 * Assignment 1
 * Lexical analyzer for a subset of Ada
 */

public class Main {
    static Token currentToken = new Token(TokenType.unknown, null, 0);

    public static void main(String[] args) throws Exception{
        if(args.length < 1){
            System.out.println("Enter input file name as 2nd argument.");
            return;
        }

        Parser parser = new Parser(args[0]);
//        Tokenizer tokenizer = new Tokenizer(args[0]);
//        System.out.println("Number of tokens " + tokenizer.getTokenList().size());
//        System.out.println("LineNumber          Tokens              Lexeme               Attributes");
//        for(int i=0; i<tokenizer.getTokenList().size(); ) {
//            System.out.println(tokenizer.getTokenList().get(i));
//            i++;
//        }


    }

    // page brake utility function
    //            if (i % 10 == 0)
    //                enterToContinue();
    public static void enterToContinue(){
        System.out.print(" Press Enter to see the next 10 tokens.");
        try{
            BufferedReader tempHalt = new BufferedReader(new InputStreamReader(System.in));
            tempHalt.readLine();
        } catch (IOException e){
            System.out.println(e);
        }
    }
}
