package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantObject;

public class NodeFuncGenericToObject<T> extends NodeFuncGeneric implements INodeFunc.INodeFuncObject<T> {
   protected final IExpressionNode.INodeObject<T> node;

   public NodeFuncGenericToObject(IExpressionNode.INodeObject<T> node, Class<?>[] types, IVariableNode[] nodes) {
      super(node, types, nodes);
      this.node = node;
   }

   @Override
   public IExpressionNode.INodeObject<T> getNode(INodeStack stack) throws InvalidExpressionException {
      return new FuncObject(this.popArgs(stack));
   }

   @Override
   public Class<T> getType() {
      return this.node.getType();
   }

   protected class FuncObject extends NodeFuncGeneric.Func implements IExpressionNode.INodeObject<T> {
      public FuncObject(IExpressionNode[] argsIn) {
         super(argsIn);
      }

      @Override
      public Class<T> getType() {
         return NodeFuncGenericToObject.this.node.getType();
      }

      @Override
      public T evaluate() {
         this.setupEvaluate(this.realArgs);
         return NodeFuncGenericToObject.this.node.evaluate();
      }

      @Override
      public IExpressionNode.INodeObject<T> inline() {
         IExpressionNode[] newArgs = new IExpressionNode[this.realArgs.length];
         NodeFuncGeneric.InlineType type = this.setupInline(newArgs);
         if (type == NodeFuncGeneric.InlineType.FULL) {
            this.setupEvaluate(newArgs);
            return new NodeConstantObject<>(this.getType(), NodeFuncGenericToObject.this.node.evaluate());
         } else {
            return type == NodeFuncGeneric.InlineType.PARTIAL ? NodeFuncGenericToObject.this.new FuncObject(newArgs) : this;
         }
      }
   }
}
