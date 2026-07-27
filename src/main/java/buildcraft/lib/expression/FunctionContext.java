/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncToDouble;
import buildcraft.lib.expression.node.func.NodeFuncToLong;
import buildcraft.lib.expression.node.func.NodeFuncToObject;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeConstantLong;
import buildcraft.lib.expression.node.value.NodeConstantObject;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class FunctionContext extends FunctionContextBase {
   public static final String FUNCTION_ARG_SEPARATOR = "@";
   public final String name;
   private final FunctionContext[] parents;
   private final Map<String, IExpressionNode> variables = new HashMap<>();
   private final Map<String, Map<List<Class<?>>, INodeFunc>> functions = new HashMap<>();

   public FunctionContext() {
      this("");
   }

   public FunctionContext(FunctionContext parent) {
      this("", parent);
   }

   public FunctionContext(FunctionContext... parents) {
      this("", parents);
   }

   public FunctionContext(String name) {
      this.name = name;
      this.parents = new FunctionContext[0];
   }

   public FunctionContext(String name, FunctionContext parent) {
      this.name = name;
      this.parents = new FunctionContext[]{parent};
   }

   public FunctionContext(String name, FunctionContext... parents) {
      this.name = name;
      this.parents = (FunctionContext[])parents.clone();
   }

   public FunctionContext[] getParents() {
      return this.parents;
   }

   public IExpressionNode getVariable(String name) {
      name = name.toLowerCase(Locale.ROOT);
      IExpressionNode current = this.variables.get(name);
      if (current != null) {
         return current;
      }

      for (FunctionContext parent : this.parents) {
         IExpressionNode node = parent.getVariable(name);
         if (node != null) {
            return node;
         }
      }

      INodeFunc func = this.getFunction(name, Collections.emptyList());
      if (func != null) {
         try {
            return func.getNode(new NodeStack());
         } catch (InvalidExpressionException e) {
            throw new IllegalStateException("Found a 0-args function that didn't allow us to get a node for it!", e);
         }
      } else {
         return null;
      }
   }

   public boolean hasLocalVariable(String name) {
      name = name.toLowerCase(Locale.ROOT);
      return this.variables.containsKey(name);
   }

   public <E extends IExpressionNode> E putVariable(String name, E node) {
      name = name.toLowerCase(Locale.ROOT);
      if (NodeTypes.getType(name) != null) {
         throw new IllegalArgumentException(
            "Cannot add a variable that clashes with a type! (Name = '" + name + "', Types = " + NodeTypes.getValidTypeNames() + ")"
         );
      }

      this.variables.put(name, node);
      return node;
   }

   public IVariableNode putVariable(String name, Class<?> type) {
      if (type == boolean.class) {
         return this.putVariableBoolean(name);
      } else if (type == long.class) {
         return this.putVariableLong(name);
      } else {
         return type == double.class ? this.putVariableDouble(name) : this.putVariableObject(name, type);
      }
   }

   public NodeVariableLong putVariableLong(String name) {
      NodeVariableLong node = new NodeVariableLong(name);
      return this.putVariable(name, node);
   }

   public NodeVariableDouble putVariableDouble(String name) {
      NodeVariableDouble node = new NodeVariableDouble(name);
      return this.putVariable(name, node);
   }

   public NodeVariableBoolean putVariableBoolean(String name) {
      NodeVariableBoolean node = new NodeVariableBoolean(name);
      return this.putVariable(name, node);
   }

   public NodeVariableObject<String> putVariableString(String name) {
      return this.putVariableObject(name, String.class);
   }

   public <T> NodeVariableObject<T> putVariableObject(String name, Class<T> type) {
      NodeVariableObject<T> node = new NodeVariableObject<>(name, type);
      return this.putVariable(name, node);
   }

   public void putConstantLong(final String name, final long value) {
      this.putVariable(name, new IExpressionNode.INodeLong() {
         @Override
         public String toString() {
            return name + " = " + value + "L";
         }

         @Override
         public long evaluate() {
            return value;
         }

         @Override
         public IExpressionNode.INodeLong inline() {
            return new NodeConstantLong(value);
         }
      });
   }

   public void putConstantDouble(final String name, final double value) {
      this.putVariable(name, new IExpressionNode.INodeDouble() {
         @Override
         public String toString() {
            return name + " = " + value + "D";
         }

         @Override
         public double evaluate() {
            return value;
         }

         @Override
         public IExpressionNode.INodeDouble inline() {
            return new NodeConstantDouble(value);
         }
      });
   }

   public void putConstantBoolean(final String name, final boolean value) {
      this.putVariable(name, new IExpressionNode.INodeBoolean() {
         @Override
         public boolean evaluate() {
            return value;
         }

         @Override
         public IExpressionNode.INodeBoolean inline() {
            return NodeConstantBoolean.of(value);
         }

         @Override
         public String toString() {
            return name + " = " + value;
         }
      });
   }

   public <T> void putConstant(final String name, final Class<T> type, final T value) {
      this.putVariable(name, new IExpressionNode.INodeObject<T>() {
         @Override
         public T evaluate() {
            return value;
         }

         @Override
         public Class<T> getType() {
            return type;
         }

         @Override
         public IExpressionNode.INodeObject<T> inline() {
            return new NodeConstantObject<>(type, value);
         }

         @Override
         public String toString() {
            return name + " = " + value;
         }
      });
   }

   public void putParsedConstant(String name, String value) {
      if (InternalCompiler.isValidLong(value)) {
         this.putConstantLong(name, InternalCompiler.parseValidLong(value));
      } else if (InternalCompiler.isValidDouble(value)) {
         this.putConstantDouble(name, Double.parseDouble(value));
      } else if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
         this.putConstant(name, String.class, value);
      } else {
         this.putConstantBoolean(name, "true".equalsIgnoreCase(value));
      }
   }

   public Set<String> getAllVariables() {
      return this.variables.keySet();
   }

   public INodeFunc getFunction(String name, List<Class<?>> args) {
      Map<List<Class<?>>, INodeFunc> map = this.functions.get(name);
      if (map != null) {
         INodeFunc func = map.get(args);
         if (func != null) {
            return func;
         }
      }

      for (FunctionContext parent : this.parents) {
         INodeFunc func = parent.getFunction(name, args);
         if (func != null) {
            return func;
         }
      }

      return null;
   }

   public Map<List<Class<?>>, INodeFunc> getFunctions(String name) {
      name = name.toLowerCase(Locale.ROOT);
      Map<List<Class<?>>, INodeFunc> map = new HashMap<>();
      this.getFunctions0(name, map);
      return map;
   }

   public Map<String, Map<List<Class<?>>, INodeFunc>> getAllFunctions() {
      return this.functions;
   }

   private void getFunctions0(String name, Map<List<Class<?>>, INodeFunc> map) {
      for (FunctionContext parent : this.parents) {
         parent.getFunctions0(name, map);
      }

      Map<List<Class<?>>, INodeFunc> all = this.functions.get(name);
      if (all != null) {
         map.putAll(all);
      }
   }

   private static List<Class<?>> getArgTypes(INodeFunc function) {
      NodeStackRecording recorder = new NodeStackRecording();

      try {
         function.getNode(recorder);
      } catch (InvalidExpressionException e) {
         throw new IllegalStateException("This should never happen!", e);
      }

      List<Class<?>> types = new ArrayList<>(recorder.types);
      Collections.reverse(types);
      return types;
   }

   @Override
   public <F extends INodeFunc> F putFunction(String name, F function) {
      name = name.toLowerCase(Locale.ROOT);
      Map<List<Class<?>>, INodeFunc> map = this.functions.computeIfAbsent(name, k -> new HashMap<>());
      map.put(getArgTypes(function), function);
      return function;
   }

   public INodeFunc.INodeFuncLong put_l(String name, NodeFuncToLong.IFuncToLong func) {
      return this.putFunction(name, new NodeFuncToLong(name, func));
   }

   public INodeFunc.INodeFuncDouble put_d(String name, NodeFuncToDouble.IFuncToDouble func) {
      return this.putFunction(name, new NodeFuncToDouble(name, func));
   }

   public INodeFunc.INodeFuncBoolean put_b(String name, NodeFuncToBoolean.IFuncToBoolean func) {
      return this.putFunction(name, new NodeFuncToBoolean(name, func));
   }

   public INodeFunc.INodeFuncObject<String> put_s(String name, Supplier<String> func) {
      return this.put_o(name, String.class, func);
   }

   public <T> INodeFunc.INodeFuncObject<T> put_o(String name, Class<T> type, Supplier<T> func) {
      return this.putFunction(name, new NodeFuncToObject<>(name, type, func));
   }

   public INodeFunc.INodeFuncObject<String> put_l_s(String name, NodeFuncLongToObject.IFuncLongToObject<String> func) {
      return this.put_l_o(name, String.class, func);
   }

   public INodeFunc.INodeFuncObject<String> put_d_s(String name, NodeFuncDoubleToObject.IFuncDoubleToObject<String> func) {
      return this.put_d_o(name, String.class, func);
   }

   public INodeFunc.INodeFuncBoolean put_s_b(String name, NodeFuncObjectToBoolean.IFuncObjectToBoolean<String> func) {
      return this.put_o_b(name, String.class, func);
   }

   public INodeFunc.INodeFuncLong put_s_l(String name, NodeFuncObjectToLong.IFuncObjectToLong<String> func) {
      return this.put_o_l(name, String.class, func);
   }

   public INodeFunc.INodeFuncLong put_sl_l(String name, NodeFuncObjectLongToLong.IFuncObjectLongToLong<String> func) {
      return this.put_ol_l(name, String.class, func);
   }

   public INodeFunc.INodeFuncLong put_sl_l(String name, NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong<String> func) {
      return this.put_oll_l(name, String.class, func);
   }

   public <A> INodeFunc.INodeFuncObject<String> put_o_s(String name, Class<A> argA, NodeFuncObjectToObject.IFuncObjectToObject<A, String> func) {
      return this.put_o_o(name, argA, String.class, func);
   }

   public INodeFunc.INodeFuncObject<String> put_s_s(String name, NodeFuncObjectToObject.IFuncObjectToObject<String, String> func) {
      return this.put_o_o(name, String.class, String.class, func);
   }

   public INodeFunc.INodeFuncObject<String> put_ss_s(String name, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<String, String, String> func) {
      return this.put_oo_o(name, String.class, String.class, String.class, func);
   }
}
