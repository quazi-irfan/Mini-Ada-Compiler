/**
 * Token classs represents an instance of a token
 */
public class Token {
    private TokenType tokenType;
    private String lexeme;
    private int value;
    private float valueR;
    private String literal;

    // Constructor
    public Token(TokenType tokenType, String lexeme) {
        this.tokenType = tokenType;
        this.lexeme = lexeme;
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

    public int getValue() throws UnsupportedValue{
        if(tokenType == TokenType.num)
            return value;
        else
            throw new UnsupportedValue();
    }

    public void setValue(int value) throws UnsupportedValue {
        if(tokenType == TokenType.num)
            this.value = value;
        else
            throw new UnsupportedValue();
    }

    public float getValueR() throws UnsupportedValue {
            return valueR;
    }

    public void setValueR(float valueR) throws UnsupportedValue {
            this.valueR = valueR;
    }

    public String getLiteral() throws UnsupportedValue {
        if(tokenType == TokenType.string)
            return literal;
        else
            throw new UnsupportedValue();
    }

    public void setLiteral(String literal) throws UnsupportedValue {
        if(tokenType == TokenType.string)
            this.literal = literal;
        else
            throw new UnsupportedValue();
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
                formattedString = String.format("%-20s %-25s %-20s", tokenTypeT, lexeme, valueR);
                return formattedString;
            } else {
                formattedString = String.format("%-20s %-25s %-20s", tokenTypeT, lexeme, value);
                return formattedString;
            }
        } else if(tokenType == TokenType.string){
            formattedString = String.format("%-20s %-25s %-20s", tokenTypeT, lexeme, literal );
            return formattedString;
        }
        else{
            formattedString = String.format("%-20s %-25s", tokenTypeT, lexeme );
            return formattedString;
        }
    }
}
