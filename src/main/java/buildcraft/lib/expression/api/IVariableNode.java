package buildcraft.lib.expression.api;

public interface IVariableNode extends IExpressionNode {
   void set(IExpressionNode var1);

   void setConstant(boolean var1);

   boolean isConstant();

   interface IVariableNodeBoolean extends IVariableNode, IExpressionNode.INodeBoolean {
      void set(boolean var1);

      @Override
      default void set(IExpressionNode from) {
         this.set(((IExpressionNode.INodeBoolean)from).evaluate());
      }
   }

   interface IVariableNodeDouble extends IVariableNode, IExpressionNode.INodeDouble {
      void set(double var1);

      @Override
      default void set(IExpressionNode from) {
         this.set(((IExpressionNode.INodeDouble)from).evaluate());
      }
   }

   interface IVariableNodeLong extends IVariableNode, IExpressionNode.INodeLong {
      void set(long var1);

      @Override
      default void set(IExpressionNode from) {
         this.set(((IExpressionNode.INodeLong)from).evaluate());
      }
   }

   interface IVariableNodeObject<T> extends IVariableNode, IExpressionNode.INodeObject<T> {
      void set(T var1);

      default void setUnchecked(Object to) {
         if (to.getClass() != this.getType()) {
            throw new ClassCastException(to.getClass() + " cannot be cast to " + this.getType());
         }

         this.set((T)to);
      }

      @Override
      default void set(IExpressionNode from) {
         this.setUnchecked(((IExpressionNode.INodeObject)from).evaluate());
      }
   }
}
