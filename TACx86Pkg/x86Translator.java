package TACx86Pkg;

import SymbolTablePkg.SymbolTable;

import java.io.IOException;
import java.io.PrintWriter;

public class x86Translator {
    SymbolTable symbolTable;
    public boolean isSuccessfullyTranslated;

    public x86Translator(String tacFileName, SymbolTable symbolTable) throws IOException {
        this.symbolTable = symbolTable;

        String asmFileName = tacFileName.substring(0, tacFileName.length()-4).concat(".asm");
        PrintWriter asmWriter = new PrintWriter(asmFileName);



        asmWriter.close();
    }

    public boolean isSuccessfullyTranslated() {
        return this.isSuccessfullyTranslated;
    }
}
