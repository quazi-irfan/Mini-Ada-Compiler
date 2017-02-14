import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lexical analyzer class
 */
public class Lexer {
    private String fileName = null;
    public static int index = 0;
    public static String input = null;
    public static Token token = new Token(TokenType.unknown, null);
    public static BufferedReader reader = null;
    public List<Token> tokenList = new ArrayList<>();

    public Lexer(String fileName)throws IOException{
        this.fileName = fileName;

        populateTokenList();
    }

    /**
     * Generates the List of tokens available in the provided source file
     * @throws IOException
     */
    private void populateTokenList() throws IOException{
        reader = new BufferedReader(new FileReader(this.fileName));
        input = reader.readLine();
        while(input != null){
            index = 0;

            // while we are not at the end of line keep looping
            while(index < input.length()) {

                // while we don't have a parsable character move forward
                while (index < input.length() && input.charAt(index) <= 32){
                    index++;
                    continue;
                }

                // if our lookup for parsable character ends at the end of line break the loop
                if(index == input.length()) {
                    break;
                }

                // set the current token to unknown before we start parsing
                token = new Token(TokenType.unknown, null);
                if (String.valueOf(input.charAt(index)).matches("[a-zA-Z]")) {
                    processWordToken();
                } else if (String.valueOf(input.charAt(index)).matches("[0-9]")) {
                    processNumberToken();
                } else if (String.valueOf(input.charAt(index)).matches("[/<>:-]")) {
                    processDoubleToken();
                } else if (input.charAt(index) == '"') {
                    processStringLiteral();
                } else {
                    processSingleToken();
                }

                if(token != null)
                    tokenList.add(token);
            }

            input = reader.readLine();
        }

        // we readLine returns null, we've found end of file token
        token = new Token(TokenType.eof, "");
        tokenList.add(token);
    }

    /**
     * Remote the comment section from the input stream before gettoken takes over
     * @param str input source line
     * @return
     */
    private String removeComments(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        int location = str.indexOf("--");
        if(location != -1){
            for(int i = 0; i<location; i++){
                stringBuilder.append(input.charAt(i));
            }
            return stringBuilder.toString();
        }

        return str;
    }

    /**
     * This function process string literal
     */
    private static void processStringLiteral() {
        token.setTokenType(TokenType.string);
        StringBuilder stringBuilder = new StringBuilder();

        do {
            stringBuilder.append(Character.valueOf(input.charAt(index)));
            index++;

            // reaches the end
            if(index >= input.length()){
                token = new Token(TokenType.unknown, "Error missing string literal termination character " + stringBuilder.toString());
                break;
            }
        }while(input.charAt(index) != '"');


        // we have found the starting and ending " character of the string literal
        if(token.getTokenType() == TokenType.string){
            stringBuilder.append(Character.valueOf(input.charAt(index)));
            index++;

            String lexeme = stringBuilder.toString();
            token.setLexeme(lexeme);

            String literal = lexeme.substring(1, lexeme.length()-1);
            try{
                token.setLiteral(literal);
            }catch (UnsupportedValue e){
                System.out.println("Internal error : Trying to assign string value to non string token.");
            }
        } else {
            token.setTokenType(TokenType.unknown);
            token.setLexeme("Error missing string literal termination character " + stringBuilder.toString());
        }

    }

    /**
     * This function process double token
     */
    private static void processDoubleToken(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(input.charAt(index));

        try {
            if (input.charAt(index+1) == '=') {
                stringBuilder.append(input.charAt(index+1));
                index = index + 2;

                String lexeme = stringBuilder.toString();
                token.setLexeme(lexeme);

                if(lexeme.equals(":="))
                    token.setTokenType(TokenType.assignop);
                else
                    token.setTokenType(TokenType.relop);

            } else if (input.charAt(index+1) == '-') {
                index = input.length();
                token = null;
                return;
            } else {
                processSingleToken();
            }
            // if the try catch section throws an error, it's most likely String IndexOutofBound. This confirms that we have a single token
        } catch (StringIndexOutOfBoundsException e) {
            processSingleToken();
        }

    }

    /**
     * This function process single token
     */
    private static void processSingleToken() {
        char lexeme =  input.charAt(index);
        token.setLexeme(Character.toString(lexeme));

        switch(input.charAt(index)) {
            case '(':
                token.setTokenType(TokenType.lparen);
                break;
            case ')':
                token.setTokenType(TokenType.rparen);
                break;
            case ',':
                token.setTokenType(TokenType.comma);
                break;
            case ':':
                token.setTokenType(TokenType.colon);
                break;
            case ';':
                token.setTokenType(TokenType.semicolon);
                break;
            case '.':
                token.setTokenType(TokenType.period);
                break;
            case '=':
                token.setTokenType(TokenType.relop);
                break;
            case '<':
                token.setTokenType(TokenType.relop);
                break;
            case '>':
                token.setTokenType(TokenType.relop);
                break;
            case '+':
                token.setTokenType(TokenType.addop);
                break;
            case '-':
                token.setTokenType(TokenType.addop);
                break;
            case '*':
                token.setTokenType(TokenType.mulop);
                break;
            case '/':
                token.setTokenType(TokenType.mulop);
                break;
            default:
                token.setTokenType(TokenType.unknown);
                token.setLexeme(Character.toString(lexeme));
                break;
        }


        // after processing single token, increment the index
        index++;
    }

    /**
     * This function process Word Token, some operator and reserver word token
     */
    public static void processWordToken(){
        int length = 1;
        StringBuilder stringBuilder = new StringBuilder();

        char currentChar = input.charAt(index);

        // parse the token
        while(String.valueOf(currentChar).matches("[a-zA-Z0-9_]")){
            if(length > 17){
                token = new Token(TokenType.unknown, "Error too long id token " + stringBuilder.toString());
                return;
            }
            stringBuilder.append(currentChar);

            index++;
            if(index == input.length()) { // we've reached the end of line
                break;
            }else{
                currentChar = input.charAt(index);
            }
            length++;
        }

        // after successful parsing check which token is it
        String lexeme = stringBuilder.toString().toUpperCase();
        token.setLexeme(lexeme);

        if(lexeme.equals("REM") || lexeme.equals("MOD") || lexeme.equals("AND")){
            token.setTokenType(TokenType.mulop);
        }
        else if(lexeme.equals("OR")){
            token.setTokenType(TokenType.addop);
        } else{
            try{
                TokenType tokenType = TokenType.valueOf(lexeme); // I don't know if this method returns null
                if(tokenType != null)
                    token.setTokenType(tokenType);
            } catch (Exception e){
                token.setTokenType(TokenType.id);
            }
        }
    }

    /**
     * This function process number token
     */
    public static void processNumberToken(){
        StringBuilder stringBuilder = new StringBuilder();

        char currentChar = input.charAt(index);

        token.setTokenType(TokenType.num);

        while(String.valueOf(currentChar).matches("[0-9.?]")){
            stringBuilder.append(currentChar);

            // exit parsing, Error : no number after decimal point
            try {
                if(currentChar == '.' & !String.valueOf(input.charAt(index+1)).matches("[0-9]")){
                    token = new Token(TokenType.unknown, "Error no number after decimal point " + stringBuilder.toString());
                    index++;    // consume the . character
                    return;
                }
            } catch (StringIndexOutOfBoundsException e){
                if(currentChar == '.') {
                    token = new Token(TokenType.unknown, "Error no number after decimal point " + stringBuilder.toString());
                    index++;    // consume the . character
                    return;
                }
            }

            // exit parsing or get the next character
            index++;
            if(index == input.length()) {
                break;
            } else {
                currentChar = input.charAt(index);
            }
        }

        String lexeme = stringBuilder.toString();
        token.setLexeme(lexeme);
        try {
            if (lexeme.contains(".")) {
                token.setValueR(Float.valueOf(lexeme));
            } else {
                token.setValue(Integer.valueOf(lexeme));
            }
        } catch (UnsupportedValue e){
            System.out.println("Internal error : Trying to assign numeric value to unsupported token.");
        }
    }
}
