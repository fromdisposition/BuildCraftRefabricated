/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.container.ContainerAutoCraftFluids;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.fabric.transfer.SingleFluidTank;
import buildcraft.lib.fabric.transfer.SidedFluidStorages;
import buildcraft.lib.fluid.stack.FluidStack;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileAutoWorkbenchFluids extends TileAutoWorkbenchBase implements MenuProvider, BlockEntityExtendedMenu, IDebuggable {
   private static final int TANK_CAPACITY_MB = 6000;
   public final SingleFluidTank tank1 = new SingleFluidTank(TANK_CAPACITY_MB, SingleFluidTank.TankAccess.OPEN, this::setChanged);
   public final SingleFluidTank tank2 = new SingleFluidTank(TANK_CAPACITY_MB, SingleFluidTank.TankAccess.OPEN, this::setChanged);
   private final FluidWorkbenchCraftSupport fluidSupport = new FluidWorkbenchCraftSupport(this.tank1, this.tank2);

   public TileAutoWorkbenchFluids(BlockPos pos, BlockState state) {
      super(BCFactoryBlockEntities.AUTO_WORKBENCH_FLUIDS, pos, state, 2, 2);
      this.crafting.setFluidSupport(this.fluidSupport);
   }

   @Nullable
   public Storage<FluidVariant> getSidedFluidStorage(@Nullable Direction direction) {
      if (direction == null) {
         return this.tank1;
      }

      return switch (direction) {
         case DOWN, NORTH, WEST -> SidedFluidStorages.insertOnly(this.tank1);
         case UP, SOUTH, EAST -> SidedFluidStorages.insertOnly(this.tank2);
         default -> null;
      };
   }

   public SingleFluidTank getTank1() {
      return this.tank1;
   }

   public SingleFluidTank getTank2() {
      return this.tank2;
   }

   @Override
   public BlockEntity asBlockEntity() {
      return this;
   }

   @Override
   public Component getDisplayName() {
      return Component.translatable("item.buildcraftfactory.autoworkbench_fluid");
   }

   @Override
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerAutoCraftFluids(containerId, playerInv, this);
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("Tanks:");
      left.add("  " + this.tank1.getFluidStack());
      left.add("  " + this.tank2.getFluidStack());
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      this.tank1.serialize(output);
      output.store("tank2Fluid", FluidStack.CODEC, this.tank2.getFluidStack());
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.tank1.deserialize(input);
      input.read("tank2Fluid", FluidStack.CODEC).ifPresent(this.tank2::setContents);
   }
}
