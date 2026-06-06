package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.node.value.NodeConstantObject;

public class NodeCastToString implements IExpressionNode.INodeObject<String>, IDependantNode {
   private final IExpressionNode from;

   public NodeCastToString(IExpressionNode from) {
      this.from = from;
   }

   @Override
   public Class<String> getType() {
      return String.class;
   }

   public String evaluate() {
      return this.from.evaluateAsString();
   }

   @Override
   public IExpressionNode.INodeObject<String> inline() {
      return NodeInliningHelper.tryInline(this, this.from, NodeCastToString::new, f -> new NodeConstantObject<>(String.class, f.evaluateAsString()));
   }

   @Override
   public void visitDependants(IDependancyVisitor visitor) {
      visitor.dependOn(this.from);
   }

   @Override
   public String toString() {
      return "_to_string(" + this.from + ")";
   }
}
