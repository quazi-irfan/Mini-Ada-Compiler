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
        SymbolTable st = new SymbolTable();
        st.insert("abc", TokenType.id, 0);
        Symbol s = st.lookup("abc");
        System.out.println(s.depth);
    }
}
