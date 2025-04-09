package compiler.Components.Blocks;

public class RecordField extends ASTNodeImpl {
    private final String name;
    private final Type type;

    public RecordField(String name, Type type) {
        super("Field", name);
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public RecordField toASTNode() {
        addChild(type.toASTNode());
        return this;
    }
}

