/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.Widget_Neptune;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import java.util.EnumMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

public class ContainerEmzuliPipe_BC8 extends AbstractPipeFilterContainer<PipeBehaviourEmzuli> {
   public final EnumMap<PipeBehaviourEmzuli.SlotIndex, ContainerEmzuliPipe_BC8.PaintWidget> paintWidgets = new EnumMap<>(PipeBehaviourEmzuli.SlotIndex.class);

   public ContainerEmzuliPipe_BC8(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, resolveBehaviour(playerInv, pos, PipeBehaviourEmzuli.class, "emzuli pipe"));
   }

   public ContainerEmzuliPipe_BC8(int containerId, Inventory playerInv, PipeBehaviourEmzuli behaviour) {
      super(BCTransportMenuTypes.EMZULI_PIPE, containerId, playerInv, behaviour, behaviour == null ? null : behaviour.pipe.getHolder());
      if (behaviour == null) {
         this.addFullPlayerInventory(8, 84);
      } else {
         this.addSlot(new SlotPhantom(behaviour.invFilters, 0, 25, 21));
         this.addSlot(new SlotPhantom(behaviour.invFilters, 1, 25, 49));
         this.addSlot(new SlotPhantom(behaviour.invFilters, 2, 134, 21));
         this.addSlot(new SlotPhantom(behaviour.invFilters, 3, 134, 49));
         this.addFullPlayerInventory(8, 84);

         for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
            ContainerEmzuliPipe_BC8.PaintWidget widget = new ContainerEmzuliPipe_BC8.PaintWidget(this, index);
            this.addWidget(widget);
            this.paintWidgets.put(index, widget);
         }
      }
   }

   public static class PaintWidget extends Widget_Neptune<ContainerEmzuliPipe_BC8> {
      public final PipeBehaviourEmzuli.SlotIndex index;

      public PaintWidget(ContainerEmzuliPipe_BC8 container, PipeBehaviourEmzuli.SlotIndex index) {
         super(container);
         this.index = index;
      }

      public void setColour(DyeColor colour) {
         this.sendWidgetData(buffer -> buffer.writeByte(colour == null ? -1 : colour.getId()));
      }

      @Override
      public void handleWidgetDataServer(BCPayloadContext ctx, FriendlyByteBuf buffer) {
         int c = buffer.readByte();
         DyeColor colour = c >= 0 && c < 16 ? DyeColor.byId(c) : null;
         if (colour == null) {
            this.container.behaviour.slotColours.remove(this.index);
         } else {
            this.container.behaviour.slotColours.put(this.index, colour);
         }

         this.container.behaviour.pipe.getHolder().scheduleNetworkGuiUpdate(buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
      }
   }
}
