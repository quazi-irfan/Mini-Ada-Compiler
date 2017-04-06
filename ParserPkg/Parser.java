package ParserPkg;

import SymbolTablePkg.ESymbolType;
import SymbolTablePkg.EVariableType;
import SymbolTablePkg.Symbol;
import SymbolTablePkg.SymbolTable;
import TokenizerPkg.*;

import java.io.IOException;
import java.util.LinkedList;

/* Our grammar
 Prog			->	procedure idt Args is
                    DeclarativePart
                    Procedures
                    begin
                    SeqOfStatements
                    end idt;

 DeclarativePart	->	IdentifierList : TypeMark ; DeclarativePart | ε

 IdentifierList		->	idt |
                        IdentifierList , idt

 TypeMark		->	integert | realt | chart | const assignop Value

 Value			->	NumericalLiteral

 Procedures		-> 	Prog Procedures | ε

 Args			->	( ArgList ) | ε

 ArgList		-> 	Mode IdentifierList : TypeMark MoreArgs

 MoreArgs		-> 	; ArgList | ε

 Mode			->	in | out | inout | ε

 SeqOfStatments	->	ε

 */
//          w:constant := 5;
//        z:integer;
//        a,b,c,d:float;
public class Parser {
    private Tokenizer tokenizer;
    private Token currentToken;
    private boolean isParsingSuccessful;
    private SymbolTable _symbolTable;
    private LinkedList<Symbol> identifierList = new LinkedList<>();
    private int localVariableOffset = 2;

    public Parser(String fileName) throws IOException {
        tokenizer = new Tokenizer(fileName);
        currentToken = tokenizer.getNextToken();

        // initialize symbol table before parsing
        _symbolTable = new SymbolTable();

        // initialize parsing
        Prog();

        if(currentToken.getTokenType() != TokenType.eof) {
            System.out.println("At line number " + currentToken.getLineNumber() + " unused token(" + currentToken.getTokenType() + ", " + currentToken.getLexeme() + ") found. Expecting End of File token.");
            System.exit(1);
        }
        else {
            isParsingSuccessful = true;
        }
    }

    // This function implements Prog	->	procedure idt Args is DeclarativePart Procedures begin SeqOfStatements end idt;
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

    // This function implements  SeqOfStatments	->	E
    private void SeqOfStatements() {
        return;
    }

    // This function implements  Procedures  -> 	Prog Procedures | E
    private void Procedures() {
        if(currentToken.getTokenType() == TokenType.PROCEDURE){ // we do not use "currentToken = tokenizer.getNextToken()" here, since we are doing a look ahead
            Prog();
            Procedures();
        }
        // else empty statement
    }

    // This function implements  DeclarativePart	->	IdentifierList : TypeMark ; DeclarativePart | E
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

    // This function implements  Args	->	( ArgList ) | E
    private void Args() {
        if(currentToken.getTokenType() == TokenType.lparen) {
            currentToken = tokenizer.getNextToken();
            ArgList();
            match(currentToken, TokenType.rparen);
        }
        // else empty production
    }

    // This function implements  ArgList	-> 	Mode IdentifierList : TypeMark MoreArgs
    private void ArgList() {
        Mode();
        IdentifierList();
        match(currentToken, TokenType.colon);
        TypeMark();
        MoreArgs();
    }

    // This function implements MoreArgs	-> 	; ArgList | E
    private void MoreArgs() {
        if(currentToken.getTokenType() == TokenType.semicolon){
            currentToken = tokenizer.getNextToken();
            ArgList();
        }
        // else empty production
    }

    // This function implements  TypeMark	->	integert | realt | chart | const assignop Value
    private void TypeMark() {
        if(currentToken.getTokenType() == TokenType.INTEGER |
                currentToken.getTokenType() == TokenType.FLOAT |
                currentToken.getTokenType() == TokenType.CHAR){

            int size;
            EVariableType type;
            if(currentToken.getTokenType() == TokenType.INTEGER) {
                type = EVariableType.integerType;
                size = 2;
            } else if(currentToken.getTokenType() == TokenType.FLOAT) {
                type = EVariableType.floatType;
                size = 4;
            }else {
                type = EVariableType.characterType;
                size = 1;
            }

            for(Symbol symbol : identifierList){
                symbol.setSymbolType(ESymbolType.variable);
                symbol.variableAttributes.typeOfVariable = type;
                symbol.variableAttributes.size = size;
                symbol.variableAttributes.offset = localVariableOffset;
                localVariableOffset += size;
            }

            identifierList.clear();
            currentToken = tokenizer.getNextToken();
        } else if(currentToken.getTokenType() == TokenType.CONSTANT){
            currentToken = tokenizer.getNextToken();
            match(currentToken, TokenType.assignop);
            String numberStr = Value();

            int size;
            EVariableType type;
            if(numberStr.indexOf('.') == -1){
                type = EVariableType.integerType;
                size = 2;
            } else {
                type = EVariableType.floatType;
                size = 4;
            }

            for(Symbol symbol : identifierList){
                symbol.setSymbolType(ESymbolType.constant);
                symbol.constantAttributes.typeOfConstant = type;
                if(type == EVariableType.integerType)
                    symbol.constantAttributes.value = Integer.parseInt(numberStr);
                else
                    symbol.constantAttributes.valueR = Float.parseFloat(numberStr);
                symbol.constantAttributes.offset = localVariableOffset;
                localVariableOffset += size;
            }
            identifierList.clear();

        } else {
            System.out.println("At line number " + currentToken.getLineNumber() + ", expecting integer/float/char/const , but found " + currentToken.getTokenType() + " token with lexeme " + currentToken.getLexeme());
            System.exit(1);
        }
    }

    // This function implements  Value ->	NumericalLiteral
    private String Value() {
        String value = currentToken.getLexeme();
        match(currentToken,TokenType.num);
        return value;
    }

    // This function implements Mode	->	in | out | inout | E
    private void Mode() {
        // todo ask if it's correct
        if(currentToken.getLexeme().equals("IN") | currentToken.getLexeme().equals("OUT") | currentToken.getLexeme().equals("INOUT")) {
            currentToken = tokenizer.getNextToken();
            return;
        }
        // else empty production
    }

    // This function implements  IdentifierList  -> 	idt IdentifierList`
    private void IdentifierList() {
        identifierList.add(_symbolTable.insert(currentToken.getLexeme(), _symbolTable.CurrentDepth));
        match(currentToken, TokenType.id);
        IdentifierList_();
    }

    // This function implements  IdentifierList`	->	,idt IdentifierList` | E
    private void IdentifierList_() {
        if(currentToken.getTokenType() == TokenType.comma){
            currentToken = tokenizer.getNextToken();
            identifierList.add(_symbolTable.insert(currentToken.getLexeme(), _symbolTable.CurrentDepth));
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

    public boolean isParsingSuccessful(){
        return isParsingSuccessful;
    }
}

