package buildcraft.lib.misc;

import buildcraft.api.items.FluidItemDrops;
import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerSimple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class BlockDropsUtil {
   private BlockDropsUtil() {
   }

   @SafeVarargs
   public static void dropTileContents(Level level, BlockPos pos, BcBlockEntity tile, SingleFluidTank... fluidTanks) {
      if (!level.isClientSide()) {
         NonNullList<ItemStack> toDrop = NonNullList.create();
         tile.addDrops(toDrop, 0);
         if (fluidTanks != null && fluidTanks.length > 0) {
            FluidItemDrops.addFluidDrops(toDrop, fluidTanks);
         }

         for (ItemStack drop : toDrop) {
            if (!drop.isEmpty()) {
               Block.popResource(level, pos, drop);
            }
         }
      }
   }

   public static void dropItems(Level level, BlockPos pos, ItemHandlerSimple... handlers) {
      if (!level.isClientSide() && handlers != null) {
         for (ItemHandlerSimple handler : handlers) {
            if (handler != null) {
               for (int slot = 0; slot < handler.getSlots(); slot++) {
                  ItemStack stack = handler.getStackInSlot(slot);
                  if (!stack.isEmpty()) {
                     Block.popResource(level, pos, stack);
                  }
               }
            }
         }
      }
   }

   public static void dropStack(Level level, BlockPos pos, ItemStack stack) {
      if (!level.isClientSide() && stack != null && !stack.isEmpty()) {
         Block.popResource(level, pos, stack);
      }
   }

   public static void dropFluidShard(Level level, BlockPos pos, FluidStack stack) {
      if (!level.isClientSide() && stack != null && !stack.isEmpty()) {
         NonNullList<ItemStack> toDrop = NonNullList.create();
         FluidItemDrops.addFluidDrops(toDrop, stack);

         for (ItemStack drop : toDrop) {
            if (!drop.isEmpty()) {
               Block.popResource(level, pos, drop);
            }
         }
      }
   }

   @SafeVarargs
   public static void dropFluidShards(Level level, BlockPos pos, SingleFluidTank... tanks) {
      if (!level.isClientSide() && tanks != null && tanks.length != 0) {
         NonNullList<ItemStack> toDrop = NonNullList.create();
         FluidItemDrops.addFluidDrops(toDrop, tanks);

         for (ItemStack drop : toDrop) {
            if (!drop.isEmpty()) {
               Block.popResource(level, pos, drop);
            }
         }
      }
   }
}
