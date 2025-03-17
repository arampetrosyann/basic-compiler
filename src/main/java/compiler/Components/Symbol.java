package compiler.Components;

public class Symbol {
    private final Token token;
    private final int lineNumber;
    private String value = null;

    public Symbol(Token token, int lineNumber) {
        this.token = token;
        this.lineNumber = lineNumber;
    }

    public Symbol(Token token, int lineNumber, String value) {
        this.token = token;
        this.lineNumber = lineNumber;
        this.value = value;
    }

    public Token getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return value != null ? "<" + token + "," + value +">" : "<" + token + ",>";
    }
}
