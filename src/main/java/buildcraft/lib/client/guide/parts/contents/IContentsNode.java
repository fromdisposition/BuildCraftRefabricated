/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import java.util.Set;

public interface IContentsNode {
   String getSearchName();

   int getSortIndex();

   boolean isVisible();

   void calcVisibility();

   void resetVisibility();

   void setVisible(Set<PageLink> var1);

   void sort();

   IContentsNode[] getVisibleChildren();

   void addChild(IContentsNode var1);

   GuidePart createGuidePart(GuiGuide var1);
}
