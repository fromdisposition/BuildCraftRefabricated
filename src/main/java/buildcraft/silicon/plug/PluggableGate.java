/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.api.transport.IWireEmitter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.fabric.menu.GateMenuKey;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.model.key.KeyPlugGate;
import buildcraft.silicon.container.ContainerGate;
import buildcraft.silicon.gate.GateLogic;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.ItemGateCopier;
import buildcraft.transport.BCTransportAttachments;
import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class PluggableGate extends PipePluggable implements IWireEmitter {
   private static final byte ID_UPDATE_PLUG = 1;
   private static final AABB[] BOXES = new AABB[6];
   private static final Identifier ADVANCEMENT_PLACE_GATE = Identifier.parse("buildcrafttransport:pipe_logic");
   private static final Identifier ADVANCEMENT_PLACE_ADV_GATE = Identifier.parse("buildcrafttransport:extended_logic");
   public final GateLogic logic;
   public final ModelVariableData clientModelData = new ModelVariableData();

   public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, GateVariant variant) {
      super(def, holder, side);
      this.logic = new GateLogic(this, variant);
   }

   public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt) {
      super(def, holder, side);
      this.logic = new GateLogic(this, nbt.getCompound("data").orElse(new CompoundTag()));
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      nbt.put("data", this.logic.writeToNbt());
      return nbt;
   }

   @Override
   public boolean readFromNbt(CompoundTag nbt) {
      CompoundTag data = nbt.getCompound("data").orElse(new CompoundTag());
      this.logic.readConfigData(data);
      return true;
   }

   @Override
   public CompoundTag writeClientUpdateData() {
      return this.logic.writeClientState();
   }

   @Override
   public void readClientUpdateData(CompoundTag nbt) {
      this.logic.readClientState(nbt);
   }

   public PluggableGate(PluggableDefinition def, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
      super(def, holder, side);
      PacketBufferBC packetBuffer = BcPayloadBuffers.ensure(buffer);
      this.logic = new GateLogic(this, packetBuffer);
   }

   @Override
   public void writeCreationPayload(FriendlyByteBuf buffer) {
      super.writeCreationPayload(buffer);
      PacketBufferBC packetBuffer = BcPayloadBuffers.ensure(buffer);
      this.logic.writeCreationToBuf(packetBuffer);
   }

   public void sendMessage(IPayloadWriter writer) {
      IPipeHolder.PipeMessageReceiver to = IPipeHolder.PipeMessageReceiver.PLUGGABLES[this.side.ordinal()];
      this.holder.sendMessage(to, buffer -> {
         buffer.writeByte(1);
         writer.write(buffer);
      });
   }

   public void sendGuiMessage(IPayloadWriter writer) {
      IPipeHolder.PipeMessageReceiver to = IPipeHolder.PipeMessageReceiver.PLUGGABLES[this.side.ordinal()];
      this.holder.sendGuiMessage(to, buffer -> {
         buffer.writeByte(1);
         writer.write(buffer);
      });
   }

   @Override
   public void writePayload(FriendlyByteBuf buffer, Object side) {
      throw new Error("All messages must have an ID, and we can't just write a payload directly!");
   }

   @Override
   public void readPayload(FriendlyByteBuf b, Object side, Object ctx) throws IOException {
      PacketBufferBC packetBuffer = BcPayloadBuffers.ensure(b);
      byte id = packetBuffer.readByte();
      if (id == 1) {
         this.logic.readPayload(packetBuffer, (Boolean)ctx);
      }
   }

   @Override
   public AABB getBoundingBox() {
      return BOXES[this.side.get3DDataValue()];
   }

   @Override
   public boolean isBlocking() {
      return true;
   }

   @Override
   public ItemStack getPickStack() {
      return BCSiliconItems.PLUG_GATE.getStack(this.logic.variant);
   }

   @Override
   public PluggableModelKey getModelRenderKey(Object layer) {
      return "cutout".equals(layer) ? new KeyPlugGate(this.side, this.logic.variant, this.logic.isOn) : null;
   }

   @Override
   public void onPlacedBy(Player player) {
      super.onPlacedBy(player);
      if (!this.holder.getPipeWorld().isClientSide()) {
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_GATE);
         if (this.logic.variant.numActionArgs >= 1) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_ADV_GATE);
         }
      }

      BCTransportAttachments.recordPluggablePlacement(player, BCTransportAttachments.PluggablesPlaced.Kind.GATE);
   }

   @Override
   public boolean onPluggableActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ) {
      if (!player.level().isClientSide()) {
         if (this.interactWithCopier(player, player.getMainHandItem())) {
            return true;
         }

         if (this.interactWithCopier(player, player.getOffhandItem())) {
            return true;
         }

         BlockPos pos = this.holder.getPipePos();
         if (player instanceof ServerPlayer serverPlayer) {
            final GateMenuKey key = new GateMenuKey(pos, this.side);
            serverPlayer.openMenu(new ExtendedMenuProvider<Object>() {
               public GateMenuKey getScreenOpeningData(ServerPlayer sp) {
                  return key;
               }

               public Component getDisplayName() {
                  return PluggableGate.this.logic.variant.getLocalizedName();
               }

               public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                  return new ContainerGate(id, inv, PluggableGate.this);
               }
            });
         }
      }

      return true;
   }

   private boolean interactWithCopier(Player player, ItemStack stack) {
      if (!(stack.getItem() instanceof ItemGateCopier)) {
         return false;
      }

      CompoundTag stored = ItemGateCopier.getCopiedGateData(stack);
      if (stored != null) {
         this.logic.readConfigData(stored);
         if (this.holder instanceof BlockEntity be) {
            be.setChanged();
         }

         this.holder.scheduleRenderUpdate();
         this.logic.sendResolveData();
         MessageUtil.sendOverlayMessage(player, Component.translatable("chat.gateCopier.gatePasted"));
      } else {
         if (!this.logic.hasConfiguration()) {
            MessageUtil.sendOverlayMessage(player, Component.translatable("chat.gateCopier.noInformation"));
            return false;
         }

         CompoundTag data = this.logic.writeToNbt();
         data.remove("wireBroadcasts");
         ItemGateCopier.setCopiedGateData(stack, data);
         MessageUtil.sendOverlayMessage(player, Component.translatable("chat.gateCopier.gateCopied"));
      }

      return true;
   }

   @Override
   public boolean isEmitting(DyeColor colour) {
      return this.logic.isEmitting(colour);
   }

   @Override
   public void emitWire(DyeColor colour) {
      this.logic.emitWire(colour);
   }

   @Override
   public boolean needsTick() {
      return this.logic.needsPeriodicTick();
   }

   @Override
   public void onTick() {
      this.logic.onTick();
      if (this.holder.getPipeWorld().isClientSide()) {
         this.clientModelData.tick();
      }
   }

   @Override
   public boolean canConnectToRedstone(@Nullable Direction to) {
      return true;
   }

   static {
      double ll = 0.125;
      double lu = 0.25;
      double ul = 0.75;
      double uu = 0.875;
      double min = 0.3125;
      double max = 0.6875;
      BOXES[Direction.DOWN.get3DDataValue()] = new AABB(min, ll, min, max, lu, max);
      BOXES[Direction.UP.get3DDataValue()] = new AABB(min, ul, min, max, uu, max);
      BOXES[Direction.NORTH.get3DDataValue()] = new AABB(min, min, ll, max, max, lu);
      BOXES[Direction.SOUTH.get3DDataValue()] = new AABB(min, min, ul, max, max, uu);
      BOXES[Direction.WEST.get3DDataValue()] = new AABB(ll, min, min, lu, max, max);
      BOXES[Direction.EAST.get3DDataValue()] = new AABB(ul, min, min, uu, max, max);
   }
}
