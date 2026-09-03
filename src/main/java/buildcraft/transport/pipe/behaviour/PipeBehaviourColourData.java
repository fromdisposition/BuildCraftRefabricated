/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.transport.pipe.IPipeEventBus;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.statements.ActionPipeColor;
import java.util.Collections;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.HitResult;

public final class PipeBehaviourColourData {
   private DyeColor colour = DyeColor.WHITE;

   public DyeColor getColour() {
      return this.colour;
   }

   public void setColour(DyeColor colour) {
      this.colour = colour;
   }

   public void readFromNbt(CompoundTag nbt) {
      DyeColor read = NBTUtilBC.readEnum(nbt.get("colour"), DyeColor.class);
      if (read != null) {
         this.colour = read;
      }
   }

   public void writeToNbt(CompoundTag nbt) {
      nbt.put("colour", NBTUtilBC.writeEnum(this.colour));
   }

   public void writePayload(FriendlyByteBuf buffer) {
      buffer.writeByte(this.colour.getId());
   }

   public void readPayload(FriendlyByteBuf buffer) {
      this.colour = DyeColor.byId(buffer.readUnsignedByte());
   }

   public boolean onPipeActivate(Player player, HitResult trace, IPipeHolder holder) {
      if (holder.getPipeWorld().isClientSide()) {
         return EntityUtil.getWrenchHand(player) != null;
      } else if (EntityUtil.getWrenchHand(player) != null) {
         EntityUtil.activateWrench(player, trace);
         return this.cycleOnWrench(player, IPipeHolder.PipeMessageReceiver.BEHAVIOUR, holder);
      } else {
         return false;
      }
   }

   public void registerPaintActions(IPipeEventBus bus, PipeBehaviour behaviour) {
      bus.on(PipeEventStatement.AddActionInternal.class, behaviour, event -> Collections.addAll(event.actions, BCTransportStatements.ACTION_PIPE_COLOUR));
      bus.on(PipeEventActionActivate.class, behaviour, event -> {
         if (event.action instanceof ActionPipeColor action && this.colour != action.color) {
            this.colour = action.color;
            behaviour.pipe.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
            behaviour.pipe.getHolder().scheduleRenderUpdate();
         }
      });
   }

   public boolean cycleOnWrench(Player player, IPipeHolder.PipeMessageReceiver receiver, IPipeHolder holder) {
      int n = this.colour.getId() + (player.isShiftKeyDown() ? 15 : 1);
      this.colour = DyeColor.byId(n & 15);
      holder.scheduleNetworkUpdate(receiver);
      holder.scheduleRenderUpdate();
      return true;
   }
}
