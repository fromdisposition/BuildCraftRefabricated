/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.fabric.integration.rei;

import buildcraft.lib.gui.BcScreen;
import buildcraft.lib.gui.IBcMenu;
import buildcraft.lib.gui.slot.IPhantomSlot;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class BcReiGhostDrag implements DraggableStackVisitor<BcScreen<?>> {
   @Override
   public <R extends Screen> boolean isHandingScreen(R screen) {
      return screen instanceof BcScreen<?> bcScreen && bcScreen.getMenu() instanceof IBcMenu;
   }

   @Override
   public Stream<DraggableStackVisitor.BoundsProvider> getDraggableAcceptingBounds(DraggingContext<BcScreen<?>> context, DraggableStack stack) {
      if (!(stack.getStack().getValue() instanceof ItemStack)) {
         return Stream.empty();
      }

      List<Rectangle> areas = new ArrayList<>();
      forEachPhantomSlot(context.getScreen(), (slotIndex, area) -> areas.add(area));
      return areas.isEmpty() ? Stream.empty() : Stream.of(DraggableStackVisitor.BoundsProvider.ofRectangles(areas));
   }

   @Override
   public DraggedAcceptorResult acceptDraggedStack(DraggingContext<BcScreen<?>> context, DraggableStack stack) {
      Point pos = context.getCurrentPosition();
      if (pos == null || !(stack.getStack().getValue() instanceof ItemStack itemStack) || itemStack.isEmpty()) {
         return DraggedAcceptorResult.PASS;
      }

      BcScreen<?> screen = context.getScreen();
      var hit = new Object() {
         boolean accepted;
      };
      forEachPhantomSlot(screen, (slotIndex, area) -> {
         if (!hit.accepted && area.contains(pos)) {
            String itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
            ((IBcMenu)screen.getMenu()).sendMessage(101, buf -> {
               buf.writeShort(slotIndex);
               buf.writeUtf(itemId);
            });
            hit.accepted = true;
         }
      });
      return hit.accepted ? DraggedAcceptorResult.ACCEPTED : DraggedAcceptorResult.PASS;
   }

   private static void forEachPhantomSlot(BcScreen<?> screen, PhantomSlotConsumer consumer) {
      List<Slot> slots = screen.getMenu().slots;
      for (int i = 0; i < slots.size(); i++) {
         if (slots.get(i) instanceof IPhantomSlot) {
            Slot slot = slots.get(i);
            consumer.accept(i, new Rectangle(screen.getGuiLeftPos() + slot.x - 1, screen.getGuiTopPos() + slot.y - 1, 18, 18));
         }
      }
   }

   @FunctionalInterface
   private interface PhantomSlotConsumer {
      void accept(int slotIndex, Rectangle area);
   }
}
