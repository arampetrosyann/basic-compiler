package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public class RecordFieldAccess extends ASTNodeImpl implements Expression {
    private final Expression record;
    private final String fieldName;

    public RecordFieldAccess(Expression record, String fieldName) {
        super("RecordFieldAccess", null);
        this.record = record;
        this.fieldName = fieldName;
    }

    @Override
    public RecordFieldAccess toASTNode() {
        addChild(record.toASTNode());
        addChild(new ASTNodeImpl("FieldName", fieldName));

        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
