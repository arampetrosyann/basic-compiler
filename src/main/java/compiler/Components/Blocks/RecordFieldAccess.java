package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

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

    @Override
    public void accept(Generator generator) {
        generator.generateBlock(this);
    }
}
