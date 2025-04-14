package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;
import compiler.Components.Symbol;
import compiler.Components.Token;

public class Literal extends ASTNodeImpl implements Expression {
    private final String value; // should be of type int, float, bool or string
    private final String type;

    public Literal(Symbol symbol) {
        super(symbol.getToken().toString(), symbol.getValue());
        String value = symbol.getValue();
        Token token = symbol.getToken();
        String type = (token == Token.STRING) ? "String" :
                (token == Token.INTEGER_NUMBER) ? "Integer" :
                        (token == Token.FLOAT_NUMBER) ? "Float" :
                                (token == Token.BOOLEAN) ? "Boolean" :
                                        null;
        if (type == null) {
            throw new IllegalArgumentException("Invalid literal type: " + value.getClass().getSimpleName());
        }
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
