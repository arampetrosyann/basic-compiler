package compiler.Components.Blocks;

import java.util.ArrayList;

public class Method implements ASTNode {
    private final String name;
    private final Type returnType;
    private final ArrayList<Param> parameters;
    private final Block body;

    public Method(String name, Type returnType, ArrayList<Param> parameters, Block body) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
        this.body = body;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("Method", name);

        if(returnType != null) {
            ASTNodeImpl returnTypeNode = new ASTNodeImpl("ReturnType", null);
            returnTypeNode.addChild(returnType.toASTNode());
            node.addChild(returnTypeNode);
        } else {
            node.addChild(new ASTNodeImpl("ReturnType", "void"));
        }

        if(!parameters.isEmpty()) {
            ASTNodeImpl paramsNode = new ASTNodeImpl("Parameters", null);
            for (Param param : parameters) {
                paramsNode.addChild(param.toASTNode());
            }
            node.addChild(paramsNode);
        }

        if(!body.getStatements().isEmpty()) {
            ASTNodeImpl bodyNode = new ASTNodeImpl("Body", null);
            bodyNode.addChild(body.toASTNode());
            node.addChild(bodyNode);
        }

        return node;
    }
}
