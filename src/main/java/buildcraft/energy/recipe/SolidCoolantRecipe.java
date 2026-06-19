/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.recipe;

import buildcraft.energy.BCEnergyRecipeSerializers;
import buildcraft.energy.BCEnergyRecipeTypes;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.recipe.BcRecipeSerializers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
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

/**
 * Converts a solid item (e.g. ice) into a coolant fluid when placed in the engine coolant tank.
 * {@code coolantAmountPerItem} is the mB of coolant fluid produced per one item of {@code item}.
 */
public record SolidCoolantRecipe(
   Item item,
   Fluid coolantFluid,
   int coolantAmountPerItem
) implements Recipe<SingleRecipeInput> {

   public static final MapCodec<SolidCoolantRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(SolidCoolantRecipe::item),
      BuiltInRegistries.FLUID.byNameCodec().fieldOf("coolant_fluid").forGetter(SolidCoolantRecipe::coolantFluid),
      Codec.INT.fieldOf("coolant_amount_per_item").forGetter(SolidCoolantRecipe::coolantAmountPerItem)
   ).apply(inst, SolidCoolantRecipe::new));

   public static final StreamCodec<RegistryFriendlyByteBuf, SolidCoolantRecipe> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.registry(Registries.ITEM), SolidCoolantRecipe::item,
      ByteBufCodecs.registry(Registries.FLUID), SolidCoolantRecipe::coolantFluid,
      ByteBufCodecs.VAR_INT, SolidCoolantRecipe::coolantAmountPerItem,
      SolidCoolantRecipe::new
   );

   public static final RecipeSerializer<SolidCoolantRecipe> SERIALIZER = BcRecipeSerializers.of(MAP_CODEC, STREAM_CODEC);

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

   @Override
   public String group() {
      return "";
   }

   @Override
   public PlacementInfo placementInfo() {
      return PlacementInfo.NOT_PLACEABLE;
   }

   @Override
   public RecipeType<SolidCoolantRecipe> getType() {
      return BCEnergyRecipeTypes.SOLID_COOLANT;
   }

   @Override
   public RecipeSerializer<SolidCoolantRecipe> getSerializer() {
      return SERIALIZER;
   }

   @Override
   public RecipeBookCategory recipeBookCategory() {
      return RecipeBookCategories.CRAFTING_MISC;
   }

   public boolean matchesItem(ItemStack stack) {
      return !stack.isEmpty() && stack.is(item);
   }

   public FluidStack getFluidFromStack(ItemStack stack) {
      if (!matchesItem(stack)) return FluidStack.EMPTY;
      return new FluidStack(coolantFluid, coolantAmountPerItem * stack.getCount());
   }
}
