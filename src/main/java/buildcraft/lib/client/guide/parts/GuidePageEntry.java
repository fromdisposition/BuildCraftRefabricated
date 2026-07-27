/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.entry.PageEntry;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;

public class GuidePageEntry extends GuidePage {
   public final Identifier name;

   public GuidePageEntry(GuiGuide gui, List<GuidePart> parts, PageEntry<?> entry, Identifier name) {
      super(gui, parts, entry);
      this.name = name;
   }

   @Nullable
   @Override
   public GuidePageBase createReloaded() {
      GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(this.name);
      if (factory == null) {
         return null;
      }

      GuidePageBase page = factory.createNew(this.gui);
      page.goToPage(this.getPage());
      return page;
   }
}
