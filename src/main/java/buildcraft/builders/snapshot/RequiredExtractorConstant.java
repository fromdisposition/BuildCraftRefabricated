/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.fluids.FluidStack;
import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class RequiredExtractorConstant extends RequiredExtractor {
   @SerializedName("items")
   private List<ItemStackRef> itemRefs = Collections.emptyList();
   @SerializedName("fluids")
   private List<FluidStackRef> fluidRefs = Collections.emptyList();

   @Nonnull
   @Override
   public List<ItemStack> extractItemsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt) {
      return Collections.unmodifiableList(this.itemRefs.stream().map(ref -> ref.get(tileNbt)).collect(Collectors.toList()));
   }

   @Nonnull
   @Override
   public List<FluidStack> extractFluidsFromBlock(@Nonnull BlockState blockState, @Nullable CompoundTag tileNbt) {
      return Collections.unmodifiableList(this.fluidRefs.stream().map(ref -> ref.get(tileNbt)).collect(Collectors.toList()));
   }

   @Nonnull
   @Override
   public List<ItemStack> extractItemsFromEntity(@Nonnull CompoundTag entityNbt) {
      return Collections.unmodifiableList(this.itemRefs.stream().map(ref -> ref.get(entityNbt)).collect(Collectors.toList()));
   }

   @Nonnull
   @Override
   public List<FluidStack> extractFluidsFromEntity(@Nonnull CompoundTag entityNbt) {
      return Collections.unmodifiableList(this.fluidRefs.stream().map(ref -> ref.get(entityNbt)).collect(Collectors.toList()));
   }
}
