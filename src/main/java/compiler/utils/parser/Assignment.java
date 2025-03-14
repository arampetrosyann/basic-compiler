package compiler.utils.parser;

public class Assignment implements Statement {
    private final Expression target;
    private final Expression value;

    public Assignment(Expression target, Expression value) {
        this.target = target;
        this.value = value;
    }


    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("Assignment", null);
        node.addChild(target.toASTNode()); // Left-hand side: could be a variable, an array access, or a record field access
        node.addChild(value.toASTNode()); // Right-hand side expression
        return node;
    }


}