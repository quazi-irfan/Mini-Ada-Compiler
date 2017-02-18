package ScannerPkg;

/**
 * ScannerPkg.Token classs represents an instance of a token
 */
public class Token {
    private TokenType tokenType;
    private String lexeme;
    private int value;
    private float valueR;
    private String literal;
    private int lineNumber;

    // Constructor
    public Token(TokenType tokenType, String lexeme, int lineNumber) {
        this.tokenType = tokenType;
        this.lexeme = lexeme;
        this.lineNumber = lineNumber;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public String getLexeme() {
        return lexeme;
    }

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public float getValueR() {
            return valueR;
    }

    public void setValueR(float valueR)  {
            this.valueR = valueR;
    }

    public String getLiteral(){
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public void setLineNumber(int lineNumber){
        this.lineNumber = lineNumber;
    }

    public int getLineNumber(){
        return this.lineNumber;
    }

    public void setAttribute(Number attribute){
        if (lexeme.contains(".")) {
            valueR = Float.valueOf(lexeme);
        } else {
            value = Integer.valueOf(lexeme);
        }
    }

    public void setAttribute(String attribute){
        literal = attribute;
    }

    public Object getAttribute(){
        if(tokenType == TokenType.num){
            if(lexeme.contains(".")){
                return valueR;
            } else {
                return value;
            }
        } else {
            return literal;
        }
    }

    /**
     * Based on the type of token we have different toString implementation
     * @return
     */
    @Override
    public String toString(){
        String formattedString;
        String tokenTypeT = tokenType.name().concat("t");

        if(tokenType == TokenType.num) {
            if (lexeme.contains(".")) {
                formattedString = String.format("%-20s %-20s %-25s %-20s", lineNumber, tokenTypeT, lexeme, valueR);
                return formattedString;
            } else {
                formattedString = String.format("%-20s %-20s %-25s %-20s", lineNumber, tokenTypeT, lexeme, value);
                return formattedString;
            }
        } else if(tokenType == TokenType.string){
            formattedString = String.format("%-20s %-20s %-25s %-20s", lineNumber, tokenTypeT, lexeme, literal );
            return formattedString;
        }
        else{
            formattedString = String.format("%-20s %-20s %-25s", lineNumber, tokenTypeT, lexeme );
            return formattedString;
        }
    }
}
