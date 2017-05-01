package TACx86Pkg;

import SymbolTablePkg.SymbolTable;

import java.io.*;

public class x86Translator {
    SymbolTable symbolTable;
    private static BufferedReader tacReader;
    private String statement;
    public boolean isSuccessfullyTranslated;

    public x86Translator(String tacFileName, SymbolTable symbolTable) throws IOException {
        this.symbolTable = symbolTable;

        String asmFileName = tacFileName.substring(0, tacFileName.length()-4).concat(".asm");
        PrintWriter asmWriter = new PrintWriter(asmFileName);

        try{
            tacReader = new BufferedReader(new FileReader(tacFileName));
        }catch (FileNotFoundException e){
            System.out.println("File not found: " + tacFileName);
            System.exit(1);
        }

        statement = tacReader.readLine();
        while(statement == null){
            

            statement = tacReader.readLine();
        }

        asmWriter.close();
    }

    public boolean isSuccessfullyTranslated() {
        return this.isSuccessfullyTranslated;
    }
}
