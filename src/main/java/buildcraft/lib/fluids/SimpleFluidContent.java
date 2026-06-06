package buildcraft.lib.fluids;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class SimpleFluidContent implements DataComponentHolder {
   public static final SimpleFluidContent EMPTY = new SimpleFluidContent(FluidStack.EMPTY);
   public static final Codec<SimpleFluidContent> CODEC = FluidStack.OPTIONAL_CODEC.xmap(SimpleFluidContent::new, content -> content.fluidStack);
   public static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidContent> STREAM_CODEC = FluidStack.OPTIONAL_STREAM_CODEC
      .map(SimpleFluidContent::new, content -> content.fluidStack);
   private final FluidStack fluidStack;

   private SimpleFluidContent(FluidStack fluidStack) {
      this.fluidStack = fluidStack;
   }

   public static SimpleFluidContent copyOf(FluidStack fluidStack) {
      return fluidStack.isEmpty() ? EMPTY : new SimpleFluidContent(fluidStack.copy());
   }

   public FluidStack copy() {
      return this.fluidStack.copy();
   }

   public boolean isEmpty() {
      return this.fluidStack.isEmpty();
   }

   public Fluid getFluid() {
      return this.fluidStack.getFluid();
   }

   public Holder<Fluid> getFluidHolder() {
      return this.fluidStack.typeHolder();
   }

   public boolean is(TagKey<Fluid> tag) {
      return this.fluidStack.is(tag);
   }

   public boolean is(Fluid fluid) {
      return this.fluidStack.is(fluid);
   }

   public boolean is(Predicate<Holder<Fluid>> predicate) {
      return predicate.test(this.fluidStack.typeHolder());
   }

   public boolean is(Holder<Fluid> holder) {
      return this.fluidStack.is(holder);
   }

   public boolean is(HolderSet<Fluid> holders) {
      return this.fluidStack.is(holders);
   }

   public int getAmount() {
      return this.fluidStack.getAmount();
   }

   public FluidType getFluidType() {
      return this.fluidStack.getFluidType();
   }

   public boolean is(FluidType fluidType) {
      return this.fluidStack.is(fluidType);
   }

   public boolean matches(FluidStack other) {
      return FluidStack.matches(this.fluidStack, other);
   }

   public boolean isSameFluid(FluidStack other) {
      return FluidStack.isSameFluid(this.fluidStack, other);
   }

   public boolean isSameFluidSameComponents(FluidStack other) {
      return FluidStack.isSameFluidSameComponents(this.fluidStack, other);
   }

   public boolean isSameFluidSameComponents(SimpleFluidContent content) {
      return this.isSameFluidSameComponents(content.fluidStack);
   }

   public DataComponentMap getComponents() {
      return this.fluidStack.getComponents();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SimpleFluidContent o ? FluidStack.matches(this.fluidStack, o.fluidStack) : false;
      }
   }

   @Override
   public int hashCode() {
      return this.fluidStack.getAmount() * 31 + FluidStack.hashFluidAndComponents(this.fluidStack);
   }
}
