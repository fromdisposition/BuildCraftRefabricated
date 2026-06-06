package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;

public class NodeStateful implements ITickableNode.Source {
   public final String name;
   public final IVariableNode getter;
   public final IVariableNode variable;
   public final IVariableNode last;
   public final IVariableNode rounderValue;
   final IExpressionNode getterReal;
   final Class<?> nodeType;
   private IExpressionNode source;
   private IExpressionNode rounder;

   public NodeStateful(String name, Class<?> nodeType, NodeStateful.IGetterFunc func) throws InvalidExpressionException {
      this.name = name;
      this.nodeType = nodeType;
      this.variable = NodeTypes.makeVariableNode(nodeType, name);
      this.last = NodeTypes.makeVariableNode(nodeType, name);
      this.rounderValue = NodeTypes.makeVariableNode(nodeType, name);
      this.getter = NodeTypes.makeVariableNode(nodeType, name);
      this.getterReal = func.createGetter(this.variable, this.last);
   }

   @Override
   public void setSource(IExpressionNode source) {
      this.source = source;
   }

   public void setRounder(IExpressionNode rounder) throws InvalidExpressionException {
      this.rounder = NodeTypes.cast(rounder, this.nodeType);
   }

   public NodeStateful.Instance createTickable() {
      if (this.source == null) {
         throw new IllegalStateException("source has not been set yet!");
      } else {
         return new NodeStateful.Instance();
      }
   }

   public enum GetterType implements NodeStateful.IGetterFunc {
      USE_VAR {
         @Override
         public IExpressionNode createGetter(IVariableNode variable, IVariableNode last) throws InvalidExpressionException {
            return variable;
         }
      },
      USE_LAST {
         @Override
         public IExpressionNode createGetter(IVariableNode variable, IVariableNode last) throws InvalidExpressionException {
            return last;
         }
      },
      INTERPOLATE_PARTIAL_TICKS {
         @Override
         public IExpressionNode createGetter(IVariableNode variable, IVariableNode last) throws InvalidExpressionException {
            Class<?> type = NodeTypes.getType(variable);
            if (type == double.class) {
               IExpressionNode.INodeDouble v = (IExpressionNode.INodeDouble)variable;
               IExpressionNode.INodeDouble l = (IExpressionNode.INodeDouble)last;
               IExpressionNode.INodeDouble p = DefaultContexts.RENDER_PARTIAL_TICKS;
               FunctionContext ctx = new FunctionContext();
               ctx.putVariable("v", v);
               ctx.putVariable("l", l);
               ctx.putVariable("p", p);
               return GenericExpressionCompiler.compileExpressionDouble("l * (1 - p) + (v * p)", ctx);
            } else if (type == long.class) {
               IExpressionNode.INodeLong v = (IExpressionNode.INodeLong)variable;
               IExpressionNode.INodeLong l = (IExpressionNode.INodeLong)last;
               IExpressionNode.INodeDouble p = DefaultContexts.RENDER_PARTIAL_TICKS;
               FunctionContext ctx = new FunctionContext();
               ctx.putVariable("v", v);
               ctx.putVariable("l", l);
               ctx.putVariable("p", p);
               return GenericExpressionCompiler.compileExpressionLong("l + ( round( (v - l) * p ) )", ctx);
            } else {
               throw new InvalidExpressionException("Cannot create an interpolated value for " + type);
            }
         }
      };
   }

   public interface IGetterFunc {
      IExpressionNode createGetter(IVariableNode var1, IVariableNode var2) throws InvalidExpressionException;
   }

   public class Instance implements ITickableNode {
      public final IVariableNode storedVar = NodeTypes.makeVariableNode(NodeStateful.this.nodeType, NodeStateful.this.name);
      public final IVariableNode storedLast = NodeTypes.makeVariableNode(NodeStateful.this.nodeType, NodeStateful.this.name);

      private Instance() {
      }

      public NodeStateful getContainer() {
         return NodeStateful.this;
      }

      @Override
      public void refresh() {
         NodeStateful.this.last.set(this.storedLast);
         NodeStateful.this.variable.set(this.storedVar);
         NodeStateful.this.getter.set(NodeStateful.this.getterReal);
      }

      @Override
      public void tick() {
         this.refresh();
         this.storedLast.set(this.storedVar);
         this.storedVar.set(NodeStateful.this.source);
         if (NodeStateful.this.rounder != null) {
            NodeStateful.this.last.set(this.storedLast);
            NodeStateful.this.variable.set(this.storedVar);
            NodeStateful.this.rounderValue.set(NodeStateful.this.last);
            this.storedLast.set(NodeStateful.this.rounder);
            NodeStateful.this.rounderValue.set(NodeStateful.this.variable);
            this.storedVar.set(NodeStateful.this.rounder);
         }
      }
   }
}
