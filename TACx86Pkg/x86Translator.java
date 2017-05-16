package TACx86Pkg;

import SymbolTablePkg.EParameterModeType;
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
                else if(symbol.getSymbolType() == ESymbolType.constant)
                    asmWriter.println(formattedString(new String[]{symbol.lexeme, "dw", symbol.getConstantValue()}));
                else
                    asmWriter.println(formattedString(new String[]{symbol.lexeme, "dw", "?"}));
        }
        asmWriter.println(x86Templates.postGlobalVariables);


        do{
            // tac file never should be empty, so statement will never receive null
            statement = tacReader.readLine();
            if(statement == null) break;
            asmWriter.println("\n\t\t;" + statement);

            StringTokenizer tokenizer = new StringTokenizer(statement);
            String firstToken = tokenizer.nextToken();

            // PROC statement
            if(firstToken.equals("PROC")) {
                String funcName = tokenizer.nextToken();
                Symbol symbol = this.symbolTable.lookup(funcName, ESymbolType.function);
                asmWriter.println(x86Templates.preTranslatedCode(funcName, symbol.functionAttributes.sizeOfLocalVariable));

                continue;
            }
            // ENDP statement
            else if(firstToken.equals("ENDP")) {
                String funcName = tokenizer.nextToken();
                Symbol symbol = this.symbolTable.lookup(funcName, ESymbolType.function);
                asmWriter.println(x86Templates.postTranslatedCode(funcName, symbol.functionAttributes.sizeOfLocalVariable, symbol.functionAttributes.sizeOfParameters));

                continue;
            }
            // START statement
            else if(firstToken.equals("START")){
                tokenizer.nextToken();
                String funcName = tokenizer.nextToken();
                asmWriter.println(x86Templates.mainProcedure(funcName));

                continue;
            }

            // push statement
            else if(firstToken.equals("push")){
                String var = fixBP(tokenizer.nextToken());
                asmWriter.println(x86Templates.pushTemplate(var));

                continue;
            }

            // call statement
            else if(firstToken.equals("call")){
                String var = tokenizer.nextToken();
                asmWriter.println("\t\tcall " + var);

                continue;
            }

            else if(firstToken.equals("wrln")){
                asmWriter.println("\t\tcall writeln");
            }

            // Output statement
            else if(firstToken.length() > 2 && firstToken.substring(0, 2).equals("wr")){
                if(firstToken.charAt(2) == 'i'){
                    String var1 = fixBP(tokenizer.nextToken());
                    asmWriter.println(x86Templates.writeInteger(var1));
                } else {
                    String var1 = fixBP(tokenizer.nextToken());
                    asmWriter.println(x86Templates.writeString(var1));
                }

                continue;
            }

            // Input statement
            else if(firstToken.length() > 2 && firstToken.substring(0, 2).equals("rd")){
                String var1 = fixBP(tokenizer.nextToken());
                asmWriter.println(x86Templates.readInt(var1));
                continue;
            }

            // Assignment statement #1 : x = y op z
            else if(statement.length() > 38){
                String var1 = fixBP(firstToken);
                tokenizer.nextToken();
                String var2 = fixBP(tokenizer.nextToken());
                String operator = fixBP(tokenizer.nextToken());
                String var3 = fixBP(tokenizer.nextToken());

                if(operator.equals("+")){
                    asmWriter.println(x86Templates.additionTemplate(var1,var2,var3));
                } else if(operator.equals("-")){
                    //todo subtraction
                } else if(operator.equals("*")){
                    asmWriter.println(x86Templates.multiplicationTemplate(var1,var2,var3));
                } else if(operator.equals("/")){
                    // todo division
                }

                continue;
            }

            // Assignment statement #2 : x = op y
            else if(statement.length() > 22 && (statement.charAt(16) == '-')){
                String var1 = fixBP(firstToken);
                tokenizer.nextToken();
                String var2 = fixBP(tokenizer.nextToken());
                asmWriter.println(x86Templates.negTemplate(var1, var2));

                continue;
            }

            // copy statement x = y
            else {
                String var1 = fixBP(firstToken);    //x
                tokenizer.nextToken();              //=
                String var2 = fixBP(tokenizer.nextToken());//y

                asmWriter.println(x86Templates.copyTemplate(var1, var2));

                continue; // redundant
            }
        } while (statement != null);

        isSuccessfullyTranslated = true;

        asmWriter.close();
    }

    private String fixBP(String var) { // todo : Does not handle -_bp-12
        StringBuilder builder;
        if(var.contains("bp")){
            builder = new StringBuilder();

            if(var.contains("@"))
                builder.append("@[").append(var.substring(2, var.length())).append("]"); // @_bp-2 becomes @[bp-2]
            else
                builder.append("[").append(var.substring(1, var.length())).append("]"); // _bp-2 becomes [bp-2]

            return builder.toString();
        }
        return var;
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
