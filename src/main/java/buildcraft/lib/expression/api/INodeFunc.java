/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
