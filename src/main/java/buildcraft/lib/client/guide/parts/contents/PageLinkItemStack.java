/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;

public final class PageLinkItemStack extends PageLink {
   public final ItemStack stack;
   public final List<String> tooltip;
   public final String searchText;

   public static PageLinkItemStack create(boolean startVisible, ItemStack stack, ProfilerFiller prof) {
      prof.push("create_page_link");
      String displayName = stack.getHoverName().getString();
      List<String> tooltip = Collections.singletonList(displayName);
      String searchText = displayName.toLowerCase(Locale.ROOT);
      ISimpleDrawable icon = new GuiStack(stack);
      PageLine text = new PageLine(icon, icon, 2, displayName, true);
      prof.pop();
      return new PageLinkItemStack(text, startVisible, stack, tooltip, searchText);
   }

   private PageLinkItemStack(PageLine text, boolean startVisible, ItemStack stack, List<String> tooltip, String searchText) {
      super(text, startVisible);
      this.stack = stack;
      this.tooltip = tooltip;
      this.searchText = searchText;
   }

   @Override
   public String getSearchName() {
      return this.searchText;
   }

   @Override
   public List<String> getTooltip() {
      return this.tooltip.size() == 1 ? null : this.tooltip;
   }

   @Override
   public void appendTooltip(GuiGuide gui) {
      if (this.tooltip.size() > 1) {
         gui.tooltips.add(this.tooltip);
      }
   }

   @Override
   public GuidePageFactory getFactoryLink() {
      return GuideManager.INSTANCE.getPageFor(this.stack);
   }
}
