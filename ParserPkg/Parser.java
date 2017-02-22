package ParserPkg;

import TokenizerPkg.*;

import java.io.IOException;

public class Parser {
    private Tokenizer tokenizer;
    private Token currentToken;

    public Parser(String fileName) throws IOException {
        tokenizer = new Tokenizer(fileName);
        currentToken = tokenizer.getNextToken();

        // initialize parsing
        Prog();

        if(currentToken.getTokenType() != TokenType.eof) {
            System.out.println("At line number " + currentToken.getLineNumber() + " unused token(" + currentToken.getTokenType() + ", " + currentToken.getLexeme() + ") found. Expecting End of File token.");
            System.exit(1);
        }
        else {
            System.out.println("Parsing successful.");
        }
    }

    //Prog	->	procedure idt Args is DeclarativePart Procedures begin SeqOfStatements end idt;
    private void Prog(){
        match(currentToken, TokenType.PROCEDURE);
        match(currentToken, TokenType.id);
        Args();
        match(currentToken, TokenType.IS);
        DeclarativePart();
        Procedures();
        match(currentToken, TokenType.BEGIN);
        SeqOfStatements();
        match(currentToken, TokenType.END);
        match(currentToken, TokenType.id);
        match(currentToken, TokenType.semicolon);
    }

    // SeqOfStatments	->	E
    private void SeqOfStatements() {
        return;
    }

    // Procedures  -> 	Prog Procedures | E
    private void Procedures() {
        if(currentToken.getTokenType() == TokenType.PROCEDURE){ // we do not use "currentToken = tokenizer.getNextToken()" here, since we are doing a look ahead
            Prog();
            Procedures();
        }
        // else empty statement
    }

    // DeclarativePart	->	IdentifierList : TypeMark ; DeclarativePart | E
    private void DeclarativePart() {
        if(currentToken.getTokenType() == TokenType.id){ // we do not use "currentToken = tokenizer.getNextToken()" here, since we are doing a look ahead
            IdentifierList();
            match(currentToken, TokenType.colon);
            TypeMark();
            match(currentToken, TokenType.semicolon);
            DeclarativePart();
        }
        // else empty production
    }

    // Args	->	( ArgList ) | E
    private void Args() {
        if(optionalMatch(currentToken, TokenType.lparen)) {
            currentToken = tokenizer.getNextToken();
            ArgList();
            match(currentToken, TokenType.rparen);
        }
        // else empty production
    }

    // ArgList	-> 	Mode IdentifierList : TypeMark MoreArgs
    private void ArgList() {
        Mode();
        IdentifierList();
        match(currentToken, TokenType.colon);
        TypeMark();
        MoreArgs();
    }

    //MoreArgs	-> 	; ArgList | E
    private void MoreArgs() {
        if(currentToken.getTokenType() == TokenType.semicolon){
            currentToken = tokenizer.getNextToken();
            ArgList();
        }
        // else empty production
    }

    // TypeMark	->	integert | realt | chart | const assignop Value
    private void TypeMark() {
        if(currentToken.getTokenType() == TokenType.INTEGER |
                currentToken.getTokenType() == TokenType.FLOAT |
                currentToken.getTokenType() == TokenType.CHAR){
            currentToken = tokenizer.getNextToken();
        } else if(currentToken.getTokenType() == TokenType.CONSTANT){
            currentToken = tokenizer.getNextToken();
            match(currentToken, TokenType.assignop);
            Value();
        } else {
            System.out.println("At line number " + currentToken.getLineNumber() + ", expecting integer/float/char/const , but found " + currentToken.getTokenType() + " token with lexeme " + currentToken.getLexeme());
            System.exit(1);
        }
    }

    // Value ->	NumericalLiteral
    private void Value() {
        match(currentToken,TokenType.num);
    }

    //Mode	->	in | out | inout | E
    private void Mode() {
        // todo ask if it's correct
        if(currentToken.getLexeme().equals("IN") | currentToken.getLexeme().equals("OUT") | currentToken.getLexeme().equals("INOUT")) {
            currentToken = tokenizer.getNextToken();
            return;
        }
        // else empty production
    }

    // IdentifierList  -> 	idt IdentifierList`
    private void IdentifierList() {
        match(currentToken, TokenType.id);
        IdentifierList_();
    }

    // IdentifierList`	->	,idt IdentifierList` | E
    private void IdentifierList_() {
        if(optionalMatch(currentToken, TokenType.comma)){
            currentToken = tokenizer.getNextToken();
            match(currentToken, TokenType.id);
            IdentifierList_();
        }
        // else empty productoin
    }

    /**
     * Matches if the currentToken is same as the desired token type.
     * If we do not get the desired token it is a fatal error, and we print the error and exit the program.
     * @param localCurrentToken Current token
     * @param desiredToken The token type we are looking for
     */

    private void match(Token localCurrentToken, TokenType desiredToken) {
        if(localCurrentToken.getTokenType() != desiredToken){
            System.out.println("At line number " + currentToken.getLineNumber() + ", expecting " + desiredToken + " token, but found " + currentToken.getTokenType() + " token with lexeme " + currentToken.getLexeme());
            System.exit(1);
        } else {
            currentToken = tokenizer.getNextToken();
        }
    }

    /**
     * Returns true or false if the localCurrentToken matchs with the desired token we are looking for.
     * Use this method, when the grammar rule has alternate path to take.
     * If we don't get the token we are looking for, we might just need to use another grammer.
     * @param localCurrentToken
     * @param desiredToken The token we are looking for.
     * @return
     */
    private boolean optionalMatch(Token localCurrentToken, TokenType desiredToken) {
        return (localCurrentToken.getTokenType() == desiredToken);
    }
}

