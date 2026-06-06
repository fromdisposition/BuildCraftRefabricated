package buildcraft.lib.expression.api;

public interface INodeFunc {
   IExpressionNode getNode(INodeStack var1) throws InvalidExpressionException;

   interface INodeFuncBoolean extends INodeFunc {
      IExpressionNode.INodeBoolean getNode(INodeStack var1) throws InvalidExpressionException;
   }

   interface INodeFuncDouble extends INodeFunc {
      IExpressionNode.INodeDouble getNode(INodeStack var1) throws InvalidExpressionException;
   }

   interface INodeFuncLong extends INodeFunc {
      IExpressionNode.INodeLong getNode(INodeStack var1) throws InvalidExpressionException;
   }

   interface INodeFuncObject<T> extends INodeFunc {
      IExpressionNode.INodeObject<T> getNode(INodeStack var1) throws InvalidExpressionException;

      Class<T> getType();
   }
}
