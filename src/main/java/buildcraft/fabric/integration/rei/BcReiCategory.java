/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.fabric.integration.rei;

import buildcraft.lib.client.fluid.FluidGuiRenderer;
import buildcraft.lib.gui.BCGraphics;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

final class BcReiCategory<D extends BcReiDisplay> implements DisplayCategory<D> {
   private static final int TEXT_LIGHT = 0xFF404040;
   private static final int TEXT_DARK = 0xFFBBBBBB;

   interface SlotLayout<D> {
      void addWidgets(D display, Point origin, List<Widget> widgets);
   }

   private final CategoryIdentifier<D> id;
   private final Renderer icon;
   private final Component title;
   private final int displayWidth;
   private final int displayHeight;
   private final SlotLayout<D> layout;

   BcReiCategory(CategoryIdentifier<D> id, ItemLike icon, String titleKey, int displayWidth, int displayHeight, SlotLayout<D> layout) {
      this.id = id;
      this.icon = EntryStacks.of(new ItemStack(icon));
      this.title = Component.translatable(titleKey);
      this.displayWidth = displayWidth;
      this.displayHeight = displayHeight;
      this.layout = layout;
   }

   @Override
   public CategoryIdentifier<? extends D> getCategoryIdentifier() {
      return this.id;
   }

   @Override
   public Renderer getIcon() {
      return this.icon;
   }

   @Override
   public Component getTitle() {
      return this.title;
   }

   @Override
   public int getDisplayWidth(D display) {
      return this.displayWidth;
   }

   @Override
   public int getDisplayHeight() {
      return this.displayHeight;
   }

   @Override
   public List<Widget> setupDisplay(D display, Rectangle bounds) {
      List<Widget> widgets = new ArrayList<>();
      widgets.add(Widgets.createRecipeBase(bounds));
      this.layout.addWidgets(display, new Point(bounds.getX() + 5, bounds.getY() + 5), widgets);
      return widgets;
   }

   static Widget texture(Identifier tex, Point o, int u, int v, int w, int h) {
      return Widgets.createTexturedWidget(tex, o.x, o.y, u, v, w, h);
   }

   static Widget slot(EntryIngredient entries, Point o, int x, int y, boolean output) {
      var slot = Widgets.createSlot(new Point(o.x + x - 1, o.y + y - 1)).entries(entries);
      return output ? slot.markOutput() : slot.markInput();
   }

   static Widget texSlot(EntryIngredient entries, Point o, int x, int y, boolean output) {
      var slot = Widgets.createSlot(new Point(o.x + x - 1, o.y + y - 1)).entries(entries).disableBackground();
      return output ? slot.markOutput() : slot.markInput();
   }

   static void tank(List<Widget> widgets, EntryIngredient entries, Point o, int x, int y, int w, int h, boolean output) {
      if (!entries.isEmpty() && entries.get(0).getValue() instanceof dev.architectury.fluid.FluidStack fluid && !fluid.isEmpty()) {
         var bcStack = new buildcraft.lib.fluid.stack.FluidStack(fluid.getFluid(), (int)(fluid.getAmount() / BcRei.MB_TO_DROPLETS));
         widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) ->
            FluidGuiRenderer.drawFluidStack(new BCGraphics(graphics), o.x + x, o.y + y, w, h, bcStack)));
      }

      var slot = Widgets.createSlot(new Rectangle(o.x + x - 1, o.y + y - 1, w + 2, h + 2)).entries(entries).disableBackground();
      widgets.add(output ? slot.markOutput() : slot.markInput());
   }

   static Widget tankBase(Point o, int x, int y, int w, int h) {
      return Widgets.createSlotBase(new Rectangle(o.x + x - 1, o.y + y - 1, w + 2, h + 2));
   }

   static Widget textLeft(Point o, int x, int y, Component text) {
      return Widgets.createLabel(new Point(o.x + x, o.y + y), text).leftAligned().noShadow().color(TEXT_LIGHT, TEXT_DARK);
   }

   static Widget textRight(Point o, int alignWidth, int y, Component text) {
      return Widgets.createLabel(new Point(o.x + alignWidth - 2, o.y + y), text).rightAligned().noShadow().color(TEXT_LIGHT, TEXT_DARK);
   }
}
