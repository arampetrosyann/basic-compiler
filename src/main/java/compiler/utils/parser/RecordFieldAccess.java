package compiler.utils.parser;

public class RecordFieldAccess implements Expression {
    private final Expression record;
    private final String fieldName;

    public RecordFieldAccess(Expression record, String fieldName) {
        this.record = record;
        this.fieldName = fieldName;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("RecordFieldAccess", null);

        node.addChild(record.toASTNode());
        node.addChild(new ASTNodeImpl("FieldName", fieldName));

        return node;
    }


}
