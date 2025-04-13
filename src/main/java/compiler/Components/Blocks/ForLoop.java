package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public class ForLoop extends ASTNodeImpl implements Statement {
    private final String variable;
    private final Expression start;
    private final Expression maxValue;
    private final Expression step;
    private final Block body;

    public ForLoop(String variable, Expression start, Expression maxValue, Expression step, Block body) {
        super("ForLoop", null);
        this.variable = variable;
        this.start = start;
        this.maxValue = maxValue;
        this.step = step;
        this.body = body;
    }

    public String getVariable() {
        return variable;
    }

    public Expression getStart() {
        return start;
    }

    public Expression getMaxValue() {
        return maxValue;
    }

    public Expression getStep() {
        return step;
    }

    public Block getBody() {
        return body;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
