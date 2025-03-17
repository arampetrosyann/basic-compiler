package compiler.Components.Blocks;

import java.util.List;

public class RecordDefinition implements Statement {
    private final String name;
    private final List<RecordField> fields;

    public RecordDefinition(String name, List<RecordField> fields) {
        this.name = name;
        this.fields = fields;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("Record", name);
        for (RecordField field : fields) {
            node.addChild(field.toASTNode());
        }
        return node;
    }
}
