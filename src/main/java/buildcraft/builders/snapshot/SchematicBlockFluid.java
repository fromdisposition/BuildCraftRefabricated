/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.lib.fluid.stack.FluidStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public class SchematicBlockFluid implements ISchematicBlock {
   private BlockState blockState;
   private boolean isFlowing;

   public static boolean predicate(SchematicBlockContext context) {
      return !context.blockState.getFluidState().isEmpty();
   }

   @Override
   public void init(SchematicBlockContext context) {
      this.blockState = context.blockState;
      this.isFlowing = !context.blockState.getFluidState().isEmpty() && !context.blockState.getFluidState().isSource();
   }

   @Nonnull
   @Override
   public Set<BlockPos> getRequiredBlockOffsets() {
      return Stream.concat(Arrays.stream(Direction.values()).filter(d -> d.getAxis().isHorizontal()), Stream.of(Direction.DOWN))
         .map(Direction::getUnitVec3i)
         .<BlockPos>map(BlockPos::new)
         .collect(Collectors.toSet());
   }

   @Nullable
   @Override
   public BlockState getBlockStateForRender() {
      return this.blockState;
   }

   @Nonnull
   @Override
   public List<FluidStack> computeRequiredFluids() {
      return !this.isFlowing && !this.blockState.getFluidState().isEmpty()
         ? Collections.singletonList(new FluidStack(this.blockState.getFluidState().getType(), 1000))
         : Collections.emptyList();
   }

   public SchematicBlockFluid getRotated(Rotation rotation) {
      SchematicBlockFluid schematicBlock = SchematicBlockManager.createCleanCopy(this);
      schematicBlock.blockState = this.blockState;
      schematicBlock.isFlowing = this.isFlowing;
      return schematicBlock;
   }

   @Override
   public boolean canBuild(Level level, BlockPos blockPos) {
      return level.isEmptyBlock(blockPos) || !level.getFluidState(blockPos).isEmpty() && !level.getFluidState(blockPos).isSource();
   }

   @Override
   public boolean build(Level level, BlockPos blockPos) {
      if (this.isFlowing) {
         return true;
      } else if (level.setBlock(blockPos, this.blockState, 11)) {
         Stream.concat(Stream.of(Direction.values()).map(Direction::getUnitVec3i).map(BlockPos::new), Stream.of(BlockPos.ZERO))
            .<BlockPos>map(blockPos::offset)
            .forEach(updatePos -> level.neighborChanged(updatePos, this.blockState.getBlock(), null));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean buildWithoutChecks(Level level, BlockPos blockPos) {
      return level.setBlock(blockPos, this.blockState, 0);
   }

   @Override
   public boolean isBuilt(Level level, BlockPos blockPos) {
      return this.isFlowing || this.blockState.equals(level.getBlockState(blockPos));
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.put("blockState", NbtUtils.writeBlockState(this.blockState));
      nbt.putBoolean("isFlowing", this.isFlowing);
      return nbt;
   }

   @Override
   public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
      this.blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, nbt.getCompoundOrEmpty("blockState"));
      this.isFlowing = nbt.getBooleanOr("isFlowing", false);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SchematicBlockFluid that = (SchematicBlockFluid)o;
         return this.isFlowing == that.isFlowing && this.blockState.equals(that.blockState);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.blockState.hashCode();
      return 31 * result + (this.isFlowing ? 1 : 0);
   }
}
