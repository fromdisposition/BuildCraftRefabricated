/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.cast.NodeCasting;

public class GenericExpressionCompiler {
   public static IExpressionNode.INodeLong compileExpressionLong(String function) throws InvalidExpressionException {
      return compileExpressionLong(function, DefaultContexts.createWithAll());
   }

   public static IExpressionNode.INodeLong compileExpressionLong(String function, FunctionContext context) throws InvalidExpressionException {
      IExpressionNode node = InternalCompiler.compileExpression(function, context);
      if (node instanceof IExpressionNode.INodeLong) {
         return (IExpressionNode.INodeLong)node;
      } else {
         throw new InvalidExpressionException("Not a long " + node);
      }
   }

   public static INodeFunc.INodeFuncLong compileFunctionLong(String function, Argument... args) throws InvalidExpressionException {
      return compileFunctionLong(function, DefaultContexts.createWithAll(), args);
   }

   public static INodeFunc.INodeFuncLong compileFunctionLong(String function, FunctionContext context, Argument... args) throws InvalidExpressionException {
      INodeFunc func = InternalCompiler.compileFunction(function, context, args);
      if (func instanceof INodeFunc.INodeFuncLong) {
         return (INodeFunc.INodeFuncLong)func;
      } else {
         throw new InvalidExpressionException("Not a long " + func);
      }
   }

   public static IExpressionNode.INodeDouble compileExpressionDouble(String function) throws InvalidExpressionException {
      return compileExpressionDouble(function, DefaultContexts.createWithAll());
   }

   public static IExpressionNode.INodeDouble compileExpressionDouble(String function, FunctionContext context) throws InvalidExpressionException {
      return NodeCasting.castToDouble(InternalCompiler.compileExpression(function, context)).inline();
   }

   public static INodeFunc.INodeFuncDouble compileFunctionDouble(String function, Argument... args) throws InvalidExpressionException {
      return compileFunctionDouble(function, DefaultContexts.createWithAll(), args);
   }

   public static INodeFunc.INodeFuncDouble compileFunctionDouble(String function, FunctionContext context, Argument... args) throws InvalidExpressionException {
      return NodeCasting.castToDouble(InternalCompiler.compileFunction(function, context, args));
   }

   public static IExpressionNode.INodeBoolean compileExpressionBoolean(String function) throws InvalidExpressionException {
      return compileExpressionBoolean(function, DefaultContexts.createWithAll());
   }

   public static IExpressionNode.INodeBoolean compileExpressionBoolean(String function, FunctionContext context) throws InvalidExpressionException {
      IExpressionNode node = InternalCompiler.compileExpression(function, context);
      if (node instanceof IExpressionNode.INodeBoolean) {
         return (IExpressionNode.INodeBoolean)node;
      } else {
         throw new InvalidExpressionException("Not a boolean " + node);
      }
   }

   public static INodeFunc.INodeFuncBoolean compileFunctionBoolean(String function, Argument... args) throws InvalidExpressionException {
      return compileFunctionBoolean(function, DefaultContexts.createWithAll(), args);
   }

   public static INodeFunc.INodeFuncBoolean compileFunctionBoolean(String function, FunctionContext context, Argument... args) throws InvalidExpressionException {
      INodeFunc func = InternalCompiler.compileFunction(function, context, args);
      if (func instanceof INodeFunc.INodeFuncBoolean) {
         return (INodeFunc.INodeFuncBoolean)func;
      } else {
         throw new InvalidExpressionException("Not a boolean " + func);
      }
   }

   public static <T> IExpressionNode.INodeObject<T> compileExpressionObject(Class<T> clazz, String function) throws InvalidExpressionException {
      return compileExpressionObject(clazz, function, DefaultContexts.createWithAll());
   }

   public static <T> IExpressionNode.INodeObject<T> compileExpressionObject(Class<T> clazz, String function, FunctionContext context) throws InvalidExpressionException {
      return NodeCasting.castToObject(InternalCompiler.compileExpression(function, context), clazz);
   }

   public static <T> INodeFunc.INodeFuncObject<T> compileFunctionObject(Class<T> clazz, String function, Argument... args) throws InvalidExpressionException {
      return compileFunctionObject(clazz, function, DefaultContexts.createWithAll(), args);
   }

   public static <T> INodeFunc.INodeFuncObject<T> compileFunctionObject(Class<T> clazz, String function, FunctionContext context, Argument... args) throws InvalidExpressionException {
      return NodeCasting.castToObject(InternalCompiler.compileFunction(function, context, args), clazz);
   }

   public static IExpressionNode.INodeObject<String> compileExpressionString(String function) throws InvalidExpressionException {
      return compileExpressionString(function, DefaultContexts.createWithAll());
   }

   public static IExpressionNode.INodeObject<String> compileExpressionString(String function, FunctionContext context) throws InvalidExpressionException {
      return NodeCasting.castToString(InternalCompiler.compileExpression(function, context)).inline();
   }

   public static INodeFunc.INodeFuncObject<String> compileFunctionString(String function, Argument... args) throws InvalidExpressionException {
      return compileFunctionString(function, DefaultContexts.createWithAll(), args);
   }

   public static INodeFunc.INodeFuncObject<String> compileFunctionString(String function, FunctionContext context, Argument... args) throws InvalidExpressionException {
      return NodeCasting.castToString(InternalCompiler.compileFunction(function, context, args));
   }
}
