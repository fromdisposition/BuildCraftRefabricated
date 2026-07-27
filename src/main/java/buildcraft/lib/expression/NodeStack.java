/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.cast.NodeCasting;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class NodeStack implements INodeStack {
   private final List<IExpressionNode> stack = new ArrayList<>();
   private INodeFunc currentlyPopping;
   private List<Class<?>> recordingTypes;
   private int index = 0;

   public NodeStack() {
   }

   public NodeStack(IExpressionNode... nodes) {
      for (IExpressionNode node : nodes) {
         this.push(node);
      }
   }

   public <T extends IExpressionNode> T push(T node) {
      this.stack.add(node);
      ExpressionDebugManager.debugPrintln("Pushed " + node);
      return node;
   }

   public IExpressionNode pop() throws InvalidExpressionException {
      if (this.stack.isEmpty()) {
         throw new InvalidExpressionException("No more nodes to pop!");
      }

      IExpressionNode node = this.stack.remove(this.stack.size() - 1);
      ExpressionDebugManager.debugPrintln("Popped " + node);
      return node;
   }

   public IExpressionNode peek() throws InvalidExpressionException {
      if (this.stack.isEmpty()) {
         throw new InvalidExpressionException("No more nodes to peek!");
      } else {
         return this.stack.get(this.stack.size() - 1);
      }
   }

   public List<IExpressionNode> peek(int count) throws InvalidExpressionException {
      if (this.stack.size() < count) {
         throw new InvalidExpressionException("Not enough nodes to peek!");
      }

      List<IExpressionNode> nodes = new ArrayList<>(count);
      int i2 = this.stack.size() - 1;

      for (int i = count; i > 0; i--) {
         nodes.add(this.stack.get(i2));
         i2--;
      }

      return nodes;
   }

   public boolean isEmpty() {
      return this.stack.isEmpty();
   }

   public void setRecorder(List<Class<?>> expected, INodeFunc toTest) throws InvalidExpressionException {
      this.checkAndRemoveRecorder();
      ExpressionDebugManager.debugStart("Recording " + toTest + ", expecting " + expected);
      this.recordingTypes = new ArrayList<>(expected);
      this.currentlyPopping = toTest;
      this.index = 0;
   }

   public void checkAndRemoveRecorder() throws InvalidExpressionException {
      if (this.recordingTypes != null) {
         if (this.index != this.recordingTypes.size()) {
            throw new InvalidExpressionException(
               "Only removed " + this.recordingTypes.subList(0, this.index) + ", expected to remove " + this.recordingTypes + " for " + this.currentlyPopping
            );
         }

         ExpressionDebugManager.debugEnd("Record was correct");
         this.recordingTypes = null;
         this.currentlyPopping = null;
         this.index = 0;
      }
   }

   private void checkTypeMatch(Class<?> type) throws InvalidExpressionException {
      if (this.recordingTypes != null) {
         if (this.index >= this.recordingTypes.size()) {
            throw new InvalidExpressionException("Attempted to pop off " + type + ", but the function was not allowed to!");
         }

         Class<?> said = this.recordingTypes.get(this.index);
         if (said != type) {
            throw new InvalidExpressionException("Attempted to pop off " + type + ", but the function previously popped off !");
         }

         this.index++;
      }
   }

   @Override
   public String toString() {
      return this.stack.toString();
   }

   @Override
   public IExpressionNode.INodeLong popLong() throws InvalidExpressionException {
      this.checkTypeMatch(long.class);
      IExpressionNode node = this.pop();
      if (node instanceof IExpressionNode.INodeLong) {
         return (IExpressionNode.INodeLong)node;
      } else {
         throw new InvalidExpressionException("Cannot cast " + node + " to a long!");
      }
   }

   @Override
   public IExpressionNode.INodeDouble popDouble() throws InvalidExpressionException {
      this.checkTypeMatch(double.class);
      return NodeCasting.castToDouble(this.pop());
   }

   @Override
   public IExpressionNode.INodeBoolean popBoolean() throws InvalidExpressionException {
      this.checkTypeMatch(boolean.class);
      IExpressionNode node = this.pop();
      if (node instanceof IExpressionNode.INodeBoolean) {
         return (IExpressionNode.INodeBoolean)node;
      } else {
         throw new InvalidExpressionException("Cannot cast " + node + " to a boolean!");
      }
   }

   @Override
   public <T> IExpressionNode.INodeObject<T> popObject(Class<T> type) throws InvalidExpressionException {
      this.checkTypeMatch(type);
      IExpressionNode node = this.pop();
      if (node instanceof IExpressionNode.INodeObject<?> nodeObj) {
         if (nodeObj.getType() == type) {
            return (IExpressionNode.INodeObject<T>)nodeObj;
         } else {
            throw new InvalidExpressionException("Cannot cast " + nodeObj.getType().getSimpleName() + " to " + type.getSimpleName() + "!");
         }
      } else {
         throw new InvalidExpressionException("Cannot cast " + node + " to " + type.getSimpleName() + "!");
      }
   }
}
