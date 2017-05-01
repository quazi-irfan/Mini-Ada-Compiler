package ParserPkg;

import SymbolTablePkg.*;
import TokenizerPkg.*;

import java.io.IOException;
import java.io.PrintWriter;
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
 AssignStat		->	idt  :=  Expr | ProcCall
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

 ProcCall		->	idt ( Params )
 Params			->	idt ParamsTail | num ParamsTail | ε
 ParamsTail		->	, idt ParamsTail | , num ParamsTail | ε

 */

public class Parser {
    private Tokenizer tokenizer;
    private Token currentToken;
    private boolean isParsingSuccessful;
    private SymbolTable _symbolTable;
    private LinkedList<Symbol> identifierList = new LinkedList<>();
    private int _identifierListOffset = 0;
    private int _identifierOffset = 2;
    private int _tempVariableID = 0;
    private int _tempStringID = 0;
    private static int _currentIndexOfFunctionParameter = 0;
    private PrintWriter tacWriter = null;

    public Parser(String fileName) throws IOException {
        String tacFileName = fileName.substring(0, fileName.length()-4).concat(".tac");
        tacWriter = new PrintWriter(tacFileName);
        System.out.println("Writing output to " + tacFileName);

        // initialize symbol table before parsing
        _symbolTable = new SymbolTable();

        tokenizer = new Tokenizer(fileName);

        // Add all string tokens to global space
        for(Token token : tokenizer.getTokenList()){
            if(token.getTokenType() == TokenType.string){
                Symbol stringSymbol = _symbolTable.insert(tempString(), 1);
                stringSymbol.setSymbolType(ESymbolType.string);
                stringSymbol.stringAttributes.attribute = ((String)token.getAttribute()).concat(",\"$\"");
            }
        }

        // initialize CurrentToken variable
        currentToken = tokenizer.getNextToken();

        // initialize parsing
        String outerFunction  = Prog();
        System.out.println(formattedString(new String[]{"START", "PROC" , outerFunction}));
        tacWriter.println(formattedString(new String[]{"START", "PROC" , outerFunction}));

        // print the symbol table of global space
//        _symbolTable.printDepth(_symbolTable.CurrentDepth);

        if(currentToken.getTokenType() != TokenType.eof) {
            System.out.println("Error: At line number " + currentToken.getLineNumber() + " unused token(" + currentToken.getTokenType() + ", " + currentToken.getLexeme() + ") found. Expecting End of File token.");
            System.exit(1);
        }
        else {
            isParsingSuccessful = true;
        }

        tacWriter.close();
    }

    // This function implements Prog	->	procedure idt Args is DeclarativePart Procedures begin SeqOfStatements end idt;
    private String Prog(){
        _identifierOffset = 4; // set it back to 4 for the start of new function
        match(currentToken, TokenType.PROCEDURE);

        // check if procedure name is already been used
        String functionName = currentToken.getLexeme();
        checkForDuplicateEntry();
        _symbolTable.insert(functionName, _symbolTable.CurrentDepth).setSymbolType(ESymbolType.function);

        // entering into another procedure scope
        _symbolTable.CurrentDepth++;
        match(currentToken, TokenType.id);

        Args(functionName);
        match(currentToken, TokenType.IS);
        _identifierOffset = 2; // set it back to 2 for local variable offset
        DeclarativePart(functionName);
        Procedures();
        match(currentToken, TokenType.BEGIN);
        System.out.println(formattedString(new String[]{"PROC", functionName}));
        tacWriter.println(formattedString(new String[]{"PROC", functionName}));
        SeqOfStatements();
        System.out.println(formattedString(new String[]{"ENDP" , functionName}));
        tacWriter.println(formattedString(new String[]{"ENDP" , functionName}));

        match(currentToken, TokenType.END);
        if(!functionName.equalsIgnoreCase(currentToken.getLexeme())){
            System.out.println("Error: Missing statement \"END " + functionName+";\"");
            System.exit(1);
        }

        // match the start id
        match(currentToken, TokenType.id);

        match(currentToken, TokenType.semicolon);

//        _symbolTable.printDepth(_symbolTable.CurrentDepth);
        _symbolTable.deleteDepth(_symbolTable.CurrentDepth);
        _symbolTable.CurrentDepth--;

        return functionName;
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
        checkForDuplicateEntry();

        // add the lexeme and it's depth of an identifiers to a temporary data structure (identifierList)
        identifierList.add(_symbolTable.insert(currentToken.getLexeme(), _symbolTable.CurrentDepth));
        match(currentToken, TokenType.id);

        IdentifierList_();
    }

    // This function implements  IdentifierList`	->	,idt IdentifierList` | E
    private void IdentifierList_() {
        if(currentToken.getTokenType() == TokenType.comma){
            currentToken = tokenizer.getNextToken();
            checkForDuplicateEntry();

            // add remaining the lexeme and it's depth of the identifiers to a temporary data structure (identifierList)
            identifierList.add(_symbolTable.insert(currentToken.getLexeme(), _symbolTable.CurrentDepth));
            match(currentToken, TokenType.id);

            IdentifierList_();
        }
        // else there is no more identifiers
    }

    // This function implements  TypeMark	->	integert | realt | chart | const assignop Value
    private void TypeMark(String functionName_, EParameterModeType parameterMode_) {
        if(currentToken.getTokenType() == TokenType.INTEGER |
                currentToken.getTokenType() == TokenType.FLOAT |
                currentToken.getTokenType() == TokenType.CHAR |
                currentToken.getTokenType() == TokenType.CONSTANT){

            // if TypeMark is integert, realt and chart,
            // then add respective attributes to the variable identifiers in the temporary data structure(identifierList)
            if(currentToken.getTokenType() == TokenType.INTEGER |
                    currentToken.getTokenType() == TokenType.FLOAT |
                    currentToken.getTokenType() == TokenType.CHAR){

                int variableSize;
                EVariableType variableType;
                if(currentToken.getTokenType() == TokenType.INTEGER) {
                    variableType = EVariableType.integerType;
                    variableSize = 2;
                } else if(currentToken.getTokenType() == TokenType.FLOAT) {
                    variableType = EVariableType.floatType;
                    variableSize = 4;
                }else {
                    variableType = EVariableType.characterType;
                    variableSize = 1;
                }

                // go through all the constant identifiers and set their attributes
                // such as, symbol type(variable or constant), variable type(int, float or char),
                // variable size, variable offset
                for(Symbol symbol : identifierList){
                    // if symbol has not been initialized
                    if(symbol.getSymbolType() == null){

                        // set symbol type
                        symbol.setSymbolType(ESymbolType.variable);

                        // set variable type
                        symbol.variableAttributes.typeOfVariable = variableType;

                        // set variable size
                        symbol.variableAttributes.size = variableSize;
                    }
                }

                currentToken = tokenizer.getNextToken();
            }

            // if TypeMark is constant
            // then add appropriate attributes to the constant identifiers in the temporary data structure(identifierList)
            else if(currentToken.getTokenType() == TokenType.CONSTANT){
                // get the attributes by parsing the rest of the grammar : assignOp value
                currentToken = tokenizer.getNextToken();
                match(currentToken, TokenType.assignop);
                String numberTokenString = Value(); // this block does not end with getNextToken because, it happens in Value function

                // populate the attributes
                EVariableType constantType;
                if(numberTokenString.indexOf('.') == -1)
                    constantType = EVariableType.integerType;
                else
                    constantType = EVariableType.floatType;

                // go through all the constant identifiers and set their attributes
                // such as, symbol type(variable or consant), constant type(integer constant or float constant), numeric value(numeric values)
                for(Symbol symbol : identifierList){
                    if(symbol.getSymbolType() == null){
                        // set symbol type
                        symbol.setSymbolType(ESymbolType.constant);

                        // set constant type
                        symbol.constantAttributes.typeOfConstant = constantType;
                        // set numeric value
                        if(constantType == EVariableType.integerType) {
                            symbol.constantAttributes.value = Integer.parseInt(numberTokenString);
                            symbol.constantAttributes.size = 2;
                        }
                        else {
                            symbol.constantAttributes.valueR = Float.parseFloat(numberTokenString);
                            symbol.constantAttributes.size = 4;
                        }
                    }
                }
            }

            Symbol funcSymbol = _symbolTable.lookup(functionName_, ESymbolType.function);
            // if a valid parameter mode was passed to this method then all the identifiers in the identifierList are function parameters
            if(parameterMode_ != null) {
                // since all identifiers are function parameter we need to add parameter type and mode in another linked list
                for(int i = _identifierListOffset; i<identifierList.size(); i++){
                    _identifierListOffset++;
                    Symbol symbol = identifierList.get(i);
                    if (symbol.constantAttributes == null) {
                        funcSymbol.functionAttributes.parameterTypeList.add(symbol.variableAttributes.typeOfVariable);
                        symbol.variableAttributes.isParameter = true;
                        symbol.variableAttributes.parameterMode = parameterMode_;
                    }
                    else {
                        funcSymbol.functionAttributes.parameterTypeList.add(symbol.constantAttributes.typeOfConstant);
                        symbol.constantAttributes.isParameter = true;
                        symbol.constantAttributes.parameterMode = parameterMode_;
                    }

                    // add the mode for every symbol
                    funcSymbol.functionAttributes.parameterModeList.add(parameterMode_);
                }
            }
            // if no valid parameter mode was passed in then all the identifiers in the identifierList are local variables
            else {
                // set offset to local variables to 2, 4, 6 so on
                for(Symbol symbol : identifierList){
                    symbol.setOffset(_identifierOffset);
                    _identifierOffset += symbol.getSize();
                }

                // offset also represents the size of all local variable because offset is always incremented when a new identifier is added
                // we subtract 2 because our offset started at 2
                funcSymbol.functionAttributes.sizeOfLocalVariable = _identifierOffset - 2;

                // clear the list
                identifierList.clear();
            }
        }

        // looking for TypeMark but didn't find any, stop parsing and report error
        else {
            System.out.println("Error: At line number " + currentToken.getLineNumber() + ", expecting integer/float/char/const , but found " + currentToken.getTokenType() + " token with lexeme " + currentToken.getLexeme());
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

            // assign offset for all function parameters
            for(int i = identifierList.size()-1; i>= 0; i--){
                Symbol symbol = identifierList.get(i);
                symbol.setOffset(_identifierOffset);
                _identifierOffset += symbol.getSize();
            }

            // set the size of parameters in the function
            Symbol funcSymbol = _symbolTable.lookup(functionName_, ESymbolType.function);
            funcSymbol.functionAttributes.numberOfParameter = identifierList.size();
            funcSymbol.functionAttributes.sizeOfParameters = _identifierOffset - 4;

            // clear the function parameters from the list
            identifierList.clear();
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
        if(currentToken.getTokenType() == TokenType.id || currentToken.getTokenType() == TokenType.GET ||
                currentToken.getTokenType() == TokenType.PUT || currentToken.getTokenType() == TokenType.PUTLN){
            Statement();
            match(currentToken, TokenType.semicolon);
            StatTail();
        }
        // else empty production
    }

    // StatTail		-> 	Statement  ; StatTail | ε
    private void StatTail(){
        if(currentToken.getTokenType() == TokenType.id || currentToken.getTokenType() == TokenType.GET ||
                currentToken.getTokenType() == TokenType.PUT || currentToken.getTokenType() == TokenType.PUTLN){
            Statement();
            match(currentToken, TokenType.semicolon);
            StatTail();
        }
        // else empty production
    }

    // Statement		-> 	AssignStat	| IOStat
    private void Statement(){
        if(currentToken.getTokenType() == TokenType.id){
            isDefinedIdentifier(currentToken.getLexeme());

            String identifier = currentToken.getLexeme();
            match(currentToken, TokenType.id);
            AssignStat(identifier);
        } else {
            IOStat();
        }
    }

    // AssignStat		->	idt  :=  Expr
    private void AssignStat(String identifier_) {
        if(currentToken.getTokenType() == TokenType.assignop) {

            Symbol symbol = _symbolTable.lookup(identifier_);
            String variable1 = getSymbolLexemeOrOffset(symbol);
            match(currentToken, TokenType.assignop);
            String synthesizedAttributeofExpe = Expr();

            System.out.println(formattedString(new String[]{variable1, "=", synthesizedAttributeofExpe}));
            tacWriter.println(formattedString(new String[]{variable1, "=", synthesizedAttributeofExpe}));

        } else {
            ProcCall(identifier_);
        }
    }

    // ProcCall			->	idt ( Params )
    private void ProcCall(String procedureName_) {
        // current token has already been fowarded inside Statement grammar
        match(currentToken, TokenType.lparen);
        Params(procedureName_);
        match(currentToken, TokenType.rparen);

        System.out.println(formattedString(new String[]{"call" , procedureName_}));
        tacWriter.println(formattedString(new String[]{"call" , procedureName_}));
        _currentIndexOfFunctionParameter = 0;
    }

    //Params			->	idt ParamsTail | num ParamsTail | ε
    private void Params(String procedureName_) {
        if(currentToken.getTokenType() == TokenType.id || currentToken.getTokenType() == TokenType.num) {
            if (currentToken.getTokenType() == TokenType.id) {
                isDefinedIdentifier(currentToken.getLexeme());

                Symbol functionSymbol = _symbolTable.lookup(procedureName_, ESymbolType.function);
                if(functionSymbol.functionAttributes.parameterModeList.get(_currentIndexOfFunctionParameter) != EParameterModeType.in){
                    System.out.println(formattedString(new String[]{"push" , "@".concat(currentToken.getLexeme())}));
                    tacWriter.println(formattedString(new String[]{"push" , "@".concat(currentToken.getLexeme())}));
                } else {
                    System.out.println(formattedString(new String[]{"push" , currentToken.getLexeme()}));
                    tacWriter.println(formattedString(new String[]{"push" , currentToken.getLexeme()}));
                }

                match(currentToken, TokenType.id);
                _currentIndexOfFunctionParameter++;
                ParamsTail(procedureName_);

            } else if (currentToken.getTokenType() == TokenType.num) {
                System.out.println(formattedString(new String[]{"push" , currentToken.getLexeme()}));
                tacWriter.println(formattedString(new String[]{"push" , currentToken.getLexeme()}));
                match(currentToken, TokenType.id);
                _currentIndexOfFunctionParameter++;
                ParamsTail(procedureName_);
            }

        }
        //Params			->	ε
    }

    // ParamsTail		->	, idt ParamsTail | , num ParamsTail | ε
    private void ParamsTail(String procedureName_) {
        if(currentToken.getTokenType() == TokenType.comma){
            currentToken = tokenizer.getNextToken(); // consume the comma token

            if(currentToken.getTokenType() == TokenType.id || currentToken.getTokenType() == TokenType.num) {

                if (currentToken.getTokenType() == TokenType.id){
                    isDefinedIdentifier(currentToken.getLexeme());

                    Symbol functionSymbol = _symbolTable.lookup(procedureName_, ESymbolType.function);
                    if(functionSymbol.functionAttributes.parameterModeList.get(_currentIndexOfFunctionParameter) != EParameterModeType.in){
                        System.out.println(formattedString(new String[]{"push" , "@".concat(currentToken.getLexeme())}));
                        tacWriter.println(formattedString(new String[]{"push" , "@".concat(currentToken.getLexeme())}));
                    } else {
                        System.out.println(formattedString(new String[]{"push" , currentToken.getLexeme()}));
                        tacWriter.println(formattedString(new String[]{"push" , currentToken.getLexeme()}));
                    }

                    match(currentToken, TokenType.id);
                    _currentIndexOfFunctionParameter++;
                    ParamsTail(procedureName_);

                } else if(currentToken.getTokenType() == TokenType.num) {
                    System.out.println(formattedString(new String[]{"push" , currentToken.getLexeme()}));
                    tacWriter.println(formattedString(new String[]{"push" , currentToken.getLexeme()}));

                    match(currentToken, TokenType.num);
                    _currentIndexOfFunctionParameter++;
                    ParamsTail(procedureName_);
                }
            }
            else {
                System.out.println("Error: Expecting identifier or number token but found " + currentToken.getLexeme() + " at line number " + currentToken.getLineNumber());
                System.exit(1);
            }
        }
        // ParamsTail -> ε
    }

    // IOStat	->	InStat | OutStat
    private void IOStat() {
        if(currentToken.getTokenType() == TokenType.GET){
            InStat();
        } else {
            OutStat();
        }
    }

    // OutStat	->	put(WriteList) | putln(WriteList)
    private void OutStat() {
        if(currentToken.getTokenType() == TokenType.PUT){
            match(currentToken, TokenType.PUT);
            match(currentToken, TokenType.lparen);
            WriteList();
            match(currentToken, TokenType.rparen);
        } else if(currentToken.getTokenType() == TokenType.PUTLN){
            match(currentToken, TokenType.PUTLN);
            match(currentToken, TokenType.lparen);
            WriteList();
            match(currentToken, TokenType.rparen);
        }
    }

    // Write_List	->	Write_Token Write_List_Tail
    private void WriteList() {
        WriteToken();
        WriteListTail();
    }

    // Write_List_Tail ->	, Write_Token Write_List_Tail | ε
    private void WriteListTail() {
        if(currentToken.getTokenType() == TokenType.comma){
            match(currentToken, TokenType.comma);
            WriteToken();
            WriteListTail();
        }
        // Write_List_Tail -> ε
    }

    // 	Write_Token	->	idt | numt | literal
    private void WriteToken() {
        if(currentToken.getTokenType() == TokenType.id || currentToken.getTokenType() == TokenType.num || currentToken.getTokenType() == TokenType.string){ // todo check if id was defined before
            if(currentToken.getTokenType() == TokenType.id){
                Symbol tempSymbol = _symbolTable.lookup(currentToken.getLexeme());
                System.out.println(formattedString(new String[]{"wri", getSymbolLexemeOrOffset(tempSymbol)}));
                tacWriter.println(formattedString(new String[]{"wri", getSymbolLexemeOrOffset(tempSymbol)}));
            } // todo find how to deal with PUTLN, and How to print string and new line
            currentToken = tokenizer.getNextToken();
        } else {
            System.out.println("Error: Expecting identifier, number or string literal, but found " + currentToken.getTokenType() + " with lexeme " + currentToken.getLexeme() + " at line " + currentToken.getLineNumber());
            System.exit(1);
        }
    }

    // In_Stat		->	get(IdList)
    private void InStat() {
        if(currentToken.getTokenType() == TokenType.GET){
            match(currentToken, TokenType.GET);
            match(currentToken, TokenType.lparen);
            IdList();
            match(currentToken, TokenType.rparen);
        }
    }

    //IdList		->	idt  IdListTail
    private void IdList() {
        if(currentToken.getTokenType() == TokenType.id){
            Symbol tempSymbol = isDefinedIdentifier(currentToken.getLexeme());
            System.out.println(formattedString(new String[]{"rdi", getSymbolLexemeOrOffset(tempSymbol)}));
            tacWriter.println(formattedString(new String[]{"rdi", getSymbolLexemeOrOffset(tempSymbol)}));

            match(currentToken, TokenType.id);
            IdListTail();
        } else {
            System.out.println("Error: Expecting identifier token, but found " + currentToken.getTokenType() + " with lexeme " + currentToken.getLexeme() + " at line " + currentToken.getLineNumber());
            System.exit(1);
        }
    }

    // IdListTail	->	, idt IdListTail | ε
    private void IdListTail() {
        if(currentToken.getTokenType() == TokenType.comma){
            match(currentToken, TokenType.comma);
            isDefinedIdentifier(currentToken.getLexeme());

            Symbol tempSymbol = isDefinedIdentifier(currentToken.getLexeme());
            System.out.println(formattedString(new String[]{"rdi", getSymbolLexemeOrOffset(tempSymbol)}));
            tacWriter.println(formattedString(new String[]{"rdi", getSymbolLexemeOrOffset(tempSymbol)}));

            match(currentToken, TokenType.id);
            IdListTail();
        }
        // IdListTail	->	ε
    }

    // Expr			->	Relation
    private String Expr() {
        return Relation();
    }

    // Relation		->	SimpleExpr
    private String Relation() {
        return SimpleExpr();
    }

    // SimpleExpr		->	Term MoreTerm
    private String SimpleExpr() {
        String synthesizedAttributeofTerm = Term();
        return MoreTerm(synthesizedAttributeofTerm);
    }

    // MoreTerm		->	Addop Term MoreTerm | ε
    private String MoreTerm(String _inheritedAttrib) {
        if(currentToken.getTokenType() == TokenType.addop){
            Symbol tempSymbol = tempVariable();
            String variable1 = getSymbolLexemeOrOffset(tempSymbol);
            String operator = currentToken.getLexeme();
            match(currentToken, TokenType.addop);
            String synthesizedAttributeofTerm = Term();
            System.out.println(formattedString(new String[]{variable1, "=" , _inheritedAttrib, operator, synthesizedAttributeofTerm}));
            tacWriter.println(formattedString(new String[]{variable1, "=" , _inheritedAttrib, operator, synthesizedAttributeofTerm}));
            return MoreTerm(getSymbolLexemeOrOffset(tempSymbol));
        }
        // MoreTerm		->	ε
        // inheritedAttribute becomes synthesized attribute
        return _inheritedAttrib;
    }

    // Term			->	Factor  MoreFactor
    private String Term() {
        String synthesizedAttributeofFactor = Factor();
        return MoreFactor(synthesizedAttributeofFactor);
    }

    // MoreFactor		->  Mulop Factor MoreFactor| ε
    private String MoreFactor(String _inheritedAttrib) {
        if(currentToken.getTokenType() == TokenType.mulop){
            Symbol tempSymbol = tempVariable();
            String variable1 = getSymbolLexemeOrOffset(tempSymbol);
            String operator = currentToken.getLexeme();
            match(currentToken, TokenType.mulop);
            String synthesizedAttributeofFactor = Factor();
            System.out.println(formattedString(new String[]{variable1, "=" , _inheritedAttrib, operator, synthesizedAttributeofFactor}));
            tacWriter.println(formattedString(new String[]{variable1, "=" , _inheritedAttrib, operator, synthesizedAttributeofFactor}));
            return MoreFactor(getSymbolLexemeOrOffset(tempSymbol));
        }
        // MoreFactor		->  ε
        // inheritedAttribute becomes synthesized attribute since
        return _inheritedAttrib;
    }

    // Factor			->	id | num | ( Expr ) | not Factor | SignOp Factor
    private String Factor() {
        if(currentToken.getTokenType() == TokenType.id){
            String synthesizedAttributeofFactor = currentToken.getLexeme();
            Symbol tempSymbol = isDefinedIdentifier(synthesizedAttributeofFactor);
            currentToken = tokenizer.getNextToken();
            return getSymbolLexemeOrOffset(tempSymbol); // pass back up the identifier

        } else if(currentToken.getTokenType() == TokenType.num){

            Symbol tempSymbol = tempVariable();
            System.out.println(formattedString(new String[]{getSymbolLexemeOrOffset(tempSymbol), "=", currentToken.getLexeme()}));
            tacWriter.println(formattedString(new String[]{getSymbolLexemeOrOffset(tempSymbol), "=", currentToken.getLexeme()}));

            match(currentToken, TokenType.num);
            return getSymbolLexemeOrOffset(tempSymbol); // passing back _bp-X up in the tree
        } else if(currentToken.getTokenType() == TokenType.lparen){

            match(currentToken, TokenType.lparen);
            String synthesizedAttributeOfExpr = Expr();
            match(currentToken, TokenType.rparen);
            return synthesizedAttributeOfExpr; // todo Check if I have to return getSymbolLexemeOrOffset here, possible bug
        } else {

            SignOp();
            Symbol tempSymbol = tempVariable();
            String variable1 = getSymbolLexemeOrOffset(tempSymbol);
            String synthesizedAttributeofFactor = Factor();
            System.out.println(formattedString(new String[]{variable1, "=", "-".concat(synthesizedAttributeofFactor)}));
            tacWriter.println(formattedString(new String[]{variable1, "=", "-".concat(synthesizedAttributeofFactor)}));
            return getSymbolLexemeOrOffset(tempSymbol);
        }
    }

    // SignOp		    ->	-
    private void SignOp() {
        if(currentToken.getLexeme().charAt(0) == '-'){
            currentToken = tokenizer.getNextToken();
        } else {
            System.out.println("Error: Expecting SignOp '-' but found " + currentToken.getLexeme() + " at line number " + currentToken.getLineNumber());
            System.exit(1);
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
            System.out.println("Error: At line number " + currentToken.getLineNumber() + ", expecting " + desiredToken + " token, but found " + currentToken.getTokenType() + " token with lexeme " + currentToken.getLexeme());
            System.exit(1);
        } else {
            currentToken = tokenizer.getNextToken();
        }
    }

    private Symbol tempVariable(){
        String tempVariableName = "_t".concat(Integer.toString(_tempVariableID));

        Symbol tempSymbol = new Symbol(tempVariableName, _symbolTable.CurrentDepth);
        tempSymbol.setSymbolType(ESymbolType.variable);
        tempSymbol.setOffset(_identifierOffset);

        _symbolTable.insert(tempSymbol);
        _identifierOffset = _identifierOffset + 2; // next temp will get a new offset
        _tempVariableID++; // increment the postfix temp variable identifier
        return tempSymbol;
    }

    private String tempString(){
        return "_s".concat(Integer.toString(_tempStringID++));
    }

    private void checkForDuplicateEntry() {
        Symbol symbol = _symbolTable.lookup(currentToken.getLexeme());
        if(symbol != null && symbol.depth == _symbolTable.CurrentDepth){
            System.out.println("Error: Duplicate symbol: '" +currentToken.getLexeme() + "' at line number " + currentToken.getLineNumber());
            System.exit(1);
        }
    }

    public boolean isParsingSuccessful(){
        return isParsingSuccessful;
    }

    private String getSymbolLexemeOrOffset(Symbol symbol_){
        if(symbol_.depth > 1){
            if(symbol_.isParameter()){
                EParameterModeType mode = symbol_.getParameterMode();
                if(mode == EParameterModeType.in){
                    return "_bp+" + symbol_.getOffset();
                } else {
                    return "@_bp+" + symbol_.getOffset();
                }
            } else {
                return "_bp-" + symbol_.getOffset();
            }
        } else {
            return symbol_.lexeme;
        }
    }

    private Symbol isDefinedIdentifier(String lexeme_){
        Symbol symbol = _symbolTable.lookup(lexeme_);
        if (symbol != null && symbol.depth <= _symbolTable.CurrentDepth) {
            return symbol;
        } else {
            System.out.println("Error: Undefined identifier " + currentToken.getLexeme() + " at line number " + currentToken.getLineNumber());
            System.exit(1);
        }

        return null;
    }

    private String formattedString(String[] values_){
        if(values_.length == 1){
            return String.format("%-8s", values_[0]);
        }
        else if(values_.length == 2){
            return String.format("%-8s%-8s", values_[0], values_[1]);
        }
        else if(values_.length == 3){
            return String.format("%-8s%-8s%-8s", values_[0], values_[1], values_[2]);
        }
        else if(values_.length == 4){
            return String.format("%-8s%-8s%-8s%-8s", values_[0], values_[1], values_[2], values_[3]);
        }
        else if(values_.length == 5){
            return String.format("%-8s%-8s%-8s%-8s%-8s", values_[0], values_[1], values_[2], values_[3], values_[4]);
        } else
            return null;
    }

    public SymbolTable getSymbolTable() {
        return _symbolTable;
    }
}

