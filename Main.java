import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception{
        int numOfTokens = 0;
        int numOfOutput = 0;

        if(args.length < 1){
            System.out.println("Enter input file name as 2nd argument.");
            return;
        }

        Lexer lexer = new Lexer(args[0]);

        Token token = lexer.getNextToken();

        System.out.println("TokenType            Lexeme                  Attribute");
        while(token.getTokenType() != TokenType.eof){
            numOfTokens++; numOfOutput++;
            System.out.println(token);

            if(numOfOutput % 5 == 0) enterToContinue();

            token = lexer.getNextToken();
        }
        System.out.println(token);
        System.out.println("Total number of tokens " + ++numOfTokens);  // ++ is to account for eof token
    }

    public static void enterToContinue(){
        System.out.print(" Press Enter to see the next 5 tokens.");
        try{
            BufferedReader tempHalt = new BufferedReader(new InputStreamReader(System.in));
            tempHalt.readLine();
        } catch (IOException e){
            System.out.println(e);
        }
    }
}
