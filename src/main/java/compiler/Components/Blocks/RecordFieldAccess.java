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

    public Expression getRecord() {
        return record;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
