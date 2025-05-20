import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.io.StringReader;

import compiler.Lexer;
import compiler.Components.Symbol;
import compiler.Components.Token;

public class TestLexer {
    private Lexer lex(String input) {
        StringReader reader = new StringReader(input);
        return new Lexer(reader);
    }

    private void matchKeyword(String keyword, Symbol symbol) {
        assertEquals(Token.KEYWORD, symbol.getToken());
        assertEquals(keyword, symbol.getValue());
    }

    @Test
    public void testVariableDeclaration() {
        String input = "x int = 2;";
        Lexer lexer = lex(input);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(Token.IDENTIFIER, symbol.getToken());
        assertEquals("x", symbol.getValue());

        symbol = lexer.getNextSymbol();
        matchKeyword("int", symbol);

        symbol = lexer.getNextSymbol();
        assertEquals(Token.ASSIGN, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.INTEGER_NUMBER, symbol.getToken());
        assertEquals("2", symbol.getValue());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.SEMI_COLON, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.EOF, symbol.getToken());
    }

    @Test
    public void testStringLiteral() {
        String input = "\"Hello, Compiler!\"";
        Lexer lexer = lex(input);

        Symbol symbol = lexer.getNextSymbol();
        assertNotNull(symbol);
        assertEquals(Token.STRING, symbol.getToken());
        assertEquals("Hello, Compiler!", symbol.getValue());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.EOF, symbol.getToken());
    }

    @Test
    public void testFloatNumber() {
        String input = "3.777";
        Lexer lexer = lex(input);

        Symbol symbol = lexer.getNextSymbol();
        assertNotNull(symbol);
        assertEquals(Token.FLOAT_NUMBER, symbol.getToken());
        assertEquals(input, symbol.getValue());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.EOF, symbol.getToken());
    }

    @Test
    public void testIntegerNumber() {
        String input = "123";
        Lexer lexer = lex(input);

        Symbol symbol = lexer.getNextSymbol();
        assertNotNull(symbol);
        assertEquals(Token.INTEGER_NUMBER, symbol.getToken());
        assertEquals(input, symbol.getValue());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.EOF, symbol.getToken());
    }

    @Test
    public void testBoolean() {
        String input = "true false";
        Lexer lexer = lex(input);

        Symbol symbol = lexer.getNextSymbol();
        assertNotNull(symbol);
        assertEquals(Token.BOOLEAN, symbol.getToken());
        assertEquals("true", symbol.getValue());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.BOOLEAN, symbol.getToken());
        assertEquals("false", symbol.getValue());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.EOF, symbol.getToken());
    }

    @Test
    public void testKeywords() {
        String input = "free final rec fun for while if else return int float bool array of string do";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertNotNull(symbol);
        matchKeyword("free", symbol);

        String[] keywords = input.split(" ");

        for (int i = 1; i < keywords.length; i++) {
            symbol = lexer.getNextSymbol();
            matchKeyword(keywords[i], symbol);
        }

        symbol = lexer.getNextSymbol();
        assertEquals(Token.EOF, symbol.getToken());
    }

    @Test
    public void testOperators() {
        String input = "== != <= >= = + - * / % < > && ||";
        Lexer lexer = lex(input);

        Symbol symbol = lexer.getNextSymbol();
        assertNotNull(symbol);
        assertEquals(Token.EQUAL, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.NOT_EQUAL, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.LESS_OR_EQUAL, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.GREATER_OR_EQUAL, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.ASSIGN, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.ADD, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.SUBTRACT, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.MULTIPLY, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.DIVIDE, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.MODULO, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.LESS, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.GREATER, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.LOGICAL_AND, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.LOGICAL_OR, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.EOF, symbol.getToken());
    }

    @Test
    public void testFunctionDeclaration() {
        String input = "fun add(a int, b int) int { return a + b; }";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);

        Symbol symbol = lexer.getNextSymbol();
        assertNotNull(symbol);
        matchKeyword("fun", symbol);

        symbol = lexer.getNextSymbol();
        assertEquals(Token.IDENTIFIER, symbol.getToken());
        assertEquals("add", symbol.getValue());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.OPEN_PARENTHESIS, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.IDENTIFIER, symbol.getToken());
        assertEquals("a", symbol.getValue());

        symbol = lexer.getNextSymbol();
        matchKeyword("int", symbol);

        symbol = lexer.getNextSymbol();
        assertEquals(Token.COMMA, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.IDENTIFIER, symbol.getToken());
        assertEquals("b", symbol.getValue());

        symbol = lexer.getNextSymbol();
        matchKeyword("int", symbol);

        symbol = lexer.getNextSymbol();
        assertEquals(Token.CLOSE_PARENTHESIS, symbol.getToken());

        symbol = lexer.getNextSymbol();
        matchKeyword("int", symbol);

        symbol = lexer.getNextSymbol();
        assertEquals(Token.OPEN_CURLY_BRACE, symbol.getToken());

        symbol = lexer.getNextSymbol();
        matchKeyword("return", symbol);

        symbol = lexer.getNextSymbol();
        assertEquals(Token.IDENTIFIER, symbol.getToken());
        assertEquals("a", symbol.getValue());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.ADD, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.IDENTIFIER, symbol.getToken());
        assertEquals("b", symbol.getValue());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.SEMI_COLON, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.CLOSE_CURLY_BRACE, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.EOF, symbol.getToken());
    }

    @Test
    public void testParenthesesAndBraces() {
        String input = "() {} []";
        Lexer lexer = lex(input);

        Symbol symbol = lexer.getNextSymbol();
        assertNotNull(symbol);
        assertEquals(Token.OPEN_PARENTHESIS, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.CLOSE_PARENTHESIS, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.OPEN_CURLY_BRACE, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.CLOSE_CURLY_BRACE, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.OPEN_SQUARE_BRACKET, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.CLOSE_SQUARE_BRACKET, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.EOF, symbol.getToken());
    }

    @Test
    public void testComments() {
        String input = "$ This is a comment\n v bool = true;";
        Lexer lexer = lex(input);

        Symbol symbol = lexer.getNextSymbol();
        assertEquals(Token.IDENTIFIER, symbol.getToken());
        assertEquals("v", symbol.getValue());

        symbol = lexer.getNextSymbol();
        matchKeyword("bool", symbol);

        symbol = lexer.getNextSymbol();
        assertEquals(Token.ASSIGN, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.BOOLEAN, symbol.getToken());
        assertEquals("true", symbol.getValue());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.SEMI_COLON, symbol.getToken());

        symbol = lexer.getNextSymbol();
        assertEquals(Token.EOF, symbol.getToken());
    }

    @Test(expected = compiler.Exceptions.LexerException.class)
    public void testIllegalCharacter() {
        String input = "@";
        Lexer lexer = lex(input);

        lexer.getNextSymbol();
    }

    @Test(expected = compiler.Exceptions.LexerException.class)
    public void testUnterminatedString() {
        String input = "\"Test string";
        Lexer lexer = lex(input);

        lexer.getNextSymbol();
    }
}
