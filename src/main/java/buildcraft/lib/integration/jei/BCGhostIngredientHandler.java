/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.integration.jei;

import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.IBcMenu;
import buildcraft.lib.gui.slot.IPhantomSlot;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BCGhostIngredientHandler<T extends BcScreen<?>> implements IGhostIngredientHandler<T> {
   public <I> List<Target<I>> getTargetsTyped(T gui, ITypedIngredient<I> ingredient, boolean doStart) {
      List<Target<I>> targets = new ArrayList<>();
      if (!(ingredient.getIngredient() instanceof ItemStack)) {
         return targets;
      }

      AbstractContainerMenu menu = gui.getMenu();
      if (!(menu instanceof IBcMenu ibcMenu)) {
         return targets;
      }

      for (int i = 0; i < menu.slots.size(); i++) {
         Slot slot = (Slot)menu.slots.get(i);
         if (slot instanceof IPhantomSlot) {
            int slotIndex = i;
            int x = gui.getGuiLeftPos() + slot.x;
            int y = gui.getGuiTopPos() + slot.y;
            targets.add(new BCGhostIngredientHandler.PhantomSlotTarget<>(ibcMenu, slotIndex, x, y));
         }
      }

      return targets;
   }

   public void onComplete() {
   }

   private static class PhantomSlotTarget<I> implements Target<I> {
      private final IBcMenu container;
      private final int slotIndex;
      private final Rect2i area;

      PhantomSlotTarget(IBcMenu container, int slotIndex, int x, int y) {
         this.container = container;
         this.slotIndex = slotIndex;
         this.area = new Rect2i(x, y, 16, 16);
      }

      public Rect2i getArea() {
         return this.area;
      }

      public void accept(I ingredient) {
         if (ingredient instanceof ItemStack stack) {
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            this.container.sendMessage(101, buf -> {
               buf.writeShort(this.slotIndex);
               buf.writeUtf(itemId);
            });
         }
      }
   }
}
