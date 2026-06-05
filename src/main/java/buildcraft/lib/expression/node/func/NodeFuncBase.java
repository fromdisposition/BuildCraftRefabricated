package buildcraft.lib.expression.node.func;

import javax.annotation.Nullable;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;

public abstract class NodeFuncBase implements INodeFunc {

    protected boolean canInline = true;

    @Nullable
    private String deprecationMessage;
    private INodeFunc deprecationRecomendation;

    public NodeFuncBase setNeverInline() {
        canInline = false;
        return this;
    }

    public NodeFuncBase deprecate(String msg) {
        return deprecate(msg, null);
    }

    public NodeFuncBase deprecate(INodeFunc useInstead) {
        return deprecate(null, useInstead);
    }

    public NodeFuncBase deprecate(String msg, INodeFunc useInstead) {
        deprecationMessage = msg;
        deprecationRecomendation = useInstead;
        return this;
    }

    public boolean isDeprecated() {
        return deprecationMessage != null || deprecationRecomendation != null;
    }

    @Nullable
    public String getDeprecationMessage() {
        return deprecationMessage;
    }

    @Nullable
    public INodeFunc getDeprecationRecomendation() {
        return deprecationRecomendation;
    }

    public interface IFunctionNode {
        NodeFuncBase getFunction();
    }
}
