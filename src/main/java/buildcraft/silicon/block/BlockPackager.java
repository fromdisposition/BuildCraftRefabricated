/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.block;

import buildcraft.silicon.tile.TilePackager;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;

public class BlockPackager extends Block implements EntityBlock {
   private final java.util.function.Supplier<? extends BlockEntityType<TilePackager>> beTypeSupplier;
   private final BlockPackager.ServerMenuFactory menuFactory;

   public BlockPackager(
      Properties properties, java.util.function.Supplier<? extends BlockEntityType<TilePackager>> beTypeSupplier, BlockPackager.ServerMenuFactory menuFactory
   ) {
      super(properties);
      this.beTypeSupplier = beTypeSupplier;
      this.menuFactory = menuFactory;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.beTypeSupplier.get().create(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? null : (lvl, pos, st, be) -> {
         if (be instanceof TilePackager packager) {
            packager.serverTick();
         }
      };
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, final BlockPos pos, Player player, BlockHitResult hitResult) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else {
         final BlockEntity be = level.getBlockEntity(pos);
         if (be instanceof TilePackager packager && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ExtendedMenuProvider<BlockPos>() {
               public BlockPos getScreenOpeningData(ServerPlayer player) {
                  return pos;
               }

               public Component getDisplayName() {
                  return be.getBlockState().getBlock().getName();
               }

               public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player p) {
                  return BlockPackager.this.menuFactory.create(containerId, inv, packager);
               }
            });
            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      if (level.getBlockEntity(pos) instanceof TilePackager packager) {
         packager.onPlacedBy(placer, stack);
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TilePackager packager) {
         NonNullList<ItemStack> drops = NonNullList.create();
         packager.addDrops(drops, 0);

         for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
               Block.popResource(level, pos, drop);
            }
         }
      }

      return super.playerWillDestroy(level, pos, state, player);
   }

   @FunctionalInterface
   public interface ServerMenuFactory {
      AbstractContainerMenu create(int var1, Inventory var2, TilePackager var3);
   }
}
