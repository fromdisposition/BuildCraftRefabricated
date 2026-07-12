/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;
import buildcraft.lib.expression.info.DependencyVisitorCollector;
import java.util.HashSet;
import java.util.Set;

public abstract class NodeFuncGeneric implements INodeFunc {
   private final IExpressionNode node;
   protected final Class<?>[] types;
   protected final IVariableNode[] variables;

   public NodeFuncGeneric(IExpressionNode node, Class<?>[] types, IVariableNode[] nodes) {
      this.node = node;
      this.types = types;
      this.variables = nodes;
      if (types.length != nodes.length) {
         throw new IllegalArgumentException("Lengths did not match! (" + types.length + " vs " + nodes.length + ")");
      }

      for (int i = 0; i < types.length; i++) {
         Class<?> givenType = types[i];
         if (NodeTypes.getType(nodes[i]) != givenType) {
            throw new IllegalArgumentException("Types did not match! (given " + givenType + ", node is " + nodes[i].getClass() + ")");
         }
      }
   }

   protected IExpressionNode[] popArgs(INodeStack stack) throws InvalidExpressionException {
      IExpressionNode[] nodes = new IExpressionNode[this.types.length];

      for (int i = this.types.length; i > 0; i--) {
         nodes[i - 1] = stack.pop(this.types[i - 1]);
      }

      return nodes;
   }

   @Override
   public String toString() {
      return "somefunc(" + this.node.toString() + ")";
   }

   protected abstract class Func implements IExpressionNode, IDependantNode {
      protected final IExpressionNode[] realArgs;

      public Func(IExpressionNode[] argsIn) {
         this.realArgs = argsIn;
      }

      protected void setupEvaluate(IExpressionNode[] nodes) {
         for (int i = 0; i < nodes.length; i++) {
            NodeFuncGeneric.this.variables[i].set(nodes[i]);
         }
      }

      protected NodeFuncGeneric.InlineType setupInline(IExpressionNode[] nodes) {
         NodeFuncGeneric.InlineType type = NodeFuncGeneric.InlineType.FULL;
         if (NodeFuncGeneric.this.node instanceof IDependantNode) {
            DependencyVisitorCollector c = DependencyVisitorCollector.createFullSearch();
            ((IDependantNode)NodeFuncGeneric.this.node).visitDependants(c);
            if (c.needsUnkown()) {
               type = NodeFuncGeneric.InlineType.PARTIAL;
            } else {
               Set<IExpressionNode> set = new HashSet<>(c.getMutableNodes());

               for (IExpressionNode n : this.realArgs) {
                  set.remove(n);
               }

               if (!set.isEmpty()) {
                  type = NodeFuncGeneric.InlineType.PARTIAL;
               }
            }
         }

         for (int i = 0; i < this.realArgs.length; i++) {
            IExpressionNode bef = this.realArgs[i];
            IExpressionNode aft = bef.inline();
            nodes[i] = aft;
            type = type.and(bef, aft);
         }

         return type;
      }

      protected String getArgsToString() {
         StringBuilder total = new StringBuilder("[");

         for (int i = 0; i < this.realArgs.length; i++) {
            if (i > 0) {
               total.append(", (");
            } else {
               total.append(" (");
            }

            total.append(this.realArgs[i].toString()).append(") ");
         }

         return total + "]";
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         visitor.dependOn(this.realArgs);
      }

      @Override
      public String toString() {
         return "[" + this.getArgsToString() + " -> generic]";
      }
   }

   public enum InlineType {
      NONE,
      PARTIAL,
      FULL;

      public NodeFuncGeneric.InlineType and(IExpressionNode before, IExpressionNode after) {
         if (this == PARTIAL) {
            return PARTIAL;
         } else if (this == NONE) {
            return before == after ? NONE : PARTIAL;
         } else {
            return after instanceof IConstantNode ? FULL : PARTIAL;
         }
      }
   }
}
