package SymbolTablePkg;

import TokenizerPkg.TokenType;

import java.util.ArrayList;
import java.util.LinkedList;

public class SymbolTable {
    public static int tableSize = 211;
    private ArrayList<LinkedList<Symbol>> hashTable = new ArrayList<>(); // array of 'linked list of symbols'

    public SymbolTable(){
        // initialize all 211 elements as null
        for(int i = 0; i<tableSize; i++){
            hashTable.add(null); // initialize every linkedlist as null
        }
    }

    public Symbol lookup(String lexeme){
        for(int i = 0; i< SymbolTable.tableSize; i++){
            LinkedList<Symbol> tempLinkedList = hashTable.get(i);
            if(tempLinkedList != null){
                for(int j = 0; j<tempLinkedList.size(); j++){
                    Symbol tempSymbol = tempLinkedList.get(j);
                    if(tempSymbol.lexeme.equals(lexeme)){
                        return tempSymbol;
                    }
                }
            }
        }
        return null;
    }

    public void insert(String lexeme, TokenType tokenType, int depth){
        // generate the symbol
        Symbol tempSymbol = new Symbol();
        tempSymbol.lexeme = lexeme;
        tempSymbol.tokenType = tokenType;
        tempSymbol.depth = depth;

        // insert it to the hash table
        int index = hash(lexeme);
        LinkedList<Symbol> tempLinkedList = hashTable.get(index);
        if(tempLinkedList == null){
            tempLinkedList = new LinkedList<>();
            tempLinkedList.addFirst(tempSymbol);
            hashTable.set(index, tempLinkedList);
        }
        else {
            tempLinkedList.addFirst(tempSymbol);
        }

    }

    public void printTable(int depth){

    }

    public void deleteDepth(int depth){

    }

    private int hash(String lexeme){
        // In C unsigned int is 2 or 4 byte or 16 or 32 bit long ( we will assume 32 bit )
        // Range of 32 bit unsigned value is 0 to 4,294,967,295(2^32-1) or 0000 0000 to FFFF FFFF

        // In Java int is 32 bit; they are always signed and uses 2's complement
        // Range of 32 bit signed value is -2,147,483,648(-2^32) to 2,147,483,647(2^32-1) or 0000 0000 to FFFF FFFF
        int h = 0, g = 0;
        for(char c : lexeme.toCharArray()){
            h = (h << 4) + c;
            g = h & 0xF000_0000; // 1111 0000 0000 0000  0000 0000 0000 0000
            long temp = Long.parseLong(Integer.toUnsignedString(g)); // using long since it is larger than int
            if(temp > 0 ){
                h = h ^ (g >> 24);
                h = h ^ g;
            }
        }
        return h % SymbolTable.tableSize;
    }
}
