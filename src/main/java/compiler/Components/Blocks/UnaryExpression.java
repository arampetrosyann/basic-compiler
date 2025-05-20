package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Components.Token;
import compiler.Generator;

public class UnaryExpression extends ASTNodeImpl implements Expression {
    private final Token operator;
    private final Expression operand;

    public UnaryExpression(Token operator, Expression operand) {
        super("UnaryExpression", null);
        this.operator = operator;
        this.operand = operand;
    }

    public Token getOperator() {
        return operator;
    }

    public Expression getOperand() {
        return operand;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }

    @Override
    public void accept(Generator generator) {
        generator.generateBlock(this);
    }
}
