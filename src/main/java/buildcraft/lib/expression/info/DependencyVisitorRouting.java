/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.info;

import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;

public abstract class DependencyVisitorRouting implements IDependancyVisitor {
   private boolean openForVisiting = true;

   protected void reopen() {
      this.openForVisiting = true;
   }

   protected void close() {
      this.openForVisiting = false;
   }

   public boolean isOpenForVisiting() {
      return this.openForVisiting;
   }

   protected abstract boolean visit(IExpressionNode var1);

   private boolean visitPotentialDependantNode(IExpressionNode node) {
      if (node instanceof IDependantNode) {
         this.dependOn((IDependantNode)node);
         return this.isOpenForVisiting();
      } else {
         return this.visit(node);
      }
   }

   @Override
   public void dependOnExplictly(IExpressionNode node) {
      this.visit(node);
   }

   @Override
   public void dependOn(IExpressionNode node) {
      if (this.isOpenForVisiting()) {
         this.visitPotentialDependantNode(node);
      }
   }

   @Override
   public void dependOn(IExpressionNode... nodes) {
      if (this.isOpenForVisiting()) {
         for (IExpressionNode node : nodes) {
            if (!this.visitPotentialDependantNode(node)) {
               this.close();
               return;
            }
         }
      }
   }

   @Override
   public void dependOnNodes(Iterable<? extends IExpressionNode> nodes) {
      if (this.isOpenForVisiting()) {
         for (IExpressionNode node : nodes) {
            if (!this.visitPotentialDependantNode(node)) {
               this.close();
               return;
            }
         }
      }
   }

   private boolean visit(IDependantNode child) {
      child.visitDependants(this);
      return this.isOpenForVisiting();
   }

   @Override
   public void dependOn(IDependantNode child) {
      this.visit(child);
   }

   @Override
   public void dependOn(IDependantNode... children) {
      if (this.isOpenForVisiting()) {
         for (IDependantNode child : children) {
            if (!this.visit(child)) {
               return;
            }
         }
      }
   }

   @Override
   public void dependOnChildren(Iterable<? extends IDependantNode> children) {
      if (this.isOpenForVisiting()) {
         for (IDependantNode child : children) {
            if (!this.visit(child)) {
               return;
            }
         }
      }
   }
}
