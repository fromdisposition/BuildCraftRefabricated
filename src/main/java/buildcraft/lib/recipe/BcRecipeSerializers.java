/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/**
 * Cross-version factory for {@link RecipeSerializer}. 26.1 turned RecipeSerializer into a concrete
 * class taking a {@code (MapCodec, StreamCodec)} pair; 1.21.x keeps it as an interface declaring
 * {@code codec()} / {@code streamCodec()}. This wrapper hides that difference so recipe classes can
 * build their serializer the same way on every target.
 */
public final class BcRecipeSerializers {
   private BcRecipeSerializers() {
   }

   // RecipeSerializer.streamCodec() is a required abstract method that 1.21.x marks @Deprecated.
   @SuppressWarnings("deprecation")
   public static <T extends Recipe<?>> RecipeSerializer<T> of(
      MapCodec<T> codec,
      StreamCodec<RegistryFriendlyByteBuf, T> streamCodec
   ) {
      //? if >= 26.1 {
      return new RecipeSerializer<>(codec, streamCodec);
      //?} else {
      /*return new RecipeSerializer<T>() {
         @Override
         public MapCodec<T> codec() {
            return codec;
         }

         @Override
         public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
            return streamCodec;
         }
      };
      *///?}
   }
}
