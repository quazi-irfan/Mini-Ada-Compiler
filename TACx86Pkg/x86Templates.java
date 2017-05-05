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

    public static String pushTemplate(String var1){
        String withOffset = "offset ".concat(var1.substring(1, var1.length()));
        String pushTemplate =   "\t\tmov ax, "+ (var1.charAt(0) == '@' ? withOffset : var1) + "\n"+
                                "\t\tpush ax";
        return pushTemplate;
    }

    public static String multiplicationTemplate(String var1, String var2, String var3) {
        String multiplicationTemplate = "";
        if(var2.charAt(0) == '@'){
            multiplicationTemplate = multiplicationTemplate.concat("\t\tmov bx, ").concat(var2.substring(1, var2.length())).concat("\n");
            multiplicationTemplate = multiplicationTemplate.concat("\t\tmov ax, [bx]").concat("\n");
        } else {
            multiplicationTemplate = multiplicationTemplate.concat("\t\tmov ax, ").concat(var2).concat("\n");
        }

        if(var3.charAt(0) == '@'){
            multiplicationTemplate = multiplicationTemplate.concat("\t\tmov bx, ").concat(var3.substring(1, var3.length())).concat("\n");
            multiplicationTemplate = multiplicationTemplate.concat("\t\tmov cx, [bx]").concat("\n");
            multiplicationTemplate = multiplicationTemplate.concat("\t\timul cx").concat("\n");
        } else {
            multiplicationTemplate = multiplicationTemplate.concat("\t\tmov bx, ").concat(var3).concat("\n");
            multiplicationTemplate = multiplicationTemplate.concat("\t\timul bx").concat("\n");
        }

        if(var1.charAt(0) == '@'){
            multiplicationTemplate = multiplicationTemplate.concat("\t\tmov bx,").concat(var1.substring(1, var1.length())).concat("\n");
            multiplicationTemplate = multiplicationTemplate.concat("\t\tmov [bx], ax");
        } else {
            multiplicationTemplate = multiplicationTemplate.concat("\t\tmov ").concat(var1).concat(", ax");
        }

        return multiplicationTemplate;
    }

    public static String writeString(String var1) {
        String writeStringTemplate =   "\t\tmov dx, offset " + var1 + "\n" +
                                        "\t\tcall writestr";
        return writeStringTemplate;
    }

    public static String readInt(String var1) {
        String readIntegerTemplate =   "\t\tcall readint\n" +
                                        "\t\tmov ax, bx\n" +
                                        "\t\tmov " + var1 + " , ax";
        return readIntegerTemplate;
    }

    public static String copyTemplate(String var1, String var2){
        String copyTemplate = "";
        if(var2.charAt(0) == '@'){
            copyTemplate = copyTemplate.concat("\t\tmov bx, ").concat(var2.substring(1, var2.length())).concat("\n");
            copyTemplate = copyTemplate.concat("\t\tmov ax, [bx]\n");
        } else {
            copyTemplate = copyTemplate.concat("\t\tmov ax, ").concat(var2).concat("\n");;
        }

        if(var1.charAt(0) == '@'){
            copyTemplate = copyTemplate.concat("\t\tmov bx, ").concat(var1.substring(1, var1.length())).concat("\n");
            copyTemplate = copyTemplate.concat("\t\tmov [bx], ax");
        } else {
            copyTemplate = copyTemplate.concat("\t\tmov ").concat(var1).concat(" , ax");
        }
        return copyTemplate;
    }

    public static String additionTemplate(String var1, String var2, String var3){
        String additionTemplate = "";

        if(var2.charAt(0) == '@') {
            additionTemplate = additionTemplate.concat("\t\tmov bx,").concat(var2.substring(1, var2.length())).concat("\n");
            additionTemplate = additionTemplate.concat("\t\tmov ax, [bx]").concat("\n");
        } else {
            additionTemplate = additionTemplate.concat("\t\tmov ax, ").concat(var2).concat("\n");
        }

        if(var3.charAt(0) == '@') {
            additionTemplate = additionTemplate.concat("\t\tmov bx,").concat(var3.substring(1, var3.length())).concat("\n");
            additionTemplate = additionTemplate.concat("\t\tadd ax, [bx]").concat("\n");
        } else {
            additionTemplate = additionTemplate.concat("\t\tadd ax, ").concat(var3).concat("\n");
        }

        if(var1.charAt(0) == '@'){
            additionTemplate = additionTemplate.concat("\t\tmov bx,").concat(var1.substring(1, var1.length())).concat("\n");
            additionTemplate = additionTemplate.concat("\t\tmov [bx], ax");
        } else {
            additionTemplate = additionTemplate.concat("\t\tmov ").concat(var1).concat(" , ax");
        }

        return additionTemplate;
    }

    public static String writeInteger(String var1){
        String writeIntegerTemplate ="";

        if(var1.charAt(0) == '@') {
            writeIntegerTemplate = writeIntegerTemplate.concat("\t\tmov bx, ").concat(var1.substring(1, var1.length())).concat("\n");
            writeIntegerTemplate = writeIntegerTemplate.concat("\t\tmov ax, [bx]").concat("\n");
        } else {
            writeIntegerTemplate = writeIntegerTemplate.concat("\t\tmov ax, ").concat(var1).concat("\n");
        }

        writeIntegerTemplate = writeIntegerTemplate.concat("\t\tcall writeint");

        return writeIntegerTemplate;
    }
}
