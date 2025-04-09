package compiler.Components.Semantic;

import java.util.ArrayList;
import java.util.List;

public class FunctionType extends VarType {
    private final VarType returnType;
    private final List<VarType> parameters;

    public FunctionType(VarType returnType, List<VarType> parameters) {
        super(TypeName.FUNCTION);

        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
    }

    public VarType getReturnType() {
        return returnType;
    }

    public List<VarType> getParameters() {
        return parameters;
    }

    public int getParametersCount() {
        return parameters.size();
    }
}
