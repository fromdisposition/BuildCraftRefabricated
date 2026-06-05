package buildcraft.lib.expression.api;

public interface IDependancyVisitor {

    void dependOn(IExpressionNode node);

    void dependOn(IExpressionNode... nodes);

    void dependOnNodes(Iterable<? extends IExpressionNode> nodes);

    void dependOn(IDependantNode child);

    void dependOn(IDependantNode... children);

    void dependOnChildren(Iterable<? extends IDependantNode> children);

    void dependOnExplictly(IExpressionNode node);

    void dependOnUnknown();
}
