package TACx86Pkg;

public class x86Templates {
    public static String preGlobalVariables =   "\t\t.model small\n" +
                                                "\t\t.586\n" +
                                                "\t\t.stack 100h\n" +
                                                "\t\t.data";
    public static String postGlobalVariables =  "\t\t.code\n" +
                                                "\t\tinclude io.asm";

    public static String preTranslatedCode(String funcName, int sizeOfLocalVariable) {
        String preTranslatedCode =  funcName +  "\t\tproc\n" +
                                                "\t\tpush bp\n" +
                                                "\t\tmov bp, sp\n" +
                                                "\t\tsub sp, " + sizeOfLocalVariable;
        return preTranslatedCode;
    }

    public static String postTranslatedCode(String funcName, int sizeOfLocalVariable, int sizeOfParameters) {
        String postTranslatedCode =     "\t\tadd sp, "+ sizeOfLocalVariable + "\n" +
                                        "\t\tpop bp\n" +
                                        "\t\tret " + sizeOfParameters +"\n\t\t" +
                                        funcName + " ENDP";
        return postTranslatedCode;
    }

    public static String mainProcedure(String funcName){
        String mainProcedure =  "main\t\tPROC\n" +
                                "\t\tmov ax, @data\n" +
                                "\t\tmov ds, ax\n" +
                                "\t\tcall "+ funcName +"\n" +
                                "\t\tmov ah, 4ch\n" +
                                "\t\tint 21h\n" +
                                "\t\tmain ENDP";
        return mainProcedure;
    }
}
