package SymbolTablePkg;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Every instance of symbol class in an entry to the symbol table.
 */
public class Symbol {
    // attributes initialized during constructor
    public int depth;
    public String lexeme;

    // attributes initialized later
    public ESymbolType symbolType;
    public VariableAttributes variableAttributes;
    public ConstantAttributes constantAttributes;
    public FunctionAttributes functionAttributes;

    public Symbol(String lexeme, int depth) {
        this.lexeme = lexeme;
        this.depth = depth;
    }

    interface SymbolAttributes{

    }

    // attribute of variables
    public class VariableAttributes implements SymbolAttributes{
        public EVariableType typeOfVariable;
        public int offset;
        public int size;
        public boolean isParameter;

        @Override
        public String toString(){
            return "Type: " + typeOfVariable.toString() + ", Offset: " + offset + ", Size: " + size + (isParameter ? " Parameter" : " Not Parameter");
        }
    }

    // attribute of constants
    public class ConstantAttributes implements SymbolAttributes{
        public EVariableType typeOfConstant;
        public int size;
        public int offset;
        public int value;
        public float valueR;
        public boolean isParameter;

        @Override
        public String toString(){
            if(typeOfConstant == EVariableType.integerType)
                return "Type: " + typeOfConstant.toString() + ", Offset: " + offset + ", Value: " + value + (isParameter ? " Parameter" : " Not Parameter");
            else
                return "Type: " + typeOfConstant.toString() + ", Offset: " + offset + ", ValueR: " + valueR + (isParameter ? " Parameter" : " Not Parameter");
        }
    }

    // attribute of function
    public class FunctionAttributes implements SymbolAttributes{
        public int sizeOfLocalVariable;
        public int sizeOfParameters;
        public int numberOfParameter;
        public LinkedList<EVariableType> parameterTypeList;
        public LinkedList<EParameterModeType> parameterModeList;

        @Override
        public String toString(){
            return "Num of params: " + numberOfParameter +
                    ", Size of params: " + sizeOfParameters + " bytes" +
                    ", Type of Params: " + Arrays.toString(parameterTypeList.toArray()) +
                    ", Mode of Params: " + Arrays.toString(parameterModeList.toArray()) +
                    ", Size of Local Var: " + sizeOfLocalVariable + " bytes";
        }
    }

    // getter and setter to set and get the symbolType of the symbol table
    public void setSymbolType(ESymbolType type_) {
        this.symbolType = type_;
        if(this.symbolType == ESymbolType.variable)
            variableAttributes = new VariableAttributes();
        else if(this.symbolType == ESymbolType.constant)
            constantAttributes = new ConstantAttributes();
        else{
            functionAttributes = new FunctionAttributes();
            functionAttributes.parameterTypeList = new LinkedList<>();
            functionAttributes.parameterModeList = new LinkedList<>();
        }
    }

    public ESymbolType getSymbolType(){
        if(variableAttributes != null)
            return ESymbolType.variable;
        else if(constantAttributes != null)
            return ESymbolType.constant;
        else if(functionAttributes != null)
            return ESymbolType.function;
        else // all are null
            return null;
    }

    public SymbolAttributes getSymbolAttributes(){
        if(variableAttributes != null)
            return variableAttributes;
        else if(constantAttributes != null)
            return constantAttributes;
        else if(functionAttributes != null)
            return functionAttributes;
        else // all are null
            return null;
    }

    public int getOffset(){
        if(variableAttributes != null)
            return variableAttributes.offset;
        else if(constantAttributes != null)
            return constantAttributes.offset;
        else
            return 0;
    }

    public void setOffset(int offset_){
        if(variableAttributes != null)
            variableAttributes.offset = offset_;
        else if(constantAttributes != null)
            constantAttributes.offset = offset_;
    }

    public int getSize(){
        if(variableAttributes != null){
            if(variableAttributes.typeOfVariable == EVariableType.integerType)
                return 2;
            else if(variableAttributes.typeOfVariable == EVariableType.floatType)
                return 4;
            else
                return 1;
        }
        else if(constantAttributes != null){
            if(constantAttributes.typeOfConstant == EVariableType.integerType)
                return 2;
            else
                return 4;
        }
        else
            return 0;
    }
}


