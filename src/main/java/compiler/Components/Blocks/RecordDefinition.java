package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

import java.util.List;

public class RecordDefinition extends ASTNodeImpl implements Statement {
    private final String name;
    private final List<RecordField> fields;

    public RecordDefinition(String name, List<RecordField> fields) {
        super("Record", name);
        this.name = name;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public List<RecordField> getFields() {
        return fields;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
