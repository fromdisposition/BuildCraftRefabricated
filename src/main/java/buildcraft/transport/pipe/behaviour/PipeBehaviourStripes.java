/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.transport.IStripesActivator;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.transport.BCTransportStatements;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PipeBehaviourStripes extends PipeBehaviour implements IStripesActivator, IMjRedstoneReceiver {
   private final MjBattery battery = new MjBattery(256L * MjAPI.MJ);
   @Nullable
   public Direction direction = null;
   private long progress;

   public PipeBehaviourStripes(IPipe pipe) {
      super(pipe);
   }

   public PipeBehaviourStripes(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      this.battery.deserializeNBT(nbt.getCompoundOrEmpty("battery"));
      this.direction = NBTUtilBC.readEnum(nbt.get("direction"), Direction.class);
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      nbt.put("battery", this.battery.serializeNBT());
      if (this.direction != null) {
         nbt.put("direction", NBTUtilBC.writeEnum(this.direction));
      }

      return nbt;
   }

   @Override
   public void readFromNbt(CompoundTag nbt) {
      super.readFromNbt(nbt);
      this.battery.deserializeNBT(nbt.getCompoundOrEmpty("battery"));
      this.direction = NBTUtilBC.readEnum(nbt.get("direction"), Direction.class);
   }

   @Override
   public void readPayload(FriendlyByteBuf buffer, Object ctx) throws IOException {
      super.readPayload(buffer, ctx);
      int dirOrd = buffer.readByte();
      this.direction = dirOrd >= 0 && dirOrd < 6 ? Direction.values()[dirOrd] : null;
   }

   @Override
   public void writePayload(FriendlyByteBuf buffer) {
      super.writePayload(buffer);
      buffer.writeByte(this.direction == null ? -1 : this.direction.ordinal());
   }

   private void setDirection(@Nullable Direction newValue) {
      if (this.direction != newValue) {
         this.direction = newValue;
         if (!this.pipe.getHolder().getPipeWorld().isClientSide()) {
            this.pipe.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
            this.pipe.getHolder().scheduleRenderUpdate();
         }
      }
   }

   @PipeEventHandler
   public void addInternalActions(PipeEventStatement.AddActionInternal event) {
      for (Direction face : Direction.values()) {
         if (!this.pipe.isConnected(face)) {
            PipePluggable plug = this.pipe.getHolder().getPluggable(face);
            if (plug == null || !plug.isBlocking()) {
               event.actions.add(BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]);
            }
         }
      }
   }

   @PipeEventHandler
   public void onActionActivate(PipeEventActionActivate event) {
      for (Direction face : Direction.values()) {
         if (event.action == BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]) {
            this.setDirection(face);
         }
      }
   }

   @Override
   public boolean canConnect(@Nonnull IMjConnector other) {
      return true;
   }

   @Override
   public long getPowerRequested() {
      return this.battery.getCapacity() - this.battery.getStored();
   }

   @Override
   public long receivePower(long microJoules, boolean simulate) {
      return this.battery.addPowerChecking(microJoules, simulate);
   }

   @Override
   public boolean canConnect(Direction face, PipeBehaviour other) {
      return !(other instanceof PipeBehaviourStripes);
   }

   @Override
   public boolean hasSimulationWork() {
      if (this.pipe.getHolder().getPipeWorld().isClientSide()) {
         return false;
      }

      if (this.progress > 0L) {
         return true;
      }

      if (this.direction == null) {
         return false;
      }

      Level world = this.pipe.getHolder().getPipeWorld();
      BlockPos offset = this.pipe.getHolder().getPipePos().relative(this.direction);
      return BlockUtil.computeBlockBreakPower(world, offset) > 0L;
   }

   @Override
   public void onTick() {
      Level world = this.pipe.getHolder().getPipeWorld();
      BlockPos pos = this.pipe.getHolder().getPipePos();
      if (!world.isClientSide()) {
         if (this.direction == null || this.pipe.isConnected(this.direction)) {
            int sides = 0;
            Direction dir = null;

            for (Direction face : Direction.values()) {
               if (this.pipe.isConnected(face)) {
                  sides++;
                  dir = face;
               }
            }

            if (sides == 1) {
               this.setDirection(dir.getOpposite());
            } else {
               this.setDirection(null);
            }
         }

         this.battery.tick(world, this.pipe.getHolder().getPipePos());
         if (this.direction != null) {
            BlockPos offset = pos.relative(this.direction);
            long target = BlockUtil.computeBlockBreakPower(world, offset);
            if (target > 0L) {
               if (world instanceof ServerLevel serverLevel && !BlockUtil.canMachineBreak(serverLevel, offset, this.pipe.getHolder().getOwner())) {
                  this.progress = 0L;
                  return;
               }

               int offsetHash = offset.hashCode();
               if (this.progress < target) {
                  this.progress = this.progress + this.battery.extractPower(0L, Math.min(target - this.progress, MjAPI.MJ * 10L));
                  if (this.progress > 0L) {
                     world.destroyBlockProgress(offsetHash, offset, (int)(this.progress * 9L / target));
                  }
               } else {
                  BlockUtil.breakBlockAndGetDropsWithXp((ServerLevel)world, offset, new ItemStack(Items.DIAMOND_PICKAXE), this.pipe.getHolder().getOwner())
                     .ifPresent(result -> {
                        result.drops().forEach(stack -> this.sendItem(stack, this.direction));
                        if (result.xp() > 0) {
                           ExperienceOrb.award((ServerLevel)world, Vec3.atCenterOf(offset), result.xp());
                        }
                     });
                  this.progress = 0L;
               }
            }
         } else {
            this.progress = 0L;
         }
      }
   }

   @PipeEventHandler
   public void onDrop(PipeEventItem.Drop event) {
      if (this.direction != null) {
         IPipeHolder holder = this.pipe.getHolder();
         Level world = holder.getPipeWorld();
         BlockPos pos = holder.getPipePos();
         if (!world.isClientSide() && world instanceof ServerLevel serverLevel) {
            ServerPlayer player = BuildCraftAPI.fakePlayerProvider.getFakePlayer(serverLevel, holder.getOwner(), pos);
            player.getInventory().clearContent();
            player.getInventory().setItem(player.getInventory().getSelectedSlot(), event.getStack());
            if (PipeApi.stripeRegistry != null && PipeApi.stripeRegistry.handleItem(world, pos, this.direction, event.getStack(), player, this)) {
               event.setStack(ItemStack.EMPTY);

               for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                  ItemStack stack = player.getInventory().removeItemNoUpdate(i);
                  if (!stack.isEmpty()) {
                     this.sendItem(stack, this.direction);
                  }
               }
            }
         }
      }
   }

   @Override
   public void dropItem(@Nonnull ItemStack stack, Direction direction) {
      InventoryUtil.drop(this.pipe.getHolder().getPipeWorld(), this.pipe.getHolder().getPipePos(), stack);
   }

   @Override
   public boolean sendItem(@Nonnull ItemStack stack, Direction from) {
      PipeFlow flow = this.pipe.getFlow();
      if (flow instanceof IFlowItems) {
         ((IFlowItems)flow).insertItemsForce(stack, from, null, 0.02);
         return true;
      } else {
         return false;
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T> T getCapability(@Nonnull Object capability, Direction facing) {
      if (capability == MjAPI.CAP_REDSTONE_RECEIVER) {
         return (T)this;
      } else if (capability == MjAPI.CAP_RECEIVER) {
         return (T)this;
      } else {
         return (T)(capability == MjAPI.CAP_CONNECTOR ? this : super.getCapability(capability, facing));
      }
   }
}
