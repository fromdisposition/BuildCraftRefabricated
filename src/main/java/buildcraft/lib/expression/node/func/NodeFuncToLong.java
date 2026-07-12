/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class NodeFuncToLong implements INodeFunc.INodeFuncLong, IExpressionNode.INodeLong {
   private final String name;
   private final NodeFuncToLong.IFuncToLong func;

   public NodeFuncToLong(String name, NodeFuncToLong.IFuncToLong func) {
      this.name = name;
      this.func = func;
   }

   @Override
   public long evaluate() {
      return this.func.apply();
   }

   @Override
   public IExpressionNode.INodeLong inline() {
      return this;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      return this;
   }

   @Override
   public String toString() {
      return "[ -> long] { " + this.name + " }";
   }

   public interface IFuncToLong {
      long apply();
   }
}
