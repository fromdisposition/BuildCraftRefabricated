/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.stack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
//? if >= 26.1 {
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
//?}
//? if < 26.1 {
/*import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
*///?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public interface FluidInstance
   //? if >= 26.1 {
   extends TypedInstance<Fluid>, DataComponentGetter
   //?}
{
   String FIELD_ID = "id";
   String FIELD_AMOUNT = "amount";
   String FIELD_COMPONENTS = "components";
   Codec<Holder<Fluid>> FLUID_HOLDER_CODEC = BuiltInRegistries.FLUID
      .holderByNameCodec()
      .validate(fluid -> FluidHolders.isEmptyFluid(fluid) ? DataResult.error(() -> "Fluid must not be minecraft:empty") : DataResult.success(fluid));
   StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_HOLDER_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID);
   //? if >= 26.1 {
   Codec<Holder<Fluid>> FLUID_HOLDER_CODEC_WITH_BOUND_COMPONENTS = FLUID_HOLDER_CODEC.validate(
      fluid -> !fluid.areComponentsBound()
         ? DataResult.error(() -> "Fluid " + FluidHolders.registryId(fluid).map(Identifier::toString).orElse("unknown") + " does not have components yet")
         : DataResult.success(fluid)
   );
   //?} else {
   /*// 1.21.x fluids carry no data components, so there is nothing to "bind" — accept any fluid holder.
   Codec<Holder<Fluid>> FLUID_HOLDER_CODEC_WITH_BOUND_COMPONENTS = FLUID_HOLDER_CODEC;
   *///?}

   int amount();

   //? if < 26.1 {
   /*// 26.x inherits these from TypedInstance<Fluid>; 1.21.x has no such type, so declare them here.
   Holder<Fluid> typeHolder();

   default boolean is(Fluid fluid) {
      return typeHolder().value().isSame(fluid);
   }

   default boolean is(TagKey<Fluid> tag) {
      return typeHolder().is(tag);
   }

   default boolean is(Holder<Fluid> holder) {
      return is(holder.value());
   }

   default boolean is(HolderSet<Fluid> holders) {
      return holders.contains(typeHolder());
   }
   *///?}
}
