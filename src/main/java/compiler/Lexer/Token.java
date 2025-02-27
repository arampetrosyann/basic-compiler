package compiler.Lexer;

public enum Token {
    COMMENT, // $
    STRING,
    KEYWORD, // free final rec fun for while if else return int float bool array of string do
    FLOAT_NUMBER,
    INTEGER_NUMBER,
    BOOLEAN,
    // Operators
    EQUAL, // ==
    NOT_EQUAL, // !=
    LESS_OR_EQUAL, // <=
    GREATER_OR_EQUAL, // >=
    ASSIGN, // =
    ADD, // +
    SUBTRACT, // -
    MULTIPLY, // *
    DIVIDE, // /
    MODULO, // %
    LESS, // <
    GREATER, // >
    OPEN_PARENTHESIS, // (
    CLOSE_PARENTHESIS, // )
    OPEN_CURLY_BRACE, // {
    CLOSE_CURLY_BRACE, // }
    OPEN_SQUARE_BRACKET, // [
    CLOSE_SQUARE_BRACKET, // ]
    LOGICAL_AND, // &&
    LOGICAL_OR, // ||
    DOT, // .
    SEMI_COLON, // ;
    COMMA, // ,
    WHITESPACE,
    IDENTIFIER, // ! - is a special built-in function
    EOF // end of file
}
