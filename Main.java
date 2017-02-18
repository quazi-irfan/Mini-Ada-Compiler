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
    public static void main(String[] args) throws Exception{
        if(args.length < 1){
            System.out.println("Enter input file name as 2nd argument.");
            return;
        }

        // starts the main loop
        Scanner scanner = new Scanner(args[0]);
        System.out.println("Number of tokens " + scanner.getTokenList().size());
        System.out.println("LineNumber          Tokens              Lexeme               Attributes");
        for(int i=0; i<scanner.getTokenList().size(); ) {
            System.out.println(scanner.getTokenList().get(i));
            i++;
            // break in output
//            if (i % 10 == 0)
//                enterToContinue();
        }

    }

    // page brake utility function
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
