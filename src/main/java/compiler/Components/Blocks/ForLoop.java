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

    @Override
    public ForLoop toASTNode() {
        addChild(new ASTNodeImpl("LoopVariable", variable));

        ASTNodeImpl startNode = new ASTNodeImpl("InitialValue", null);
        startNode.addChild(start.toASTNode());
        addChild(startNode);

        ASTNodeImpl maxValueNode = new ASTNodeImpl("MaximumValue", null);
        maxValueNode.addChild(maxValue.toASTNode());
        addChild(maxValueNode);

        ASTNodeImpl stepNode = new ASTNodeImpl("Step", null);
        stepNode.addChild(step.toASTNode());
        addChild(stepNode);

        ASTNodeImpl bodyNode = new ASTNodeImpl("Body", null);
        bodyNode.addChild(body.toASTNode());
        addChild(bodyNode);

        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
