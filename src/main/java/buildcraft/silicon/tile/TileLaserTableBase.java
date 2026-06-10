/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.tile;

import buildcraft.api.mj.ILaserTarget;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.lib.fabric.transfer.MjEnergyStorage;
import buildcraft.lib.fabric.transfer.MjPowerCell;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.misc.data.AverageLong;
import buildcraft.lib.tile.BcBlockEntity;
import org.jspecify.annotations.Nullable;
import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class TileLaserTableBase extends BcBlockEntity implements ILaserTarget, IDebuggable {
   private final AverageLong avgPower = new AverageLong(120);
   public long avgPowerClient;
   public long power;
   private long lastSyncedPower = -1L;

   protected TileLaserTableBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public abstract long getTarget();

   protected int getLaserBufferMultiplier() {
      return 1;
   }

   @Override
   public long getRequiredLaserPower() {
      long target = this.getTarget();
      if (target <= 0L) {
         return 0L;
      }

      long buffered = target * this.getLaserBufferMultiplier();
      return Math.max(0L, buffered - this.power);
   }

   @Override
   public long receiveLaserPower(long microJoules) {
      long received = Math.min(microJoules, this.getRequiredLaserPower());
      this.power += received;
      this.avgPower.push(received);
      return microJoules - received;
   }

   @Override
   public boolean isInvalidTarget() {
      return this.isRemoved();
   }

   public void serverTick() {
      this.avgPower.tick();
      this.avgPowerClient = (long)this.avgPower.getAverage();
      if (this.getTarget() <= 0L) {
         this.avgPower.clear();
      }

      if (this.power != this.lastSyncedPower) {
         this.lastSyncedPower = this.power;
         this.setChanged();
         if (this.getLevel() != null) {
            MessageUtil.sendUpdateToTrackingPlayers(this);
         }
      }
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putLong("power", this.power);
      output.putLong("avg_power", this.avgPowerClient);
      output.store("items", CompoundTag.CODEC, this.itemManager.serializeNBT());
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.power = input.getLongOr("power", 0L);
      this.avgPowerClient = input.getLongOr("avg_power", 0L);
      input.read("items", CompoundTag.CODEC).ifPresent(tag -> this.itemManager.deserializeNBT(tag));
   }

   @Override
   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public @Nullable MjEnergyStorage getSidedEnergyStorage() {
      return MjEnergyStorage.createIfRfEnabled(this.createEnergyCell());
   }

   private MjPowerCell createEnergyCell() {
      return new MjPowerCell() {
         @Override
         public long getStored() {
            return TileLaserTableBase.this.power;
         }

         @Override
         public void setStored(long microJoules) {
            long capacity = this.getCapacity();
            TileLaserTableBase.this.power = capacity <= 0L ? 0L : Math.max(0L, Math.min(microJoules, capacity));
            TileLaserTableBase.this.setChanged();
         }

         @Override
         public long getCapacity() {
            long target = TileLaserTableBase.this.getTarget();
            return target <= 0L ? 0L : target * TileLaserTableBase.this.getLaserBufferMultiplier();
         }

         @Override
         public long addPower(long microJoules, boolean simulate) {
            long room = this.getCapacity() - this.getStored();
            if (room <= 0L) {
               return microJoules;
            }

            long accepted = Math.min(microJoules, room);
            if (!simulate) {
               TileLaserTableBase.this.power += accepted;
               TileLaserTableBase.this.avgPower.push(accepted);
               TileLaserTableBase.this.setChanged();
            }

            return microJoules - accepted;
         }

         @Override
         public long extractPower(long min, long max) {
            if (TileLaserTableBase.this.power < min) {
               return 0L;
            }

            long extracting = Math.min(TileLaserTableBase.this.power, max);
            TileLaserTableBase.this.power -= extracting;
            TileLaserTableBase.this.setChanged();
            return extracting;
         }
      };
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("power = " + this.power);
      left.add("target = " + this.getTarget());
   }

   protected boolean extract(ItemHandlerSimple inv, Collection<IngredientStack> items, boolean simulate, boolean precise) {
      long remainingStacks = 0L;
      if (precise) {
         for (ItemStack stack : inv.stacks) {
            if (!stack.isEmpty()) {
               remainingStacks++;
            }
         }
      }

      for (IngredientStack definition : items) {
         int remaining = definition.count;

         for (int i = 0; i < inv.getSlots() && remaining > 0; i++) {
            ItemStack slotStack = inv.getStackInSlot(i);
            if (!slotStack.isEmpty() && definition.ingredient.test(slotStack)) {
               int spend = Math.min(remaining, slotStack.getCount());
               remaining -= spend;
               if (!simulate) {
                  slotStack = slotStack.copy();
                  slotStack.setCount(slotStack.getCount() - spend);
                  inv.setStackInSlot(i, slotStack);
               }
            }
         }

         if (remaining != 0) {
            return false;
         }

         remainingStacks--;
      }

      return !precise || remainingStacks == 0L;
   }
}
