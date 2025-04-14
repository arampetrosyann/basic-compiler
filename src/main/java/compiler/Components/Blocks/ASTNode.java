package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public interface ASTNode {
    void accept(Analyzer analyzer);
}
