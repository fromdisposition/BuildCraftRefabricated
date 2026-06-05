package buildcraft.lib.expression.info;

import java.util.HashSet;
import java.util.Set;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;

public class DependencyVisitorCollector extends DependencyVisitorRouting {

    private boolean allConstant = true;
    private boolean needsUnkown = false;
    private final Set<IExpressionNode> mutableNodes;

    private DependencyVisitorCollector(Set<IExpressionNode> mutableNodes) {
        this.mutableNodes = mutableNodes;
    }

    public static DependencyVisitorCollector createConstantSearch() {
        return new DependencyVisitorCollector(null);
    }

    public static DependencyVisitorCollector createFullSearch() {
        return new DependencyVisitorCollector(new HashSet<>());
    }

    public static boolean testIsConstant(IDependantNode... node) {
        DependencyVisitorCollector search = createConstantSearch();
        search.dependOn(node);
        return search.areAllConstant();
    }

    public static Set<IExpressionNode> searchMutableNodes(IDependantNode... nodes) {
        DependencyVisitorCollector search = createFullSearch();
        search.dependOn(nodes);
        return search.getMutableNodes();
    }

    @Override
    protected boolean visit(IExpressionNode node) {
        if (node instanceof IConstantNode) {

            return true;
        } else {
            allConstant = false;
            if (mutableNodes == null) {
                return false;
            }
            mutableNodes.add(node);
        }
        return true;
    }

    @Override
    public void dependOnUnknown() {
        needsUnkown = true;
    }

    public boolean areAllConstant() {
        return allConstant;
    }

    public boolean needsUnkown() {
        return needsUnkown;
    }

    public Set<IExpressionNode> getMutableNodes() {
        if (mutableNodes == null) {
            throw new IllegalStateException(
                "Attempted to get a list of all mutable nodes when this object was constructed from #createConstantSearch()!");
        }
        return mutableNodes;
    }
}
