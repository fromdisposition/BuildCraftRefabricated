/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.block;

import buildcraft.api.mj.ILaserTargetBlock;
import buildcraft.silicon.tile.TileLaserTableBase;
import java.util.function.Supplier;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockLaserTable extends Block implements ILaserTargetBlock, EntityBlock {
   private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);
   private final Supplier<? extends BlockEntityType<? extends TileLaserTableBase>> beTypeSupplier;
   private final BlockLaserTable.ServerMenuFactory menuFactory;

   public BlockLaserTable(
      Properties properties, Supplier<? extends BlockEntityType<? extends TileLaserTableBase>> beTypeSupplier, BlockLaserTable.ServerMenuFactory menuFactory
   ) {
      super(properties);
      this.beTypeSupplier = beTypeSupplier;
      this.menuFactory = menuFactory;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   protected boolean isPathfindable(BlockState state, PathComputationType type) {
      return false;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return this.beTypeSupplier.get().create(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? null : (lvl, pos, st, be) -> {
         if (be instanceof TileLaserTableBase table) {
            table.serverTick();
         }
      };
   }

   protected InteractionResult useWithoutItem(BlockState state, Level level, final BlockPos pos, Player player, BlockHitResult hitResult) {
      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      } else {
         final BlockEntity be = level.getBlockEntity(pos);
         if (be instanceof TileLaserTableBase table && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new ExtendedMenuProvider<BlockPos>() {
               public BlockPos getScreenOpeningData(ServerPlayer player) {
                  return pos;
               }

               public Component getDisplayName() {
                  return be.getBlockState().getBlock().getName();
               }

               public AbstractContainerMenu createMenu(int containerId, Inventory inv, Player p) {
                  return BlockLaserTable.this.menuFactory.create(containerId, inv, table);
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
      if (level.getBlockEntity(pos) instanceof TileLaserTableBase table) {
         table.onPlacedBy(placer, stack);
         level.sendBlockUpdated(pos, state, state, 2);
      }
   }

   public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TileLaserTableBase table) {
         NonNullList<ItemStack> drops = NonNullList.create();
         table.addDrops(drops, 0);

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
      AbstractContainerMenu create(int var1, Inventory var2, TileLaserTableBase var3);
   }
}
