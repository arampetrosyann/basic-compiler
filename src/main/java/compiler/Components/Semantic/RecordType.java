package compiler.Components.Semantic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecordType extends VarType {
    private final String recordName;
    private final Map<String, VarType> fields;

    public RecordType(String name, Map<String, VarType> fields) {
        super(TypeName.RECORD);

        this.recordName = name;
        this.fields = fields;
    }

    public boolean hasField(String field) {
        return fields.containsKey(field);
    }

    public String getRecordName() {
        return recordName;
    }

    public VarType getFieldValue(String field) {
        return fields.get(field);
    }

    public Map<String, VarType> getFields() {
        return fields;
    }

    public List<String> getFieldNames() {
        return new ArrayList<>(fields.keySet());
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
