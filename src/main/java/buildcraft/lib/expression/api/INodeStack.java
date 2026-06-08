/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.api;

public interface INodeStack {
   IExpressionNode.INodeLong popLong() throws InvalidExpressionException;

   IExpressionNode.INodeDouble popDouble() throws InvalidExpressionException;

   IExpressionNode.INodeBoolean popBoolean() throws InvalidExpressionException;

   <T> IExpressionNode.INodeObject<T> popObject(Class<T> var1) throws InvalidExpressionException;

   default IExpressionNode pop(Class<?> type) throws InvalidExpressionException {
      if (type == long.class) {
         return this.popLong();
      } else if (type == double.class) {
         return this.popDouble();
      } else {
         return type == boolean.class ? this.popBoolean() : this.popObject(type);
      }
   }
}
