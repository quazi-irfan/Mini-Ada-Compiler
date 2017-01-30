import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Lexer {
    private String fileName = null;
    public static int index = 0;
    public static String input = null;
    public static Token token = new Token(TokenType.unknown, null);
    public static BufferedReader reader = null;

    public Lexer(String fileName)throws IOException{
        this.fileName = fileName;

        reader = new BufferedReader(new FileReader(this.fileName));
        input = reader.readLine();
        if(input != null)
            input = removeComments(input);
        index = 0;
    }

    Token getNextToken() throws  IOException{
        // end of file
        if(input == null)
            return null;

        // if we've reached the end of the line then get a new line
        if(index > input.length() - 1) {
            input = reader.readLine();
            if(input == null)
                return null;
            input = removeComments(input);
            index = 0;
        }

        // consume empty characters until we get to the first readable chacters
        while (index < input.length() && input.charAt(index) <= 32) {
            index++;
            continue;
        }

        // start looking for token with the first non-white space character
        token = new Token(TokenType.unknown, null);

        if (String.valueOf(input.charAt(index)).matches("[a-zA-Z]")) {
            processWordToken();
        } else if (String.valueOf(input.charAt(index)).matches("[0-9]")) {
            processNumberToken();
        } else if (String.valueOf(input.charAt(index)).matches("[/<>:]")) {
            processDoubleToken();
        } else if (input.charAt(index) == '"') {
            processStringLiteral();
        } else {
            processSingleToken();
        }

        return token;
    }

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

    private static void processStringLiteral() {
        token.setTokenType(TokenType.string);
        StringBuilder stringBuilder = new StringBuilder();

        do {
            stringBuilder.append(Character.valueOf(input.charAt(index)));
            index++;

            // reaches the end
            if(index >= input.length()){
                token = new Token(TokenType.unknown, "Error parsing string literal  " + stringBuilder.toString());
                break;
            }
        }while(input.charAt(index) != '"');


        // we have found the starting and ending " character of the string literal
        if(token.getTokenType() == TokenType.string){
            stringBuilder.append(Character.valueOf(input.charAt(index)));
            index++;
            token.setTokenType(TokenType.string);

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
            token.setLexeme("Error parsing string literal " + stringBuilder.toString());
        }

    }

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

            } else {
                processSingleToken();
            }
        } catch (StringIndexOutOfBoundsException e) {
            processSingleToken();
        }

    }

    private static void processSingleToken() {
        char lexeme =  input.charAt(index);
        token.setLexeme(Character.toString(lexeme));

        switch(input.charAt(index)) {
            case '(':
                token.setTokenType(TokenType.lparent);
                break;
            case ')':
                token.setTokenType(TokenType.rparent);
                break;
            case ',':
                token.setTokenType(TokenType.commat);
                break;
            case ':':
                token.setTokenType(TokenType.colont);
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

    public static void processWordToken(){
        StringBuilder stringBuilder = new StringBuilder();

        char currentChar = input.charAt(index);

        // parse the token
        while(String.valueOf(currentChar).matches("[a-zA-Z0-9_]")){
            stringBuilder.append(currentChar);

            index++;
            if(index == input.length()) {
                break;
            }else{
                currentChar = input.charAt(index);
            }
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

    public static void processNumberToken(){
        StringBuilder stringBuilder = new StringBuilder();

        char currentChar = input.charAt(index);

        token.setTokenType(TokenType.inum);

        while(String.valueOf(currentChar).matches("[0-9.?]")){
            stringBuilder.append(currentChar);

            if(currentChar == '.')
                token.setTokenType(TokenType.rnum);

            // exit parsing, Error : no number after decimal point
            try {
                if(currentChar == '.' & !String.valueOf(input.charAt(index+1)).matches("[0-9]")){
                    token = new Token(TokenType.unknown, "Error parsing number " + stringBuilder.toString());
                    index++;    // consume the . character
                    return;
                }
            } catch (StringIndexOutOfBoundsException e){
                if(currentChar == '.') {
                    token = new Token(TokenType.unknown, "Error parsing number " + stringBuilder.toString());
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
            if (token.getTokenType() == TokenType.inum) {
                token.setValue(Integer.valueOf(lexeme));
            } else if (token.getTokenType() == TokenType.rnum) {
                token.setValueR(Float.valueOf(lexeme));
            }
        } catch (UnsupportedValue e){
            System.out.println("Internal error : Trying to assign numeric value to unsupported token.");
        }
    }
}
