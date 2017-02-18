import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Scanner Class
 */
public class Scanner {
    private int lineNumber = 1; // even in an empty time the eof token will be at line 1
    private static int index = 0;
    private static String input = null;
    private static Token token = new Token(TokenType.unknown, null);
    private static BufferedReader reader = null;
    private List<Token> tokenList = new ArrayList<>();

    public Scanner(String fileName)throws IOException{
        // parse the source input file and enlist all available tokens in the TokenList
        reader = new BufferedReader(new FileReader(fileName));
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
            lineNumber++;
        }

        // we readLine returns null, we've found end of file token
        token = new Token(TokenType.eof, "", lineNumber);
        tokenList.add(token);
    }

    /**
     * This function process Word Token, some operator and reserver word token
     */
    public void processWordToken(){
        StringBuilder stringBuilder = new StringBuilder();
        char currentChar = input.charAt(index);

        int length = 1; // we are in this function here because the first token is valid start of word token

        // valid start of word token found, keep parsing until we find a character that can't be part of word token
        while(String.valueOf(currentChar).matches("[a-zA-Z0-9_]")){
            // if the length of the word token is greater then 17, we've encountered an error, and we exit the function
            if(length > 17){
                token = new Token(TokenType.unknown, "Error too long id token " + stringBuilder.toString(), lineNumber);
                return;
            }

            // if the length < 17, and we have a valid character we add it as a part of word token
            stringBuilder.append(currentChar);

            // now, move the index to next character.
            // if the index is same as the line length, break because we don't any more character to read
            // else read the next character
            index++;
            if(index == input.length()) {
                break;
            }else{
                currentChar = input.charAt(index);
            }

            // increment the length of the word token
            length++;
        }

        // valid word token found, now populate a token object
        String lexeme = stringBuilder.toString().toUpperCase();
        token.setLexeme(lexeme);
        token.setLineNumber(lineNumber);

        // if the word token is a REM, MOD or AND, it's a multiplication operator token
        if(lexeme.equals("REM") || lexeme.equals("MOD") || lexeme.equals("AND")){
            token.setTokenType(TokenType.mulop);
        }
        // if the word token is OR, it's a addition operator token
        else if(lexeme.equals("OR")){
            token.setTokenType(TokenType.addop);
        }
        // else the word token is an reserved word or an identifier token
        else{
            // check if it is any of the following reserve words
            // BEGIN, MODULE, CONSTANT, PROCEDURE, IS, IF, THEN, ELSE,
            // ELSIF, WHILE, LOOP, FLOAT, INTEGER, CHAR, GET, PUT, END,
            try{
                TokenType tokenType = TokenType.valueOf(lexeme);
                if(tokenType != null)   // todo : I don't know if valueOf returns null
                    token.setTokenType(tokenType);
            }
            // else the word token is an identifier token
            catch (Exception e){
                token.setTokenType(TokenType.id);
            }
        }
    }

    /**
     * This function process number token
     */
    public void processNumberToken(){
        StringBuilder stringBuilder = new StringBuilder();
        char currentChar = input.charAt(index);

        while(String.valueOf(currentChar).matches("[0-9.?]")){ // todo : probably .? part of the regex is redundent
            stringBuilder.append(currentChar);

            // check if current character is . we need another number following it, or it's an error in number token
            try {
                if(currentChar == '.' && !String.valueOf(input.charAt(index+1)).matches("[0-9]")){
                    token = new Token(TokenType.unknown, "Error no number after decimal point " + stringBuilder.toString(), lineNumber);
                    index++;    // consume the . character
                    return;
                }
            }
            // if exception is thrown, there is no more character in that line after . character, so it's a malformed number token
            catch (StringIndexOutOfBoundsException e){
                token = new Token(TokenType.unknown, "Error no number after decimal point " + stringBuilder.toString(), lineNumber);
                index++;    // consume the . character
                return;
            }

            // now, move the index to next character.
            // if the index is same as the line length, break because we don't any more character to read
            // else read the next character
            index++;
            if(index == input.length()) {
                break;
            } else {
                currentChar = input.charAt(index);
            }
        }

        // valid number token found, now populate a token object
        String lexeme = stringBuilder.toString();
        token.setTokenType(TokenType.num);
        token.setLexeme(lexeme);
        token.setLineNumber(lineNumber);

        // todo : replace setValue setValueR with setAttribute
        if (lexeme.contains(".")) {
            token.setValueR(Float.valueOf(lexeme));
        } else {
            token.setValue(Integer.valueOf(lexeme));
        }
    }

    /**
     * This function process string literal
     */
    private void processStringLiteral() {
        StringBuilder stringBuilder = new StringBuilder();
        token.setTokenType(TokenType.string);

        // keep parsing until we get to the ending " character
        // it's an error if we reach the end of the line while looking for " character
        do {
            stringBuilder.append(Character.valueOf(input.charAt(index)));
            index++;

            // We have reached the end of file
            if(index >= input.length()){
                token = new Token(TokenType.unknown, "Error missing string literal termination character " + stringBuilder.toString(), lineNumber);
                return;
            }
        }while(input.charAt(index) != '"');

        // valid string token found, now populate a token object

        // add the ending " character to the string lexeme, and increment the index
        stringBuilder.append(Character.valueOf(input.charAt(index)));
        index++;

        String lexeme = stringBuilder.toString();
        token.setLexeme(lexeme);
        token.setLineNumber(lineNumber);

        String literal = lexeme.substring(1, lexeme.length()-1);
        token.setLiteral(literal);
    }

    /**
     * This function process double token
     */
    private void processDoubleToken(){
        StringBuilder stringBuilder = new StringBuilder();

        // the first character is one from the list [/<>:-]
        stringBuilder.append(input.charAt(index));

        // try to get the next character
        try {
            // process /= <= >= :=
            if (input.charAt(index+1) == '=') {
                stringBuilder.append(input.charAt(index+1));
                index = index + 2;

                String lexeme = stringBuilder.toString();
                token.setLexeme(lexeme);
                token.setLineNumber(lineNumber);

                // /= <= >= are relational operator
                if(lexeme.equals(":="))
                    token.setTokenType(TokenType.assignop);
                // := is assignment operator
                else
                    token.setTokenType(TokenType.relop);
            }
            // process comment --
            else if (input.charAt(index+1) == '-') {
                // we have found a token, so we will ignore the remaining input
                // to do that we move the index to the end of the line
                index = input.length();

                // we also set the token to null, so our parser knows it's a comment, and avoid printing the content of the token
                // in all other occasions our scanner will try to print the content of the token
                token = null;
                return;
            }
            // else process single token
            else {
                processSingleToken();
            }
        }
        // we have reached the end of line, so there is no character. So we have a single token
        catch (StringIndexOutOfBoundsException e) {
            processSingleToken();
        }
    }

    /**
     * This function process single token
     */
    private void processSingleToken() {
        char lexeme =  input.charAt(index);
        token.setLexeme(Character.toString(lexeme));
        token.setLineNumber(lineNumber);

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
                // unknown single character token
                token = new Token(TokenType.unknown, Character.toString(lexeme), lineNumber);
                break;
        }

        // after processing single token, increment the index
        index++;
    }

    public List<Token> getTokenList(){
        return tokenList;
    }
}
