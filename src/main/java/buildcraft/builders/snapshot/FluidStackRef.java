/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.fluids.FluidStack;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public class FluidStackRef {
   private final NbtRef<StringTag> fluid;
   private final NbtRef<IntTag> amount;

   public FluidStackRef(NbtRef<StringTag> fluid, NbtRef<IntTag> amount) {
      this.fluid = fluid;
      this.amount = amount;
   }

   public FluidStack get(Tag nbt) {
      Identifier fluidId = Identifier.parse(((StringTag)this.fluid.get(nbt).orElseThrow(NullPointerException::new)).value());
      Fluid fluidObj = (Fluid)BuiltInRegistries.FLUID.getValue(fluidId);
      int fluidAmount = Optional.ofNullable(this.amount).flatMap(ref -> ref.get(nbt)).<Integer>map(IntTag::value).orElse(1000);
      return new FluidStack(fluidObj, fluidAmount);
   }
}
