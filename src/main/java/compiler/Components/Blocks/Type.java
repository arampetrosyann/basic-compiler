package compiler.Components.Blocks;

public class Type extends ASTNodeImpl {
    private final String identifier;
    private final TypeCategory category;

    public Type(String identifier, TypeCategory category) {
        super("Type", identifier);
        this.identifier = identifier;
        this.category = category;
    }

    public String getIdentifier() {
        return identifier;
    }

    public TypeCategory getCategory() {
        return category;
    }

    public Type getArrayElementType() {
        if (category == TypeCategory.ARRAY) {
            String elementIdentifier = identifier.substring(0, identifier.length() - 2);
            TypeCategory elementCategory = Character.isUpperCase(elementIdentifier.charAt(0))
                    ? TypeCategory.RECORD
                    : TypeCategory.PRIMITIVE;
            return new Type(elementIdentifier, elementCategory);
        }
        throw new IllegalStateException("Not an array type: " + identifier);
    }
}
