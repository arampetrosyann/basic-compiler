package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

import java.util.ArrayList;

public class Method extends ASTNodeImpl implements Statement, Expression {
    private final String name;
    private final Type returnType;
    private final ArrayList<Param> parameters;
    private final Block body;

    public Method(String name, Type returnType, ArrayList<Param> parameters, Block body) {
        super("Method", name);
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
        this.body = body;
    }

    public Block getBody() {
        return body;
    }

    public ArrayList<Param> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    public Method toASTNode() {
        if(returnType != null) {
            addChild(returnType.toASTNode());
        } else {
            addChild(new ASTNodeImpl("ReturnType", "void"));
        }

        if(!parameters.isEmpty()) {
            ASTNodeImpl paramsNode = new ASTNodeImpl("Parameters", null);
            for (Param param : parameters) {
                paramsNode.addChild(param.toASTNode());
            }
            addChild(paramsNode);
        }

        if(!body.getStatements().isEmpty()) {
            ASTNodeImpl bodyNode = new ASTNodeImpl("Body", null);
            bodyNode.addChild(body.toASTNode());
            addChild(bodyNode);
        }

        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
