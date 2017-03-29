package SymbolTablePkg;

import java.util.LinkedList;

/**
 * Every instance of symbol class in an entry to the symbol table.
 */
public class Symbol {
    // common attributes of a symbol
    public int depth;
    public String lexeme;
    public ESymbolType symbolType;

    // attributes of a symbol
    public VariableAttributes variableAttributes;
    public ConstantAttributes constantAttributes;
    public FunctionAttributes functionAttributes;

    public Symbol(String lexeme, ESymbolType symbolType, int depth) {
        this.lexeme = lexeme;
        this.symbolType = symbolType;
        this.depth = depth;
    }

    // attribute of variables
    private class VariableAttributes {
        public EVariableType typeOfVariable;
        public int offset;
        public int size;
    }

    // attribute of constants
    private class ConstantAttributes {
        public EVariableType typeOfConstant;
        public int offset;
        public int value;
        public float valueR;
    }

    // attribute of function
    private class FunctionAttributes {
        public int numberOfLocalVariable;
        public EVariableType returnType;
        public int numberOfParameter;
        public LinkedList<EVariableType> parameterList;
        public LinkedList<EParameterModeType> parameterModeList;
    }

    // getter and setter to set and get the symbolType of the symbol table
    public void setSymbolType(ESymbolType type_) {
        this.symbolType = type_;
        if(this.symbolType == ESymbolType.variable)
            variableAttributes = new VariableAttributes();
        else if(this.symbolType == ESymbolType.constant)
            constantAttributes = new ConstantAttributes();
        else
            functionAttributes = new FunctionAttributes();
    }

    public ESymbolType getSymbolType(){
        if(variableAttributes != null)
            return ESymbolType.variable;
        else if(constantAttributes != null)
            return ESymbolType.constant;
        else
            return ESymbolType.function;
    }
}


