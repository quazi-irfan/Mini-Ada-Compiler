package SymbolTablePkg;

/**
 * Quazi Irfan
 * Compiler
 * Assignment 4
 * Symbol Table
 */

/* Given program:
    PROCEDURE one IS;

    id:integer;

    PROCEDURE two IS;
    BEGIN
    END two;

    PROCEDURE three(id:INTEGER) IS;
    id2:INTEGER;
    BEGIN
    END three;

    PROCEDURE four (id:INTEGER, OUT id2:INTEGER);
    id3:INTEGER;
    id4 : constant = 42;
    BEGIN
    END four;

    BEGIN
    END one;
 */

public class Main {
    public static void main(String[] args) throws Exception {
        SymbolTable st = new SymbolTable();

        // procedure one (only entry at global scope)
        st.insert("one", st.CurrentDepth);
        st.lookup("one").setSymbolType(ESymbolType.function);
        // set function attributes for one
        st.CurrentDepth++;

        st.insert("id", st.CurrentDepth);
        st.lookup("id").setSymbolType(ESymbolType.variable);
        // set attributes for variable id

        // procedure two
        st.insert("two", st.CurrentDepth);
        st.lookup("two").setSymbolType(ESymbolType.function);
        // set function attributes for two
        st.CurrentDepth++;
        System.out.println("Exiting procedure two");
        st.printDepth(st.CurrentDepth); // prints nothing
        st.CurrentDepth--;

        // procedure three
        st.insert("three", st.CurrentDepth);
        st.lookup("three").setSymbolType(ESymbolType.function);
        // set function attributes for three
        st.CurrentDepth++;
        st.insert("id", st.CurrentDepth);
        st.lookup("id").setSymbolType(ESymbolType.variable);
        // set attributes for variable id
        st.insert("id2", st.CurrentDepth);
        st.lookup("id2").setSymbolType(ESymbolType.variable);
        // set attributes for variable id2
        System.out.println("Exiting procedure three");
        st.printDepth(st.CurrentDepth);
        st.deleteDepth(st.CurrentDepth);
        st.CurrentDepth--;

        // procedure four
        st.insert("four", st.CurrentDepth);
        st.lookup("four").setSymbolType(ESymbolType.function);
        // set function attributes for four
        st.CurrentDepth++;
        st.insert("id", st.CurrentDepth);
        st.lookup("id").setSymbolType(ESymbolType.variable);
        // set attributes for variable id
        st.insert("id2", st.CurrentDepth);
        st.lookup("id2").setSymbolType(ESymbolType.variable);
        // set attributes for variable id2
        st.insert("id3", st.CurrentDepth);
        st.lookup("id3").setSymbolType(ESymbolType.variable);
        // set attributes for variable id3
        st.insert("id4", st.CurrentDepth);
        st.lookup("id4").setSymbolType(ESymbolType.constant);
        // set attributes for variable id4
        System.out.println("Exiting procedure four");
        st.printDepth(st.CurrentDepth);
        st.deleteDepth(st.CurrentDepth);
        st.CurrentDepth--;

        System.out.println("Exiting procedure One");
        st.printDepth(st.CurrentDepth);
        st.deleteDepth(st.CurrentDepth);
        st.CurrentDepth--;

        System.out.println("Exiting global scope");
        st.printDepth(st.CurrentDepth);
        st.deleteDepth(st.CurrentDepth);
        st.CurrentDepth--;
    }
}