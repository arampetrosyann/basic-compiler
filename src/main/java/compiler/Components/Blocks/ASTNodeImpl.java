package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

import java.util.ArrayList;
import java.util.List;

public class ASTNodeImpl implements ASTNode {
    private final String type;
    private final String value;
    private final List<ASTNode> children;

    public ASTNodeImpl(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public void addChild(ASTNode child) {
        children.add(child);
    }

    public void printAST(int depth) {
        String indent = " ".repeat(depth * 2);

        // Print current node
        if (!type.equals("Program")) {
            System.out.println(indent + this);
        }

        // Recursively print child nodes
        for (ASTNodeImpl child : getChildren()) {
            child.printAST(depth + 1);
        }
    }

    public List<ASTNodeImpl> getChildren() {
        List<ASTNodeImpl> children = new ArrayList<>();
        for (ASTNode child : this.children) {
            children.add((ASTNodeImpl) child);
        }
        return children;
    }

    @Override
    public String toString() {
        return type + (value != null ? ", " + value : "");
    }

    @Override
    public ASTNodeImpl toASTNode() {
        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
