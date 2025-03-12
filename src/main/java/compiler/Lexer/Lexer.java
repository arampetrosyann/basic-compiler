package compiler.Lexer;

import compiler.Regex;
import compiler.utils.Symbol;
import compiler.utils.Token;

import java.io.*;
import java.util.*;

public class Lexer {
    private final PushbackReader input;

    private char currentCharacter;
    private int currentLine = 0;
    private boolean isComplete = false;

    private record SymbolConfig(Token token, boolean includeValue) {}

    private static final Map<Regex, SymbolConfig> patternsSymbol;
    static {
        Map<Regex, SymbolConfig> map = new LinkedHashMap<>();

        map.put(new Regex("free|final|rec|fun|for|while|if|else|return|int|float|bool|array|of|string|do"), new SymbolConfig(Token.KEYWORD, true));
        map.put(new Regex("([0-9]+\\.[0-9]*)|([0-9]*\\.[0-9]+)"), new SymbolConfig(Token.FLOAT_NUMBER, true));
        map.put(new Regex("[0-9][0-9]*"), new SymbolConfig(Token.INTEGER_NUMBER, true));
        map.put(new Regex("true|false"), new SymbolConfig(Token.BOOLEAN, true));
        map.put(new Regex("=="), new SymbolConfig(Token.EQUAL, false));
        map.put(new Regex("!="), new SymbolConfig(Token.NOT_EQUAL, false));
        map.put(new Regex("<="), new SymbolConfig(Token.LESS_OR_EQUAL, false));
        map.put(new Regex(">="), new SymbolConfig(Token.GREATER_OR_EQUAL, false));
        map.put(new Regex("="), new SymbolConfig(Token.ASSIGN, false));
        map.put(new Regex("\\+"), new SymbolConfig(Token.ADD, false));
        map.put(new Regex("\\-"), new SymbolConfig(Token.SUBTRACT, false));
        map.put(new Regex("\\*"), new SymbolConfig(Token.MULTIPLY, false));
        map.put(new Regex("/"), new SymbolConfig(Token.DIVIDE, false));
        map.put(new Regex("%"), new SymbolConfig(Token.MODULO, false));
        map.put(new Regex("<"), new SymbolConfig(Token.LESS, false));
        map.put(new Regex(">"), new SymbolConfig(Token.GREATER, false));
        map.put(new Regex("\\("), new SymbolConfig(Token.OPEN_PARENTHESIS, false));
        map.put(new Regex("\\)"), new SymbolConfig(Token.CLOSE_PARENTHESIS, false));
        map.put(new Regex("{"), new SymbolConfig(Token.OPEN_CURLY_BRACE, false));
        map.put(new Regex("}"), new SymbolConfig(Token.CLOSE_CURLY_BRACE, false));
        map.put(new Regex("\\["), new SymbolConfig(Token.OPEN_SQUARE_BRACKET, false));
        map.put(new Regex("\\]"), new SymbolConfig(Token.CLOSE_SQUARE_BRACKET, false));
        map.put(new Regex("&&"), new SymbolConfig(Token.LOGICAL_AND, false));
        map.put(new Regex("\\|\\|"), new SymbolConfig(Token.LOGICAL_OR, false));
        map.put(new Regex("\\."), new SymbolConfig(Token.DOT, false));
        map.put(new Regex(";"), new SymbolConfig(Token.SEMI_COLON, false));
        map.put(new Regex(","), new SymbolConfig(Token.COMMA, false));
        map.put(new Regex("!"), new SymbolConfig(Token.LOGICAL_NOT, true));
        map.put(new Regex("[_a-zA-Z][_a-zA-Z0-9]*"), new SymbolConfig(Token.IDENTIFIER, true));

        patternsSymbol = Collections.unmodifiableMap(map);
    }

    private static final List<Regex> expressions = new ArrayList<>(patternsSymbol.keySet());

    public Lexer(Reader source) {
        input = new PushbackReader(source, 100);

        readChar();
    }

    private void skipComment() {
        while (currentCharacter != '\n' && !isComplete) {
            readChar();
        }

        if (!isComplete) readChar();
    }

    private void pushBack(String str) {
        try {
            for (int i = str.length() - 1; i >= 0; i--) {
                input.unread(str.charAt(i));
            }
        } catch (IOException e) {
            throw new RuntimeException("IO exception during push back");
        }
    }

    private void readChar() {
        try {
            int elem = input.read();

            if (elem == -1) {
                isComplete = true;
                return;
            }

            currentCharacter = (char) elem;

            if (currentCharacter == '\n') currentLine++;
        } catch (IOException e) {
            throw new RuntimeException("IO exception while reading a character");
        }
    }
    
    public Symbol getNextSymbol() {
        if (isComplete) return new Symbol(Token.EOF);

        // skip whitespaces and comments
        while (Character.isWhitespace(currentCharacter) || currentCharacter == '$') {
            if (currentCharacter == '$') {
                skipComment();
            } else {
                readChar();
            }

            if (isComplete) return new Symbol(Token.EOF);
        }

        StringBuilder lexeme = new StringBuilder(String.valueOf(currentCharacter));

        // string literals
        if (currentCharacter == '"') {
            readChar();

            while (!isComplete && currentCharacter != '"') {
                lexeme.append(currentCharacter);
                readChar();
            }

            if (isComplete) {
                throw new IllegalArgumentException("Unterminated string on the line " + currentLine);
            }

            lexeme.append(currentCharacter);

            readChar();

            return new Symbol(Token.STRING, lexeme.toString());
        }

        readChar();

        // for longest match
        Symbol candidateSymbol = null;
        int candidateLength = 0;

        String currentLexeme = lexeme.toString();

        for (Regex pattern : expressions) {
            if (pattern.matches(currentLexeme)) {
                SymbolConfig conf = patternsSymbol.get(pattern);

                candidateSymbol = new Symbol(conf.token, conf.includeValue ? currentLexeme : null);
                candidateLength = currentLexeme.length();

                break;
            }
        }

        while (!isComplete && !Character.isWhitespace(currentCharacter)) {
            lexeme.append(currentCharacter);
            currentLexeme = lexeme.toString();

            boolean stillMatches = false;

            for (Regex pattern : expressions) {
                if (pattern.matches(currentLexeme)) {
                    SymbolConfig conf = patternsSymbol.get(pattern);

                    candidateSymbol = new Symbol(conf.token, conf.includeValue ? currentLexeme : null);
                    candidateLength = currentLexeme.length();

                    stillMatches = true;
                    break;
                }
            }

            if (stillMatches) {
                readChar();
            } else {
                break;
            }
        }

        if (candidateSymbol == null) {
            throw new IllegalArgumentException("Illegal character on the line " + currentLine);
        }

        // push back extra characters
        int extraLength = lexeme.length() - candidateLength;

        if (extraLength > 0) {
            pushBack(lexeme.substring(candidateLength));
            if (!isComplete) readChar();
        }

        return candidateSymbol;
    }

    public boolean isComplete() {
        return isComplete;
    }
}
