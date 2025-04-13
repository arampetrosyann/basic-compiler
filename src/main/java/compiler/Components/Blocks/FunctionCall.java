package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

import java.util.List;

public class FunctionCall extends ASTNodeImpl implements Expression, Statement {
    private final String functionName;
    private final List<Expression> arguments;

    public FunctionCall(String functionName, List<Expression> arguments) {
        super("FunctionCall", functionName);
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}