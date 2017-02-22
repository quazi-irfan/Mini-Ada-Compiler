import ParserPkg.Parser;

/**
 * Quazi Irfan
 * Compiler
 * Assignment 3
 * Recursive Descent Parser
 */
public class Main {
    public static void main(String[] args) throws Exception{
        if(args.length < 1){
            System.out.println("Enter input file name as 2nd argument.");
            return;
        }

        Parser parser = new Parser(args[0]);
    }
}
