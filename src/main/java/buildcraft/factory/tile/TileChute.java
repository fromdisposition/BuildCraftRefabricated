package buildcraft.factory.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.block.BlockChute;
import buildcraft.factory.container.ContainerChute;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.MjEnergyStorage;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.mj.MjBatteryReceiver;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.lib.transfer.neighbor.NeighborTransfers;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class TileChute extends BcBlockEntity implements MenuProvider, BlockEntityExtendedMenu {
   private static final int PICKUP_MAX = 3;
   private static final long PROGRESS_TARGET = 100000L;
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftfactory:retired_hopper");
   public final ItemHandlerSimple inv;
   private final MjBattery battery = new MjBattery(1L * MjAPI.MJ);
   private final IMjReceiver mjReceiver = new MjBatteryReceiver(this.battery);
   private int progress = 0;

   public TileChute(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.CHUTE, pos, state);
      this.inv = this.itemManager.addInvHandler("inv", 4, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
   }

   public IMjReceiver getMjReceiver() {
      return this.mjReceiver;
   }

   public MjBattery getBattery() {
      return this.battery;
   }

   @Nullable
   public MjEnergyStorage getSidedEnergyStorage() {
      return MjEnergyStorage.createIfRfEnabled(this.battery);
   }

   public ItemHandlerSimple getInv() {
      return this.inv;
   }

   public void serverTick() {
      if (this.level != null && !this.level.isClientSide()) {
         if (this.level.getBlockState(this.worldPosition).getBlock() instanceof BlockChute) {
            this.battery.tick(this.getLevel(), this.getBlockPos());
            Direction currentSide = (Direction)this.level.getBlockState(this.worldPosition).getValue(BlockChute.FACING);
            long target = 100000L;
            if (currentSide == Direction.UP) {
               this.progress += 1000;
            }

            this.progress = this.progress + (int)this.battery.extractPower(0L, target - this.progress);
            if (this.progress >= target) {
               this.progress = 0;
               this.pickupItems(currentSide);
            }

            this.putInNearInventories(currentSide);
         }
      }
   }

   private void pickupItems(Direction currentSide) {
      AABB aabb = createPickupBox(this.worldPosition, currentSide);
      int count = 3;

      for (ItemEntity entity : this.level.getEntitiesOfClass(ItemEntity.class, aabb, Entity::isAlive)) {
         ItemStack stack = entity.getItem();
         if (!stack.isEmpty()) {
            ItemStack remaining = stack.copy();

            for (int slot = 0; slot < this.inv.getSlots() && !remaining.isEmpty() && count > 0; slot++) {
               int before = remaining.getCount();
               remaining = this.inv.insertItem(slot, remaining, false);
               int moved = before - remaining.getCount();
               if (moved > 0) {
                  count -= moved;
               }
            }

            if (remaining.isEmpty()) {
               entity.discard();
            } else {
               entity.setItem(remaining);
            }

            if (count <= 0) {
               return;
            }
         }
      }
   }

   private static AABB createPickupBox(BlockPos pos, Direction side) {
      double x = pos.getX();
      double y = pos.getY();
      double z = pos.getZ();

      return switch (side) {
         case DOWN -> new AABB(x, y - 0.25, z, x + 1.0, y, z + 1.0);
         case UP -> new AABB(x, y + 1.0, z, x + 1.0, y + 1.25, z + 1.0);
         case NORTH -> new AABB(x, y, z - 0.25, x + 1.0, y + 1.0, z);
         case SOUTH -> new AABB(x, y, z + 1.0, x + 1.0, y + 1.0, z + 1.25);
         case WEST -> new AABB(x - 0.25, y, z, x, y + 1.0, z + 1.0);
         case EAST -> new AABB(x + 1.0, y, z, x + 1.25, y + 1.0, z + 1.0);
         default -> throw new MatchException(null, null);
      };
   }

   private void putInNearInventories(Direction currentSide) {
      for (int ourSlot = 0; ourSlot < this.inv.getSlots(); ourSlot++) {
         ItemStack inSlot = this.inv.getStackInSlot(ourSlot);
         if (!inSlot.isEmpty()) {
            ItemStack oneItem = inSlot.copyWithCount(1);
            ItemStack leftover = InventoryUtil.addToRandomInjectable(this.level, this.worldPosition, currentSide, oneItem);
            if (leftover.isEmpty()) {
               this.inv.extractItem(ourSlot, 1, false);
               this.grantAdvancement();
               return;
            }

            int inserted = NeighborTransfers.insertItemCountShuffled(this.level, this.worldPosition, inSlot, 1, currentSide);
            if (inserted > 0) {
               this.inv.extractItem(ourSlot, inserted, false);
               this.grantAdvancement();
               return;
            }
         }
      }
   }

   private void grantAdvancement() {
      if (this.getOwner() != null) {
         AdvancementUtil.unlockAdvancement(this.getOwner().id(), this.level, ADVANCEMENT);
      }
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftfactory.chute");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerChute(containerId, playerInv, this);
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("progress", this.progress);
      output.putLong("mjStored", this.battery.getStored());
      output.store("items", CompoundTag.CODEC, this.itemManager.serializeNBT());
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.progress = input.getIntOr("progress", 0);
      this.battery.addPowerChecking(input.getLongOr("mjStored", 0L), false);
      input.read("items", CompoundTag.CODEC).ifPresent(this.itemManager::deserializeNBT);
   }
}
