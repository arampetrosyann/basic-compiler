package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

public interface ASTNode {
    void accept(Analyzer analyzer);
    void accept(Generator generator);
    void setLineNumber(int value);
    int getLineNumber();
}
