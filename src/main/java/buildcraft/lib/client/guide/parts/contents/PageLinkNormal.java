/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import java.util.List;
import net.minecraft.ChatFormatting;

public class PageLinkNormal extends PageLink {
   public final GuidePageFactory factory;
   public final List<String> tooltip;

   public PageLinkNormal(PageLine text, boolean startVisible, List<String> tooltip, GuidePageFactory factory) {
      this(text, startVisible, tooltip, factory, false);
   }

   public PageLinkNormal(PageLine text, boolean startVisible, List<String> tooltip, GuidePageFactory factory, boolean creativeOnly) {
      super(text, startVisible, creativeOnly);
      this.factory = factory;
      this.tooltip = tooltip != null && tooltip.size() == 1 && !strip(tooltip.get(0)).equals(strip(text.text)) ? tooltip : null;
   }

   private static String strip(String text) {
      return ChatFormatting.stripFormatting(text.trim());
   }

   @Override
   public List<String> getTooltip() {
      return this.tooltip;
   }

   @Override
   public GuidePageFactory getFactoryLink() {
      return this.factory;
   }
}
