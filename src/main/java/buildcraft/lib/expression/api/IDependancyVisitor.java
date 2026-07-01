/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.api;

public interface IDependancyVisitor {
   void dependOn(IExpressionNode var1);

   void dependOn(IExpressionNode... var1);

   void dependOnNodes(Iterable<? extends IExpressionNode> var1);

   void dependOn(IDependantNode var1);

   void dependOn(IDependantNode... var1);

   void dependOnChildren(Iterable<? extends IDependantNode> var1);

   void dependOnExplictly(IExpressionNode var1);

   void dependOnUnknown();
}
