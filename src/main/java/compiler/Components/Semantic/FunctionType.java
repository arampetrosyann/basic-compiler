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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionType that = (FunctionType) o;
        return this.returnType.equals(that.returnType) &&
                this.parameters.equals(that.parameters); // order matters
    }

    @Override
    public int hashCode() {
        int result = returnType.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }

}
