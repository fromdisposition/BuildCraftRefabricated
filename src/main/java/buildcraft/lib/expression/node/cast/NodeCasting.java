package buildcraft.lib.expression.node.cast;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.NodeStack;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import java.util.Collections;

public class NodeCasting {
   public static IExpressionNode.INodeObject<String> castToString(IExpressionNode node) {
      return node instanceof IExpressionNode.INodeObject && ((IExpressionNode.INodeObject)node).getType() == String.class
         ? (IExpressionNode.INodeObject)node
         : new NodeCastToString(node);
   }

   public static INodeFunc.INodeFuncObject<String> castToString(final INodeFunc func) {
      return func instanceof INodeFunc.INodeFuncObject && ((INodeFunc.INodeFuncObject)func).getType() == String.class
         ? (INodeFunc.INodeFuncObject)func
         : new INodeFunc.INodeFuncObject<String>() {
            @Override
            public Class<String> getType() {
               return String.class;
            }

            @Override
            public IExpressionNode.INodeObject<String> getNode(INodeStack stack) throws InvalidExpressionException {
               return new NodeCastToString(func.getNode(stack));
            }
         };
   }

   public static IExpressionNode.INodeDouble castToDouble(IExpressionNode node) throws InvalidExpressionException {
      if (node instanceof IExpressionNode.INodeDouble) {
         return (IExpressionNode.INodeDouble)node;
      } else {
         Class<?> type = NodeTypes.getType(node);
         FunctionContext ctx = NodeTypes.getContext(type);
         if (ctx == null) {
            throw new InvalidExpressionException("Cannot cast " + node + " to a double!");
         } else {
            INodeFunc func = ctx.getFunction("(double)", Collections.singletonList(type));
            if (func != null && NodeTypes.getType(func) == double.class) {
               return (IExpressionNode.INodeDouble)func.getNode(new NodeStack(node));
            } else {
               throw new InvalidExpressionException("Cannot cast " + node + " to a double!");
            }
         }
      }
   }

   public static INodeFunc.INodeFuncDouble castToDouble(INodeFunc func) throws InvalidExpressionException {
      if (func instanceof INodeFunc.INodeFuncDouble) {
         return (INodeFunc.INodeFuncDouble)func;
      } else {
         Class<?> type = NodeTypes.getType(func);
         FunctionContext ctx = NodeTypes.getContext(type);
         if (ctx == null) {
            throw new InvalidExpressionException("Cannot cast " + func + " to a double!");
         } else {
            INodeFunc caster = ctx.getFunction("(double)", Collections.singletonList(type));
            if (caster != null && NodeTypes.getType(caster) == double.class) {
               return stack -> (IExpressionNode.INodeDouble)caster.getNode(new NodeStack(func.getNode(stack)));
            } else {
               throw new InvalidExpressionException("Cannot cast " + func + " to a double!");
            }
         }
      }
   }

   public static IExpressionNode castToType(IExpressionNode node, Class<?> to) throws InvalidExpressionException {
      Class<?> from = NodeTypes.getType(node);
      if (from == to) {
         return node;
      }

      FunctionContext castingContext = new FunctionContext(NodeTypes.getContext(from), NodeTypes.getContext(to));
      INodeFunc caster = castingContext.getFunction("(" + NodeTypes.getName(to) + ")", Collections.singletonList(from));
      if (caster == null) {
         if (to == String.class) {
            return new NodeCastToString(node);
         } else {
            throw new InvalidExpressionException("Cannot cast from " + NodeTypes.getName(from) + " to " + NodeTypes.getName(to));
         }
      } else {
         NodeStack stack = new NodeStack(node);
         stack.setRecorder(Collections.singletonList(from), caster);
         IExpressionNode casted = caster.getNode(stack);
         stack.checkAndRemoveRecorder();
         Class<?> actual = NodeTypes.getType(casted);
         if (actual != to) {
            throw new IllegalStateException("The caster " + caster + " didn't produce the correct result! (Expected " + to + ", but got " + actual + ")");
         } else {
            return casted;
         }
      }
   }

   public static <T> IExpressionNode.INodeObject<T> castToObject(IExpressionNode node, Class<T> clazz) throws InvalidExpressionException {
      return (IExpressionNode.INodeObject<T>)castToType(node, clazz);
   }

   public static <T> INodeFunc.INodeFuncObject<T> castToObject(final INodeFunc func, final Class<T> to) throws InvalidExpressionException {
      Class<?> from = NodeTypes.getType(func);
      if (from == to) {
         return (INodeFunc.INodeFuncObject<T>)func;
      }

      FunctionContext castingContext = new FunctionContext(NodeTypes.getContext(from), NodeTypes.getContext(to));
      final INodeFunc caster = castingContext.getFunction("(" + NodeTypes.getName(to) + ")", Collections.singletonList(from));
      if (caster == null) {
         if (to == String.class) {
            return (INodeFunc.INodeFuncObject<T>)castToString(func);
         } else {
            throw new InvalidExpressionException("Cannot cast from " + NodeTypes.getName(from) + " to " + NodeTypes.getName(to));
         }
      } else {
         return new INodeFunc.INodeFuncObject<T>() {
            @Override
            public IExpressionNode.INodeObject<T> getNode(INodeStack stack) throws InvalidExpressionException {
               return (IExpressionNode.INodeObject<T>)caster.getNode(new NodeStack(func.getNode(stack)));
            }

            @Override
            public Class<T> getType() {
               return to;
            }
         };
      }
   }
}
