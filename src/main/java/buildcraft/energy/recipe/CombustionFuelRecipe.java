/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.recipe;

import buildcraft.energy.BCEnergyRecipeSerializers;
import buildcraft.energy.BCEnergyRecipeTypes;
import buildcraft.lib.fluid.stack.FluidStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public record CombustionFuelRecipe(
   Fluid fluid,
   long powerPerCycle,
   int totalBurningTime,
   @Nullable Fluid residueFluid,
   int residueAmountPer1000Mb
) implements Recipe<SingleRecipeInput> {

   public static final MapCodec<CombustionFuelRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(CombustionFuelRecipe::fluid),
      Codec.LONG.fieldOf("power_per_cycle").forGetter(CombustionFuelRecipe::powerPerCycle),
      Codec.INT.fieldOf("total_burning_time").forGetter(CombustionFuelRecipe::totalBurningTime),
      BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("residue_fluid").forGetter(r -> Optional.ofNullable(r.residueFluid())),
      Codec.INT.optionalFieldOf("residue_amount_per_1000mb", 0).forGetter(CombustionFuelRecipe::residueAmountPer1000Mb)
   ).apply(inst, (fluid, power, time, residueOpt, residueAmt) ->
      new CombustionFuelRecipe(fluid, power, time, residueOpt.orElse(null), residueAmt)));

   public static final StreamCodec<RegistryFriendlyByteBuf, CombustionFuelRecipe> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.registry(Registries.FLUID), CombustionFuelRecipe::fluid,
      ByteBufCodecs.VAR_LONG, CombustionFuelRecipe::powerPerCycle,
      ByteBufCodecs.VAR_INT, CombustionFuelRecipe::totalBurningTime,
      ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.FLUID)), r -> Optional.ofNullable(r.residueFluid()),
      ByteBufCodecs.VAR_INT, CombustionFuelRecipe::residueAmountPer1000Mb,
      (f, power, time, resOpt, resAmt) -> new CombustionFuelRecipe(f, power, time, resOpt.orElse(null), resAmt)
   );

   public static final RecipeSerializer<CombustionFuelRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

   @Override
   public boolean matches(SingleRecipeInput in, Level level) {
      return false;
   }

   @Override
   public ItemStack assemble(SingleRecipeInput in) {
      return ItemStack.EMPTY;
   }

   @Override
   public boolean isSpecial() {
      return true;
   }

   @Override
   public boolean showNotification() {
      return false;
   }

   @Override
   public String group() {
      return "";
   }

   @Override
   public PlacementInfo placementInfo() {
      return PlacementInfo.NOT_PLACEABLE;
   }

   @Override
   public RecipeType<CombustionFuelRecipe> getType() {
      return BCEnergyRecipeTypes.COMBUSTION_FUEL;
   }

   @Override
   public RecipeSerializer<CombustionFuelRecipe> getSerializer() {
      return SERIALIZER;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return RecipeBookCategories.CRAFTING_MISC;
   }

   public boolean isDirty() {
      return residueFluid != null;
   }

   public FluidStack getResiduePerBucket() {
      return isDirty() ? new FluidStack(residueFluid, residueAmountPer1000Mb) : FluidStack.EMPTY;
   }
}
