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
                                        "\t\tret " + sizeOfParameters +"\n" +
                                        funcName + "\t\tENDP";
        return postTranslatedCode;
    }

    public static String mainProcedure(String funcName){
        String mainProcedure =  "main\t\tPROC\n" +
                                "\t\tmov ax, @data\n" +
                                "\t\tmov ds, ax\n" +
                                "\t\tcall "+ funcName +"\n" +
                                "\t\tmov ah, 4ch\n" +
                                "\t\tint 21h\n" +
                                "main\t\tENDP\n" +
                                "\t\tEND main";
        return mainProcedure;
    }

    public static String additionTemplate(String var1, String var2, String var3){
        String additionTemplate =   "\t\tmov ax, " + var2 + "\n" +
                                    "\t\tadd ax, " + var3 + "\n" +
                                    "\t\tmov " + var1 + " , ax";
        return additionTemplate;
    }

    public static String copyTemplate(String var1, String var2){
        String copyTemplate =   "\t\tmov ax," + var2 + "\n" +
                                "\t\tmov " + var1 + " , ax";
        return copyTemplate;
    }

    public static String writeInteger(String var1){
        String writeIntegerTemplate =   "\t\tmov dx, " + var1 + "\n" +
                                        "\t\tcall writeint";
        return writeIntegerTemplate;
    }

    public static String pushTemplate(String var1){
        String pushTemplate =   "\t\tmov ax, "+ (var1.charAt(0) == '@' ? "offset" : "") + var1 + "\n"+
                                "\t\tpush ax";
        return pushTemplate;
    }

    public static String multiplicationTemplate(String var1, String var2, String var3) {
        String multiplicationTemplate =     "\t\tmov ax, " + var2 + "\n" +
                                            "\t\tmov bx, " + var3 + "\n" +
                                            "\t\timul bx\n" +
                                            "\t\tmov " + var1 + ", ax ";
        return multiplicationTemplate;
    }
}
