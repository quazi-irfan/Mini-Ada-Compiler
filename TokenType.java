public enum TokenType {
    BEGIN, MODULE, CONSTANT, PROCEDURE, IS, IF, THEN, ELSE,
    ELSIF, WHILE, LOOP, FLOAT, INTEGER, CHAR, GET, PUT, END,

    id,
    inum,
    rnum,
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
