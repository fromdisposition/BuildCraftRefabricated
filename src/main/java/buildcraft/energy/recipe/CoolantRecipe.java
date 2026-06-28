/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.recipe;

import buildcraft.energy.BCEnergyRecipeSerializers;
import buildcraft.energy.BCEnergyRecipeTypes;
import buildcraft.lib.recipe.BcRecipeSerializers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
//? if >= 1.21.10 {
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
//?}
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public record CoolantRecipe(
   Fluid fluid,
   float degreesCoolingPerMb
) implements Recipe<SingleRecipeInput> {

   public static final MapCodec<CoolantRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(CoolantRecipe::fluid),
      Codec.FLOAT.fieldOf("degrees_cooling_per_mb").forGetter(CoolantRecipe::degreesCoolingPerMb)
   ).apply(inst, CoolantRecipe::new));

   public static final StreamCodec<RegistryFriendlyByteBuf, CoolantRecipe> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.registry(Registries.FLUID), CoolantRecipe::fluid,
      ByteBufCodecs.FLOAT, CoolantRecipe::degreesCoolingPerMb,
      CoolantRecipe::new
   );

   public static final RecipeSerializer<CoolantRecipe> SERIALIZER = BcRecipeSerializers.of(MAP_CODEC, STREAM_CODEC);

   @Override
   public boolean matches(SingleRecipeInput in, Level level) {
      return false;
   }

   @Override
   //? if >= 26.1 {
   public ItemStack assemble(SingleRecipeInput in) {
   //?} else {
   /*public ItemStack assemble(SingleRecipeInput in, net.minecraft.core.HolderLookup.Provider provider) {
   *///?}
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

   //? if >= 1.21.10 {
   @Override
   public String group() {
      return "";
   }
   //?} else {
   /*@Override
   public String getGroup() {
      return "";
   }
   *///?}

   //? if >= 1.21.10 {
   @Override
   public PlacementInfo placementInfo() {
      return PlacementInfo.NOT_PLACEABLE;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return RecipeBookCategories.CRAFTING_MISC;
   }
   //?} else {
   /*// 1.21.1 Recipe pre-dates the 1.21.4 placement/book-category overhaul; supply the older abstract methods.
   @Override
   public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider provider) {
      return ItemStack.EMPTY;
   }

   @Override
   public boolean canCraftInDimensions(int width, int height) {
      return false;
   }
   *///?}

   @Override
   public RecipeType<CoolantRecipe> getType() {
      return BCEnergyRecipeTypes.COOLANT;
   }

   @Override
   public RecipeSerializer<CoolantRecipe> getSerializer() {
      return SERIALIZER;
   }

   public boolean matchesFluid(Fluid other) {
      return fluid.isSame(other);
   }
}
