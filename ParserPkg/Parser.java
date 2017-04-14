package ParserPkg;

import SymbolTablePkg.*;
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

 DeclarativePart    ->	IdentifierList : TypeMark ; DeclarativePart | ε
 IdentifierList	    ->	idt | idt IdentifierList_
 IdentifierList_    ->	,idt IdentifierList_ | ε
 TypeMark		->	integert | realt | chart | const assignop Value
 Value			->	NumericalLiteral
 Procedures		-> 	Prog Procedures | ε
 Args			->	( ArgList ) | ε
 ArgList		-> 	Mode IdentifierList : TypeMark MoreArgs
 MoreArgs		-> 	; ArgList | ε
 Mode			->	in | out | inout | ε

 SeqOfStatments	->	Statement  ; StatTail | ε
 StatTail		-> 	Statement  ; StatTail | ε
 Statement		-> 	AssignStat	| IOStat
 AssignStat		->	idt  :=  Expr
 IOStat			->	ε
 Expr			->	Relation
 Relation		->	SimpleExpr
 SimpleExpr		->	Term MoreTerm
 MoreTerm		->	Addop Term MoreTerm | ε
 Term			->	Factor  MoreFactor
 MoreFactor		->  Mulop Factor MoreFactor| ε
 Factor			->	id |
					num	|
					( Expr )|
					not Factor|
					SignOp Factor
 Addop			->	+ | - | or
 Mulop			-> 	* | / | mod | rem | and
 SignOp		    ->	-

 */

public class Parser {
    private Tokenizer tokenizer;
    private Token currentToken;
    private boolean isParsingSuccessful;
    private SymbolTable _symbolTable;
    private LinkedList<Symbol> identifierList = new LinkedList<>();
    private int identifierOffset = 0;

    public Parser(String fileName) throws IOException {
        tokenizer = new Tokenizer(fileName);
        currentToken = tokenizer.getNextToken();

        // initialize symbol table before parsing
        _symbolTable = new SymbolTable();

        // initialize parsing
        Prog();

        // print the symbol table of global space
//        _symbolTable.printDepth(_symbolTable.CurrentDepth);

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
        identifierOffset = 0; // set it back to zero for the start of new function
        match(currentToken, TokenType.PROCEDURE);
        String functionName = currentToken.getLexeme();
        checkForDuplicateSymbol();
        _symbolTable.insert(functionName, _symbolTable.CurrentDepth).setSymbolType(ESymbolType.function);
        _symbolTable.CurrentDepth++;
        match(currentToken, TokenType.id);

        Args(functionName);
        match(currentToken, TokenType.IS);
        identifierOffset = 0; // set it back to zero for local variable offset
        DeclarativePart(functionName);
        Procedures();
        match(currentToken, TokenType.BEGIN);
        SeqOfStatements();

        match(currentToken, TokenType.END);
        if(!functionName.equalsIgnoreCase(currentToken.getLexeme())){
            System.out.println("Error : Missing statement \"END " + functionName+";\"");
            System.exit(1);
        }
        // match the start id
        match(currentToken, TokenType.id);

        match(currentToken, TokenType.semicolon);

//        _symbolTable.printDepth(_symbolTable.CurrentDepth);
        _symbolTable.deleteDepth(_symbolTable.CurrentDepth);
        _symbolTable.CurrentDepth--;
    }

    // This function implements  DeclarativePart	->	IdentifierList : TypeMark ; DeclarativePart | E
    private void DeclarativePart(String functionName_) {
        if(currentToken.getTokenType() == TokenType.id){ // we do not use "currentToken = tokenizer.getNextToken()" here, since we are doing a look ahead
            IdentifierList();
            match(currentToken, TokenType.colon);
            TypeMark(functionName_, null);
            match(currentToken, TokenType.semicolon);
            DeclarativePart(functionName_);
        }
        // else empty production
    }

    // This function implements  IdentifierList  -> 	idt IdentifierList`
    private void IdentifierList() {
        checkForDuplicateSymbol();
        identifierList.add(_symbolTable.insert(currentToken.getLexeme(), _symbolTable.CurrentDepth));
        match(currentToken, TokenType.id);
        IdentifierList_();
    }

    // This function implements  IdentifierList`	->	,idt IdentifierList` | E
    private void IdentifierList_() {
        if(currentToken.getTokenType() == TokenType.comma){
            currentToken = tokenizer.getNextToken();
            checkForDuplicateSymbol();
            identifierList.add(_symbolTable.insert(currentToken.getLexeme(), _symbolTable.CurrentDepth));
            match(currentToken, TokenType.id);
            IdentifierList_();
        }
        // else empty productoin
    }

    // This function implements  TypeMark	->	integert | realt | chart | const assignop Value
    private void TypeMark(String functionName_, EParameterModeType parameterMode) {
        if(currentToken.getTokenType() == TokenType.INTEGER |
                currentToken.getTokenType() == TokenType.FLOAT |
                currentToken.getTokenType() == TokenType.CHAR |
                currentToken.getTokenType() == TokenType.CONSTANT){

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
                    if(symbol.getSymbolType() == null){
                        symbol.setSymbolType(ESymbolType.variable);
                        symbol.variableAttributes.typeOfVariable = type;
                        symbol.variableAttributes.size = size;
                        symbol.variableAttributes.offset = identifierOffset;
                        identifierOffset += size;
                    }
                }

                currentToken = tokenizer.getNextToken();
            } else if(currentToken.getTokenType() == TokenType.CONSTANT){
                currentToken = tokenizer.getNextToken();
                match(currentToken, TokenType.assignop);
                String numberStr = Value(); // this block does not end with getNextToken because, it happens in Value function

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
                    if(symbol.getSymbolType() == null){
                        symbol.setSymbolType(ESymbolType.constant);
                        symbol.constantAttributes.typeOfConstant = type;

                        if(type == EVariableType.integerType)
                            symbol.constantAttributes.value = Integer.parseInt(numberStr);
                        else
                            symbol.constantAttributes.valueR = Float.parseFloat(numberStr);

                        symbol.constantAttributes.offset = identifierOffset;
                        identifierOffset += size;
                    }
                }
            }

            Symbol funcSymbol = _symbolTable.lookup(functionName_, ESymbolType.function);

            // add information about function parameter
            if(parameterMode != null) {
                funcSymbol.functionAttributes.numberOfParameter += identifierList.size();

                // add type and mode of each parameter
                for (Symbol symbol : identifierList) {
                    if (symbol.constantAttributes == null)
                        funcSymbol.functionAttributes.parameterTypeList.add(symbol.variableAttributes.typeOfVariable);
                    else
                        funcSymbol.functionAttributes.parameterTypeList.add(symbol.constantAttributes.typeOfConstant);

                    if (parameterMode != null) {
                        funcSymbol.functionAttributes.parameterModeList.add(parameterMode);
                    }
                }
            }
            // add information about local variable
            else {
                funcSymbol.functionAttributes.sizeOfLocalVariable = identifierOffset;
            }

            identifierList.clear();
        }
        else {
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

    // This function implements  Procedures  -> 	Prog Procedures | E
    private void Procedures() {
        if(currentToken.getTokenType() == TokenType.PROCEDURE){ // we do not use "currentToken = tokenizer.getNextToken()" here, since we are doing a look ahead
            Prog();
            Procedures();
        }
        // else empty statement
    }

    // This function implements  Args	->	( ArgList ) | E
    private void Args(String functionName_) {
        if(currentToken.getTokenType() == TokenType.lparen) {
            currentToken = tokenizer.getNextToken();
            ArgList(functionName_);
            match(currentToken, TokenType.rparen);
        }
        // no more function parameters
    }

    // This function implements  ArgList	-> 	Mode IdentifierList : TypeMark MoreArgs
    private void ArgList(String functionName_) {
        EParameterModeType parameterMode = Mode();
        IdentifierList();
        match(currentToken, TokenType.colon);
        TypeMark(functionName_, parameterMode);

        MoreArgs(functionName_);
    }

    // This function implements MoreArgs	-> 	; ArgList | E
    private void MoreArgs(String functionName_) {
        if(currentToken.getTokenType() == TokenType.semicolon){
            currentToken = tokenizer.getNextToken();
            ArgList(functionName_);
        }
    }

    // This function implements Mode	->	in | out | inout | E
    private EParameterModeType Mode() {
        String lexeme = currentToken.getLexeme();
        if(lexeme.equalsIgnoreCase("IN") | lexeme.equalsIgnoreCase("OUT") | lexeme.equalsIgnoreCase("INOUT")) {

            EParameterModeType parameterMode;
            if(lexeme.equalsIgnoreCase("IN"))
                parameterMode = EParameterModeType.in;
            else if(lexeme.equalsIgnoreCase("OUT"))
                parameterMode = EParameterModeType.out;
            else
                parameterMode = EParameterModeType.inout;

            currentToken = tokenizer.getNextToken();
            return parameterMode;
        } else {
            return EParameterModeType.in;
        }
        // else empty production
    }

    // Thie grammar checks for id token because we allow only assignment statement and IO statement
    // All assignment statement has to start with an identifier token
    // This function implements  SeqOfStatments	->	Statement  ; StatTail | ε
    private void SeqOfStatements() {
        if(currentToken.getTokenType() == TokenType.id){
            Statement();
            match(currentToken, TokenType.semicolon);
            StatTail();
        }
        // else empty production
    }

    // StatTail		-> 	Statement  ; StatTail | ε
    private void StatTail(){
        if(currentToken.getTokenType() == TokenType.id){
            Statement();
            match(currentToken, TokenType.semicolon);
            StatTail();
        }
        // else empty production
    }

    // Statement		-> 	AssignStat	| IOStat
    private void Statement(){
        if(currentToken.getTokenType() == TokenType.id){
            AssignStat();
        } else {
            IOStat();
        }
    }

    // AssignStat		->	idt  :=  Expr
    private void AssignStat() {
        // check if the variable is declared before use
        Symbol symbol = _symbolTable.lookup(currentToken.getLexeme());
        if(symbol != null && symbol.depth <= _symbolTable.CurrentDepth) {
            match(currentToken, TokenType.id);
        } else {
            System.out.println("Error: Undefined identifier " + currentToken.getLexeme());
            System.exit(1);
        }
        match(currentToken, TokenType.assignop);
        Expr();
    }

    // IOStat			->	ε
    private void IOStat() {
        return;
    }

    // Expr			->	Relation
    private void Expr() {
        Relation();
    }

    // Relation		->	SimpleExpr
    private void Relation() {
        SimpleExpr();
    }

    // SimpleExpr		->	Term MoreTerm
    private void SimpleExpr() {
        Term();
        MoreTerm();
    }

    // MoreTerm		->	Addop Term MoreTerm | ε
    private void MoreTerm() {
        if(currentToken.getTokenType() == TokenType.addop){
            match(currentToken, TokenType.addop);
            Term();
            MoreTerm();
        }
    }

    // Term			->	Factor  MoreFactor
    private void Term() {
        Factor();
        MoreFactor();
    }

    // MoreFactor		->  Mulop Factor MoreFactor| ε
    private void MoreFactor() {
        if(currentToken.getTokenType() == TokenType.mulop){
            match(currentToken, TokenType.mulop);
            Factor();
            MoreFactor();
        }
    }

    // Factor			->	id | num | ( Expr ) | not Factor | SignOp Factor
    private void Factor() {
        if(currentToken.getTokenType() == TokenType.id){
            Symbol symbol = _symbolTable.lookup(currentToken.getLexeme());
            if(symbol != null && symbol.depth <= _symbolTable.CurrentDepth) {
                match(currentToken, TokenType.id);
            } else {
                System.out.println("Error: Undefined identifier " + currentToken.getLexeme());
                System.exit(1);
            }
            // todo replace the value of the variable with the value if the variable is constant
        } else if(currentToken.getTokenType() == TokenType.num){
            match(currentToken, TokenType.num);
        } else if(currentToken.getTokenType() == TokenType.lparen){
            match(currentToken, TokenType.lparen);
            Expr();
            match(currentToken, TokenType.rparen);
        } else
            SignOp();
    }

    // SignOp		    ->	-
    private void SignOp() {
        if(currentToken.getLexeme() == "-"){
            currentToken = tokenizer.getNextToken();
            Factor();
        }
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

    private void checkForDuplicateSymbol() {
        Symbol symbol = _symbolTable.lookup(currentToken.getLexeme());
        if(symbol != null && symbol.depth == _symbolTable.CurrentDepth){
            System.out.println("Error: Duplicate symbol: '" +currentToken.getLexeme() + "'");
            System.exit(1);
        }
    }

    public boolean isParsingSuccessful(){
        return isParsingSuccessful;
    }
}

