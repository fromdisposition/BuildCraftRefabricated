package buildcraft.lib.expression.api;

public interface IDependancyVisitor {
   void dependOn(IExpressionNode var1);

   void dependOn(IExpressionNode... var1);

   void dependOnNodes(Iterable<? extends IExpressionNode> var1);

   void dependOn(IDependantNode var1);

   void dependOn(IDependantNode... var1);

   void dependOnChildren(Iterable<? extends IDependantNode> var1);

   void dependOnExplictly(IExpressionNode var1);

   void dependOnUnknown();
}
