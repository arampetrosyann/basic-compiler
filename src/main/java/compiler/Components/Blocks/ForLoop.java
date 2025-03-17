package compiler.Components.Blocks;

public class ForLoop implements Statement {
    private final String variable;
    private final Expression start;
    private final Expression maxValue;
    private final Expression step;
    private final Block body;

    public ForLoop(String variable, Expression start, Expression maxValue, Expression step, Block body) {
        this.variable = variable;
        this.start = start;
        this.maxValue = maxValue;
        this.step = step;
        this.body = body;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("ForLoop", null);
        node.addChild(new ASTNodeImpl("LoopVariable", variable));

        ASTNodeImpl startNode = new ASTNodeImpl("InitialValue", null);
        startNode.addChild(start.toASTNode());
        node.addChild(startNode);

        ASTNodeImpl maxValueNode = new ASTNodeImpl("MaximumValue", null);
        maxValueNode.addChild(maxValue.toASTNode());
        node.addChild(maxValueNode);

        ASTNodeImpl stepNode = new ASTNodeImpl("Step", null);
        stepNode.addChild(step.toASTNode());
        node.addChild(stepNode);

        ASTNodeImpl bodyNode = new ASTNodeImpl("Body", null);
        bodyNode.addChild(body.toASTNode());
        node.addChild(bodyNode);

        return node;
    }
}
