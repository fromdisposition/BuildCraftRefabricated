/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IFlowPowerLike;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventPower;
import buildcraft.api.transport.pipe.PipeEventRedstoneFlux;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import buildcraft.transport.statements.ActionPowerLimit;
import java.io.IOException;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

public class PipeBehaviourLimiter extends PipeBehaviour {
   public static final int MAX_SHIFT = 6;
   private int limitShift = 0;

   public PipeBehaviourLimiter(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourLimiter(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      this.limitShift = MathUtil.clamp(nbt.getIntOr("limitShift", 0), 0, 6);
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      nbt.putInt("limitShift", this.limitShift);
      return nbt;
   }

   @Override
   public void readFromNbt(CompoundTag nbt) {
      super.readFromNbt(nbt);
      this.limitShift = MathUtil.clamp(nbt.getIntOr("limitShift", 0), 0, 6);
   }

   @Override
   public void readPayload(FriendlyByteBuf buffer, Object side) throws IOException {
      this.limitShift = buffer.readUnsignedByte();
   }

   public void writePayload(FriendlyByteBuf buffer, Object side) {
      buffer.writeByte(this.limitShift);
   }

   @PipeEventHandler
   public void configurePower(PipeEventPower.Configure event) {
      if (this.limitShift == 6) {
         event.disableTransfer();
      } else {
         event.setMaxPower(event.getMaxPower() >> this.limitShift);
      }
   }

   @PipeEventHandler
   public void configurePower(PipeEventRedstoneFlux.Configure event) {
      if (this.limitShift == 6) {
         event.disableTransfer();
      } else {
         event.setMaxPower(event.getMaxPower() >> this.limitShift);
      }
   }

   @PipeEventHandler
   public void onActionActivate(PipeEventActionActivate event) {
      if (event.action instanceof ActionPowerLimit) {
         this.limitShift = ((ActionPowerLimit)event.action).limitShift;
         this.requestReconfigure();
      }
   }

   @Override
   public boolean onPipeActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      if (EntityUtil.getWrenchHand(player) == null) {
         return false;
      }

      if (!player.level().isClientSide()) {
         EntityUtil.activateWrench(player, trace);
         this.limitShift++;
         if (this.limitShift > 6) {
            this.limitShift = 0;
         }

         boolean isRf = this.pipe.getFlow() instanceof PipeFlowRedstoneFlux;
         int limit;
         if (this.limitShift == 6) {
            limit = 0;
         } else if (isRf) {
            PipeApi.RedstoneFluxTransferInfo transferInfo = PipeApi.getRfTransferInfo(this.pipe.getDefinition());
            limit = transferInfo.transferPerTick >> this.limitShift;
         } else {
            PipeApi.PowerTransferInfo transferInfo = PipeApi.getPowerTransferInfo(this.pipe.getDefinition());
            limit = (int)((transferInfo.transferPerTick >> this.limitShift) / MjAPI.MJ);
         }

         String limitLabel = isRf ? LocaleUtil.localizeRfFlow(limit) : LocaleUtil.localizeMjFlow(limit * MjAPI.MJ);
         MessageUtil.sendOverlayMessage(player, Component.translatable("chat.pipe.power.limit.mode", limitLabel));
         this.requestReconfigure();
      }

      return true;
   }

   private void requestReconfigure() {
      if (this.pipe.getFlow() instanceof IFlowPowerLike) {
         ((IFlowPowerLike)this.pipe.getFlow()).reconfigure();
         this.pipe.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
      }
   }

   @Override
   public int getTextureIndex(Direction face) {
      return 6 - this.limitShift;
   }
}
