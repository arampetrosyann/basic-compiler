package compiler.Components.Blocks;

public class Param extends ASTNodeImpl {
    private final Type type;
    private final String name;

    public Param(Type type, String name) {
        super("Param", null);
        this.type = type;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
