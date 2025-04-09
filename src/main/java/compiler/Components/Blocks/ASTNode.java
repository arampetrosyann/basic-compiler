package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public interface ASTNode {
    ASTNodeImpl toASTNode();
    void accept(Analyzer analyzer);
}
