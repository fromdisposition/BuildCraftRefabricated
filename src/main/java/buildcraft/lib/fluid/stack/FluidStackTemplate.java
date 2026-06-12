/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.stack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;

public record FluidStackTemplate(Holder<Fluid> fluid, int amount, DataComponentPatch components) implements FluidInstance {
   public static final MapCodec<FluidStackTemplate> MAP_CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            FLUID_HOLDER_CODEC.fieldOf("id").forGetter(FluidStackTemplate::fluid),
            ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStackTemplate::amount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStackTemplate::components)
         )
         .apply(i, FluidStackTemplate::new)
   );
   public static final Codec<FluidStackTemplate> CODEC = Codec.withAlternative(
      MAP_CODEC.codec(), FLUID_HOLDER_CODEC, fluid -> new FluidStackTemplate((Fluid)fluid.value(), 1000)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> STREAM_CODEC = StreamCodec.composite(
      FLUID_HOLDER_STREAM_CODEC,
      FluidStackTemplate::fluid,
      ByteBufCodecs.VAR_INT,
      FluidStackTemplate::amount,
      DataComponentPatch.STREAM_CODEC,
      FluidStackTemplate::components,
      FluidStackTemplate::new
   );

   public FluidStackTemplate(Holder<Fluid> fluid, int amount, DataComponentPatch components) {
      if (!FluidHolders.isEmptyFluid(fluid) && amount > 0) {
         this.fluid = fluid;
         this.amount = amount;
         this.components = components;
      } else {
         throw new IllegalStateException("Fluid must be non-empty");
      }
   }

   public FluidStackTemplate(Holder<Fluid> fluid, int amount) {
      this(fluid, amount, DataComponentPatch.EMPTY);
   }

   public FluidStackTemplate(Fluid fluid, int amount, DataComponentPatch components) {
      this(FluidHolders.fluidHolder(fluid), amount, components);
   }

   public FluidStackTemplate(Fluid fluid, int amount) {
      this(fluid, amount, DataComponentPatch.EMPTY);
   }

   public static FluidStackTemplate fromNonEmptyStack(FluidStack stack) {
      if (stack.isEmpty()) {
         throw new IllegalStateException("Stack must be non-empty");
      } else {
         return new FluidStackTemplate(stack.typeHolder(), stack.getAmount(), stack.getComponentsPatch());
      }
   }

   public FluidStackTemplate withAmount(int amount) {
      return this.amount == amount ? this : new FluidStackTemplate(this.fluid, amount, this.components);
   }

   public FluidStack create() {
      return new FluidStack(this.fluid, this.amount, this.components);
   }

   public FluidStack apply(DataComponentPatch additionalPatch) {
      return this.apply(this.amount, additionalPatch);
   }

   public FluidStack apply(int amount, DataComponentPatch additionalPatch) {
      FluidStack stack = new FluidStack(this.fluid, amount, additionalPatch);
      stack.applyComponents(this.components);
      return stack;
   }

   public Holder<Fluid> typeHolder() {
      return this.fluid;
   }

   public <T> @Nullable T get(DataComponentType<? extends T> type) {
      return (T)this.components.get(this.fluid.components(), type);
   }

   public static Codec<FluidStackTemplate> fixedAmountCodec(int amount) {
      return Codec.lazyInitialized(
         () -> RecordCodecBuilder.create(
            i -> i.group(
                  FLUID_HOLDER_CODEC.fieldOf("id").forGetter(FluidStackTemplate::fluid),
                  DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStackTemplate::components)
               )
               .apply(i, (holder, patch) -> new FluidStackTemplate(holder, amount, patch))
         )
      );
   }

   public static StreamCodec<RegistryFriendlyByteBuf, FluidStackTemplate> fixedAmountStreamCodec(int amount) {
      return StreamCodec.composite(
         FLUID_HOLDER_STREAM_CODEC,
         FluidStackTemplate::fluid,
         DataComponentPatch.STREAM_CODEC,
         FluidStackTemplate::components,
         (holder, patch) -> new FluidStackTemplate(holder, amount, patch)
      );
   }
}
