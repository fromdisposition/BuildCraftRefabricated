/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.schematics;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.lib.fluids.FluidStack;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

public interface ISchematicEntity {
   void init(SchematicEntityContext var1);

   Vec3 getPos();

   @Nonnull
   default List<ItemStack> computeRequiredItems() {
      return Collections.emptyList();
   }

   @Nonnull
   default List<FluidStack> computeRequiredFluids() {
      return Collections.emptyList();
   }

   ISchematicEntity getRotated(Rotation var1);

   Entity build(Level var1, BlockPos var2);

   Entity buildWithoutChecks(Level var1, BlockPos var2);

   CompoundTag serializeNBT();

   void deserializeNBT(CompoundTag var1) throws InvalidInputDataException;
}
