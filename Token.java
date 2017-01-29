public class Token {
    private TokenType tokenType;
    private String lexeme;
    private int value;
    private float valueR;
    private String literal;

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
        if(tokenType == TokenType.inum)
            return value;
        else
            throw new UnsupportedValue();
    }

    public void setValue(int value) throws UnsupportedValue {
        if(tokenType == TokenType.inum)
            this.value = value;
        else
            throw new UnsupportedValue();
    }

    public float getValueR() throws UnsupportedValue {
        if(tokenType == TokenType.rnum)
            return valueR;
        else
            throw new UnsupportedValue();
    }

    public void setValueR(float valueR) throws UnsupportedValue {
        if(tokenType == TokenType.rnum)
            this.valueR = valueR;
        else
            throw new UnsupportedValue();
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

    @Override
    public String toString(){
        if(tokenType == TokenType.inum)
            return "TokenType " + tokenType + " Lexeme " + lexeme + " int value " + value;
        else if(tokenType == TokenType.rnum)
            return "TokenType " + tokenType + " Lexeme " + lexeme + " real value " + valueR;
        else if(tokenType == TokenType.string)
            return "TokenType " + tokenType + " Lexeme " + lexeme + " string value " + literal;
        else if(tokenType == TokenType.unknown)
            return lexeme;
        else
            return "TokenType " + tokenType + " Lexeme " + lexeme;
    }
}
