package TokenizerPkg;

/**
 * This euum class holds type of tokens we can detect
 */

public enum TokenType {
    BEGIN, MODULE, CONSTANT, PROCEDURE, IS, IF, THEN, ELSE,
    ELSIF, WHILE, LOOP, FLOAT, INTEGER, CHAR, GET, PUT, END,

    id,
    num,
    string,

    relop, // = /= < <= > >=
    addop, // + - or
    mulop, // * / rem mod and
    assignop,

    lparen,
    rparen,
    comma,
    colon,
    semicolon,
    period,

    eof,
    unknown
}
