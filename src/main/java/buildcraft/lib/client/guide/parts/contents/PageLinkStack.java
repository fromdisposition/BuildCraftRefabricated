/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.entry.FluidStackValueFilter;
import buildcraft.lib.client.guide.entry.PageEntryFluidStack;
import buildcraft.lib.client.guide.entry.PageValue;
import buildcraft.lib.client.guide.parts.GuidePage;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.gui.GuiFluid;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;

public final class PageLinkStack extends PageLink {
   private final List<String> tooltip;
   private final String searchText;
   private final Supplier<GuidePageFactory> factory;

   public static PageLinkStack item(boolean startVisible, ItemStack stack, ProfilerFiller prof) {
      return create("create_page_link", startVisible, stack.getHoverName().getString(), new GuiStack(stack), () -> GuideManager.INSTANCE.getPageFor(stack), prof);
   }

   public static PageLinkStack fluid(boolean startVisible, FluidStack stack, ProfilerFiller prof) {
      return create("create_page_link_fluid", startVisible, stack.getHoverName().getString(), new GuiFluid(stack), () -> {
         FluidStackValueFilter filter = new FluidStackValueFilter(stack);
         return g -> new GuidePage(g, ImmutableList.of(), new PageValue<>(PageEntryFluidStack.INSTANCE, filter));
      }, prof);
   }

   private static PageLinkStack create(
      String profilerKey, boolean startVisible, String displayName, ISimpleDrawable icon, Supplier<GuidePageFactory> factory, ProfilerFiller prof
   ) {
      prof.push(profilerKey);
      PageLine text = new PageLine(icon, icon, 2, displayName, true);
      prof.pop();
      return new PageLinkStack(text, startVisible, displayName, factory);
   }

   private PageLinkStack(PageLine text, boolean startVisible, String displayName, Supplier<GuidePageFactory> factory) {
      super(text, startVisible);
      this.tooltip = Collections.singletonList(displayName);
      this.searchText = displayName.toLowerCase(Locale.ROOT);
      this.factory = factory;
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
      return this.factory.get();
   }
}
