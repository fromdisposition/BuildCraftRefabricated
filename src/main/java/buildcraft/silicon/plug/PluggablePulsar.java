/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.misc.MathUtil;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconStatements;
import buildcraft.silicon.client.model.key.KeyPlugSimple;
import buildcraft.transport.BCTransportAttachments;
import java.io.IOException;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class PluggablePulsar extends PipePluggable {
   private static final int PULSE_STAGE = 20;
   private static final AABB[] BOXES = new AABB[6];
   private boolean manuallyEnabled = false;
   private int pulseStage = 0;
   private int gateEnabledTicks;
   private int gateSinglePulses;
   private boolean lastPulsing = false;
   private boolean isPulsing = false;
   private boolean autoEnabled = false;

   public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side) {
      super(definition, holder, side);
   }

   public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side, CompoundTag nbt) {
      super(definition, holder, side);
      this.manuallyEnabled = nbt.getBooleanOr("manuallyEnabled", false);
      this.gateEnabledTicks = nbt.getIntOr("gateEnabledTicks", 0);
      this.gateSinglePulses = nbt.getIntOr("gateSinglePulses", 0);
      this.pulseStage = MathUtil.clamp(nbt.getIntOr("pulseStage", 0), 0, 20);
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      nbt.putBoolean("manuallyEnabled", this.manuallyEnabled);
      nbt.putInt("gateEnabledTicks", this.gateEnabledTicks);
      nbt.putInt("gateSinglePulses", this.gateSinglePulses);
      nbt.putInt("pulseStage", this.pulseStage);
      return nbt;
   }

   public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
      super(definition, holder, side);
      this.readData(buffer);
   }

   @Override
   public void writeCreationPayload(FriendlyByteBuf buffer) {
      super.writeCreationPayload(buffer);
      this.writeData(buffer);
   }

   @Override
   public void readPayload(FriendlyByteBuf buffer, Object side, Object ctx) throws IOException {
      super.readPayload(buffer, side, ctx);
      this.readData(buffer);
      if (this.holder.getPipeWorld().isClientSide()) {
         this.holder.scheduleRenderUpdate();
      }
   }

   private void sendPulseStateUpdate() {
      if (!this.holder.getPipeWorld().isClientSide()) {
         this.holder.sendMessage(IPipeHolder.PipeMessageReceiver.PLUGGABLES[this.side.ordinal()], this::writeData);
      }
   }

   @Override
   public void writePayload(FriendlyByteBuf buffer, Object side) {
      super.writePayload(buffer, side);
      this.writeData(buffer);
   }

   private void writeData(FriendlyByteBuf buffer) {
      buffer.writeBoolean(this.isPulsing());
      buffer.writeBoolean(this.gateEnabledTicks > 0 || this.gateSinglePulses > 0);
      buffer.writeBoolean(this.manuallyEnabled);
   }

   private void readData(FriendlyByteBuf buffer) {
      this.isPulsing = buffer.readBoolean();
      this.autoEnabled = buffer.readBoolean();
      this.manuallyEnabled = buffer.readBoolean();
   }

   @Override
   public AABB getBoundingBox() {
      return BOXES[this.side.ordinal()];
   }

   @Override
   public boolean isBlocking() {
      return true;
   }

   @Override
   public ItemStack getPickStack() {
      return new ItemStack(BCSiliconItems.PLUG_PULSAR);
   }

   @Override
   public void onPlacedBy(Player player) {
      super.onPlacedBy(player);
      BCTransportAttachments.recordPluggablePlacement(player, BCTransportAttachments.PluggablesPlaced.Kind.PULSAR);
   }

   @Override
   public boolean needsTick() {
      return true;
   }

   @Override
   public void onTick() {
      if (this.holder.getPipeWorld().isClientSide()) {
         this.isPulsing = this.isPulsing();
         if (this.isPulsing) {
            this.pulseStage++;
            if (this.pulseStage == 20) {
               this.pulseStage = 0;
            }
         } else {
            this.pulseStage = 0;
         }
      } else {
         boolean isOn = this.isPulsing();
         if (isOn) {
            this.pulseStage++;
         } else {
            this.pulseStage = 0;
         }

         if (this.gateEnabledTicks > 0) {
            this.gateEnabledTicks--;
         }

         if (this.pulseStage == 20) {
            this.pulseStage = 0;
            if (this.holder.getPipe().getBehaviour() instanceof IMjRedstoneReceiver rsRec) {
               long power = MjAPI.MJ;
               if (this.gateSinglePulses > 0) {
                  long excess = rsRec.receivePower(power, true);
                  if (excess == 0L) {
                     rsRec.receivePower(power, false);
                  } else {
                     this.gateSinglePulses++;
                  }
               } else {
                  rsRec.receivePower(power, false);
               }
            }

            if (this.gateSinglePulses > 0) {
               this.gateSinglePulses--;
            }
         }

         if (isOn != this.lastPulsing) {
            this.lastPulsing = isOn;
            this.sendPulseStateUpdate();
         }
      }
   }

   @Override
   public boolean onPluggableActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ) {
      if (!this.holder.getPipeWorld().isClientSide()) {
         this.manuallyEnabled = !this.manuallyEnabled;
         this.holder
            .getPipeWorld()
            .playSound(null, this.holder.getPipePos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, this.manuallyEnabled ? 0.6F : 0.5F);
         this.sendPulseStateUpdate();
      }

      return true;
   }

   public void enablePulsar() {
      this.gateEnabledTicks = 10;
   }

   public void addSinglePulse() {
      this.gateSinglePulses++;
   }

   public boolean getIsPulsingClient() {
      return this.isPulsing;
   }

   public boolean getAutoEnabledClient() {
      return this.autoEnabled;
   }

   public boolean getManuallyEnabledClient() {
      return this.manuallyEnabled;
   }

   public int getPulseStageClient() {
      return this.pulseStage;
   }

   private boolean isPulsing() {
      return this.manuallyEnabled || this.gateEnabledTicks > 0 || this.gateSinglePulses > 0;
   }

   @PipeEventHandler
   public void onAddActions(PipeEventStatement.AddActionInternalSided event) {
      if (event.side == this.side) {
         event.actions.add(BCSiliconStatements.ACTION_PULSAR_CONSTANT);
         event.actions.add(BCSiliconStatements.ACTION_PULSAR_SINGLE);
      }
   }

   public KeyPlugSimple getModelRenderKey(Object layer) {
      if (layer == null) {
         return null;
      }

      String name = layer.toString().toLowerCase();
      return name.contains("cutout") ? new KeyPlugSimple("pulsar", this.isPulsing, layer, this.side) : null;
   }

   static {
      double ll = 0.125;
      double lu = 0.25;
      double ul = 0.75;
      double uu = 0.875;
      double min = 0.3125;
      double max = 0.6875;
      BOXES[Direction.DOWN.ordinal()] = new AABB(min, ll, min, max, lu, max);
      BOXES[Direction.UP.ordinal()] = new AABB(min, ul, min, max, uu, max);
      BOXES[Direction.NORTH.ordinal()] = new AABB(min, min, ll, max, max, lu);
      BOXES[Direction.SOUTH.ordinal()] = new AABB(min, min, ul, max, max, uu);
      BOXES[Direction.WEST.ordinal()] = new AABB(ll, min, min, lu, max, max);
      BOXES[Direction.EAST.ordinal()] = new AABB(ul, min, min, uu, max, max);
   }
}
