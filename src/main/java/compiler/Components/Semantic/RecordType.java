package compiler.Components.Semantic;

import java.util.Map;

public class RecordType extends VarType {
    private final Map<String, VarType> fields;

    public RecordType(Map<String, VarType> fields) {
        super(TypeName.RECORD);

        this.fields = fields;
    }

    public boolean hasField(String field) {
        return fields.containsKey(field);
    }

    public VarType getFieldValue(String field) {
        return fields.get(field);
    }

    public Map<String, VarType> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordType that = (RecordType) o;
        return this.fields.equals(that.fields);
    }

    @Override
    public int hashCode() {
        return fields.hashCode();
    }

}
