/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.recipe;

import buildcraft.lib.recipe.BcRecipeSerializers;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.plug.FacadeInstance;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class FacadeSwapRecipe extends CustomRecipe {
   //? if < 26.1 {
   /*public FacadeSwapRecipe() {
      super(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
   }
   *///?}
   //? if < 1.21.10 {
   /*public boolean canCraftInDimensions(int width, int height) {
      return width * height >= 1;
   }
   *///?}
   public static final FacadeSwapRecipe INSTANCE = new FacadeSwapRecipe();
   public static final MapCodec<FacadeSwapRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
   public static final StreamCodec<RegistryFriendlyByteBuf, FacadeSwapRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
   public static final RecipeSerializer<FacadeSwapRecipe> SERIALIZER = BcRecipeSerializers.of(MAP_CODEC, STREAM_CODEC);

   @Override
   public boolean matches(CraftingInput input, Level level) {
      return !swapResult(input).isEmpty();
   }

   @Override
   //? if >= 26.1 {
   public ItemStack assemble(CraftingInput input) {
   //?} else {
   /*public ItemStack assemble(CraftingInput input, net.minecraft.core.HolderLookup.Provider provider) {
   *///?}
      return swapResult(input);
   }

   private ItemStack swapResult(CraftingInput input) {
      ItemStack facade = ItemStack.EMPTY;

      for (int slot = 0; slot < input.size(); slot++) {
         ItemStack stack = input.getItem(slot);
         if (!stack.isEmpty()) {
            if (!facade.isEmpty()) {
               return ItemStack.EMPTY;
            }

            facade = stack;
         }
      }

      if (facade.isEmpty() || !(facade.getItem() instanceof ItemPluggableFacade)) {
         return ItemStack.EMPTY;
      }

      FacadeInstance states = ItemPluggableFacade.getStates(facade).withSwappedIsHollow();
      return BCSiliconItems.PLUG_FACADE.createItemStack(states);
   }

   @Override
   public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
      return NonNullList.withSize(input.size(), ItemStack.EMPTY);
   }

   @Override
   public RecipeSerializer<? extends CustomRecipe> getSerializer() {
      return SERIALIZER;
   }

   @Override
   public boolean isSpecial() {
      return true;
   }

   @Override
   public CraftingBookCategory category() {
      return CraftingBookCategory.MISC;
   }
}
