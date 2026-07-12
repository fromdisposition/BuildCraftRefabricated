/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.contents;

public interface IContentsLeaf extends IContentsNode {
   @Override
   default int getSortIndex() {
      return 0;
   }

   @Override
   default void calcVisibility() {
   }

   @Override
   default void sort() {
   }

   @Override
   default void addChild(IContentsNode node) {
   }

   @Override
   default IContentsNode[] getVisibleChildren() {
      return new IContentsNode[0];
   }
}
