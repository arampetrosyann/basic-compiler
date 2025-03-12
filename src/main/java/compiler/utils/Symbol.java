package compiler.utils;

public class Symbol {
    private final Token token;
    private String value = null;

    public Symbol(Token token) {
        this.token = token;
    }

    public Symbol(Token token, String value) {
        this.token = token;
        this.value = value;
    }

    public Token getToken() {
        return token;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value != null ? "<" + token + "," + value +">" : "<" + token + ",>";
    }
}
