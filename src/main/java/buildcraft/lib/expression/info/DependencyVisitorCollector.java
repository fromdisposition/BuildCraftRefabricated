/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.info;

import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import java.util.HashSet;
import java.util.Set;

public class DependencyVisitorCollector extends DependencyVisitorRouting {
   private boolean allConstant = true;
   private boolean needsUnkown = false;
   private final Set<IExpressionNode> mutableNodes;

   private DependencyVisitorCollector(Set<IExpressionNode> mutableNodes) {
      this.mutableNodes = mutableNodes;
   }

   public static DependencyVisitorCollector createConstantSearch() {
      return new DependencyVisitorCollector(null);
   }

   public static DependencyVisitorCollector createFullSearch() {
      return new DependencyVisitorCollector(new HashSet<>());
   }

   public static boolean testIsConstant(IDependantNode... node) {
      DependencyVisitorCollector search = createConstantSearch();
      search.dependOn(node);
      return search.areAllConstant();
   }

   public static Set<IExpressionNode> searchMutableNodes(IDependantNode... nodes) {
      DependencyVisitorCollector search = createFullSearch();
      search.dependOn(nodes);
      return search.getMutableNodes();
   }

   @Override
   protected boolean visit(IExpressionNode node) {
      if (node instanceof IConstantNode) {
         return true;
      }

      this.allConstant = false;
      if (this.mutableNodes == null) {
         return false;
      }

      this.mutableNodes.add(node);
      return true;
   }

   @Override
   public void dependOnUnknown() {
      this.needsUnkown = true;
   }

   public boolean areAllConstant() {
      return this.allConstant;
   }

   public boolean needsUnkown() {
      return this.needsUnkown;
   }

   public Set<IExpressionNode> getMutableNodes() {
      if (this.mutableNodes == null) {
         throw new IllegalStateException("Attempted to get a list of all mutable nodes when this object was constructed from #createConstantSearch()!");
      } else {
         return this.mutableNodes;
      }
   }
}
