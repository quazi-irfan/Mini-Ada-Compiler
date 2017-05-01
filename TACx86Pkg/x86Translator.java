package TACx86Pkg;

import SymbolTablePkg.ESymbolType;
import SymbolTablePkg.Symbol;
import SymbolTablePkg.SymbolTable;

import java.io.*;
import java.util.StringTokenizer;

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

        asmWriter.println(x86Templates.preGlobalVariables);
        for(Symbol symbol : symbolTable.lookup(1)){
            if(symbol.getSymbolType() != ESymbolType.function)
            if(symbol.getSymbolType() == ESymbolType.string)
                asmWriter.println(formattedString(new String[]{symbol.lexeme, "db", symbol.stringAttributes.attribute}));
            else
                asmWriter.println(formattedString(new String[]{symbol.lexeme, "dw", "?"}));
        }
        asmWriter.println(x86Templates.postGlobalVariables);


        do{
            // tac file never should be empty, so statement will never receive null
            statement = tacReader.readLine();
            if(statement == null) break;

            StringTokenizer tokenizer = new StringTokenizer(statement);
            String firstToken = tokenizer.nextToken();

            if(firstToken.equals("PROC")) {
                String funcName = tokenizer.nextToken();
                Symbol symbol = this.symbolTable.lookup(funcName, ESymbolType.function);
                asmWriter.println(x86Templates.preTranslatedCode(funcName, symbol.functionAttributes.sizeOfLocalVariable));

                continue;
            }

            /*
            2 types of assignment
            x = y op z
            x = op y

            copy statement
            x = y

            x = y op z
            can be +,-,*,/
             */

            if(firstToken.equals("ENDP")) {
                String funcName = tokenizer.nextToken();
                Symbol symbol = this.symbolTable.lookup(funcName, ESymbolType.function);
                asmWriter.println(x86Templates.postTranslatedCode(funcName, symbol.functionAttributes.sizeOfLocalVariable, symbol.functionAttributes.sizeOfParameters));

                continue;
            }

            if(firstToken.equals("START")){
                tokenizer.nextToken();
                String funcName = tokenizer.nextToken();
                asmWriter.println(x86Templates.mainProcedure(funcName));
            }
        } while (statement != null);

        asmWriter.close();
    }

    public boolean isSuccessfullyTranslated() {
        return this.isSuccessfullyTranslated;
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
}
