package buildcraft.lib.expression.api;

public interface IDependantNode {
    void visitDependants(IDependancyVisitor visitor);
}
