import SymbolTablePkg.ESymbolType;
import SymbolTablePkg.EVariableType;
import SymbolTablePkg.Symbol;
import SymbolTablePkg.SymbolTable;
import TokenizerPkg.TokenType;

/**
 * Quazi Irfan
 * Compiler
 * Assignment 4
 * Symbol Table
 */
public class Main {
    public static void main(String[] args) throws Exception{
        SymbolTable symbolTable = new SymbolTable();
        symbolTable.insert("abc", ESymbolType.constant, 1);
        Symbol symbol = symbolTable.lookup("abc");
        System.out.println(symbol.depth);
    }
}
