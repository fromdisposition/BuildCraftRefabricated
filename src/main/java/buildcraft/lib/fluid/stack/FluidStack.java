/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.stack;

import buildcraft.lib.common.MutableDataComponentHolder;
import buildcraft.lib.fluid.display.FluidDisplayNames;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
//? if >= 1.21.10 {
//?}
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class FluidStack implements MutableDataComponentHolder, FluidInstance {
   public static final MapCodec<FluidStack> MAP_CODEC = MapCodec.recursive(
      "FluidStack",
      c -> RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               FLUID_HOLDER_CODEC_WITH_BOUND_COMPONENTS.fieldOf("id").forGetter(FluidStack::typeHolder),
               ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStack::getAmount),
               DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(stack -> stack.components.asPatch())
            )
            .apply(instance, FluidStack::new)
      )
   );
   public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(MAP_CODEC::codec);
   public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
      .xmap(optional -> optional.orElse(FluidStack.EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
   public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, FluidStack>() {
      @Override
      public FluidStack decode(RegistryFriendlyByteBuf buf) {
         int amount = buf.readVarInt();
         if (amount <= 0) {
            return FluidStack.EMPTY;
         }

         Holder<Fluid> holder = FluidInstance.FLUID_HOLDER_STREAM_CODEC.decode(buf);
         DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
         return new FluidStack(holder, amount, patch);
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
         if (stack.isEmpty()) {
            buf.writeVarInt(0);
         } else {
            buf.writeVarInt(stack.getAmount());
            FluidInstance.FLUID_HOLDER_STREAM_CODEC.encode(buf, stack.typeHolder());
            DataComponentPatch.STREAM_CODEC.encode(buf, stack.components.asPatch());
         }
      }
   };
   public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, FluidStack>() {
      @Override
      public FluidStack decode(RegistryFriendlyByteBuf buf) {
         FluidStack stack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buf);
         if (stack.isEmpty()) {
            throw new DecoderException("Empty FluidStack not allowed");
         } else {
            return stack;
         }
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
         if (stack.isEmpty()) {
            throw new EncoderException("Empty FluidStack not allowed");
         }

         FluidStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
      }
   };
   public static final FluidStack EMPTY = new FluidStack(null);
   private int amount;
   private final @Nullable Holder<Fluid> fluid;
   private final PatchedDataComponentMap components;

   @Override
   public DataComponentMap getComponents() {
      return (this.isEmpty() ? DataComponentMap.EMPTY : this.components);
   }

   public DataComponentPatch getComponentsPatch() {
      return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
   }

   public boolean hasNonDefault(DataComponentType<?> type) {
      //? if >= 1.21.10 {
      return !this.isEmpty() && this.components.hasNonDefault(type);
      //?} else {
      /*return !this.isEmpty() && this.components.has(type);
      *///?}
   }

   public FluidStack(Fluid fluid, int amount, DataComponentPatch patch) {
      this(FluidHolders.fluidHolder(fluid), amount, patch);
   }

   public FluidStack(Fluid fluid, int amount) {
      this(fluid, amount, DataComponentPatch.EMPTY);
   }

   public FluidStack(Holder<Fluid> fluid, int amount) {
      this(fluid, amount, DataComponentPatch.EMPTY);
   }

   public FluidStack(Holder<Fluid> fluid, int amount, DataComponentPatch patch) {
      //? if >= 26.1 {
      this(fluid, amount, PatchedDataComponentMap.fromPatch(fluid.components(), patch));
      //?} else {
      /*this(fluid, amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
      *///?}
   }

   private FluidStack(Holder<Fluid> fluid, int amount, PatchedDataComponentMap components) {
      this.fluid = fluid;
      this.amount = amount;
      this.components = components;
   }

   private FluidStack(@Nullable Void unused) {
      this.fluid = null;
      this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
   }

   public boolean isEmpty() {
      return this == EMPTY || (this.fluid.value()).isSame(Fluids.EMPTY) || this.amount <= 0;
   }

   public FluidStack split(int amount) {
      int i = Math.min(amount, this.getAmount());
      FluidStack fluidStack = this.copyWithAmount(i);
      this.shrink(i);
      return fluidStack;
   }

   public Fluid getFluid() {
      return this.typeHolder().value();
   }

   @Override
   public Holder<Fluid> typeHolder() {
      return this.isEmpty() ? FluidHolders.emptyFluidHolder() : this.fluid;
   }

   public FluidStack copy() {
      return this.isEmpty() ? EMPTY : new FluidStack(this.typeHolder(), this.amount(), this.components.copy());
   }

   public FluidStack copyWithAmount(int amount) {
      if (this.isEmpty()) {
         return EMPTY;
      }

      FluidStack fluidStack = this.copy();
      fluidStack.setAmount(amount);
      return fluidStack;
   }

   public FluidStack transmuteCopy(Fluid newFluid) {
      return this.transmuteCopy(newFluid, this.amount());
   }

   public FluidStack transmuteCopy(Fluid newFluid, int newAmount) {
      return this.isEmpty() ? EMPTY : this.transmuteCopyIgnoreEmpty(newFluid, newAmount);
   }

   private FluidStack transmuteCopyIgnoreEmpty(Fluid newFluid, int newAmount) {
      return new FluidStack(newFluid, newAmount, this.components.asPatch());
   }

   public static boolean matches(FluidStack first, FluidStack second) {
      if (first == second) {
         return true;
      } else {
         return first.getAmount() != second.getAmount() ? false : isSameFluidSameComponents(first, second);
      }
   }

   public static boolean isSameFluid(FluidStack first, FluidStack second) {
      return first.is(second.getFluid());
   }

   public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
      if (!first.is(second.getFluid())) {
         return false;
      } else {
         return first.isEmpty() && second.isEmpty() ? true : Objects.equals(first.components, second.components);
      }
   }

   public static MapCodec<FluidStack> lenientOptionalFieldOf(String fieldName) {
      return CODEC.lenientOptionalFieldOf(fieldName).xmap(optional -> optional.orElse(EMPTY), stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
   }

   public static int hashFluidAndComponents(@Nullable FluidStack stack) {
      if (stack != null) {
         int i = 31 + stack.getFluid().hashCode();
         return 31 * i + stack.getComponents().hashCode();
      } else {
         return 0;
      }
   }

   public String getDescriptionId() {
      return FluidDisplayNames.descriptionIdFor(this.getFluid());
   }

   @Override
   public String toString() {
      return this.getAmount() + " " + this.getFluid();
   }

   @Override
   public <T> @Nullable T set(DataComponentType<T> type, @Nullable T component) {
      return this.components.set(type, component);
   }

   public <T> @Nullable T set(TypedDataComponent<T> value) {
      //? if >= 1.21.10 {
      return this.components.set(value);
      //?} else {
      /*return this.components.set(value.type(), value.value());
      *///?}
   }

   @Override
   public <T> @Nullable T remove(DataComponentType<? extends T> type) {
      return (T)this.components.remove(type);
   }

   @Override
   public void applyComponents(DataComponentPatch patch) {
      this.components.applyPatch(patch);
   }

   @Override
   public void applyComponents(DataComponentMap components) {
      this.components.setAll(components);
   }

   public Component getHoverName() {
      return FluidDisplayNames.forStack(this);
   }

   @Override
   public int amount() {
      return this.isEmpty() ? 0 : this.amount;
   }

   public int getAmount() {
      return this.amount();
   }

   public void setAmount(int amount) {
      this.amount = amount;
   }

   public void grow(int addedAmount) {
      this.setAmount(this.getAmount() + addedAmount);
   }

   public void shrink(int removedAmount) {
      this.grow(-removedAmount);
   }
}
