public class Main {
    public static void main(String[] args) throws Exception{
        if(args.length < 1){
            System.out.println("Enter input file name as 2nd argument.");
            return;
        }

        Lexer lexer = new Lexer(args[0]);

        Token token = lexer.getNextToken();

        while(token != null){
            System.out.println(token);

            token = lexer.getNextToken();
        }
    }
}
