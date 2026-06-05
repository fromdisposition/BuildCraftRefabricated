/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.fluid;

import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import buildcraft.lib.fluids.FluidInstance;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.fluids.FluidStackTemplate;
import buildcraft.lib.fluids.FluidConstants;
import buildcraft.lib.fluids.FluidType;
import buildcraft.lib.transfer.TransferPreconditions;
import buildcraft.lib.transfer.resource.DataComponentHolderResource;
import org.jspecify.annotations.Nullable;

public final class FluidResource implements DataComponentHolderResource<Fluid> {
    private static final Map<Fluid, FluidResource> DEFAULT_RESOURCES = new ConcurrentHashMap<>();

    public static final FluidResource EMPTY = new FluidResource(FluidStack.EMPTY);

    public static final Codec<FluidResource> CODEC = FluidStack.fixedAmountCodec(FluidConstants.BUCKET_VOLUME).xmap(FluidResource::of, resource -> resource.toStack(FluidConstants.BUCKET_VOLUME));

    public static final Codec<FluidResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
            optional -> optional.orElse(FluidResource.EMPTY),
            resource -> resource.isEmpty() ? Optional.empty() : Optional.of(resource));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResource> STREAM_CODEC = StreamCodec.composite(
            FluidInstance.FLUID_HOLDER_STREAM_CODEC, FluidResource::typeHolder,
            DataComponentPatch.STREAM_CODEC, FluidResource::getComponentsPatch,
            FluidResource::of);

    public static FluidResource of(FluidStack stack) {
        if (stack.isEmpty() || buildcraft.lib.fabric.Mc26Compat.componentsPatchEmpty(stack)) {
            return of(stack.getFluid());
        }
        return new FluidResource(stack.copyWithAmount(FluidConstants.BUCKET_VOLUME));
    }

    public static FluidResource of(@Nullable FluidStackTemplate template) {
        if (template == null) {
            return EMPTY;
        }
        if (template.components().isEmpty()) {
            return of(template.fluid());
        }

        var stack = template.create();
        stack.setAmount(FluidConstants.BUCKET_VOLUME);
        return new FluidResource(stack);
    }

    public static FluidResource of(Fluid fluid) {
        if (fluid == Fluids.EMPTY) return EMPTY;
        return DEFAULT_RESOURCES.computeIfAbsent(fluid, f -> new FluidResource(new FluidStack(f, FluidConstants.BUCKET_VOLUME)));
    }

    public static FluidResource of(Fluid fluid, DataComponentPatch patch) {
        return of(fluid.builtInRegistryHolder(), patch);
    }

    public static FluidResource of(Holder<Fluid> fluid) {
        return of(fluid.value());
    }

    public static FluidResource of(Holder<Fluid> holder, DataComponentPatch patch) {
        if (holder.value() == Fluids.EMPTY || patch.isEmpty()) {
            return of(holder.value());
        }
        return new FluidResource(new FluidStack(holder, FluidConstants.BUCKET_VOLUME, patch));
    }

    private final FluidStack innerStack;

    private FluidResource(FluidStack stack) {
        this.innerStack = stack;
    }

    @Override
    public Fluid value() {
        return innerStack.getFluid();
    }

    public Fluid getFluid() {
        return value();
    }

    @Override
    public Holder<Fluid> typeHolder() {
        return innerStack.typeHolder();
    }

    public FluidType getFluidType() {
        return innerStack.getFluidType();
    }

    @Override
    public boolean isEmpty() {
        return innerStack.isEmpty();
    }

    @Override
    public FluidResource withMergedPatch(DataComponentPatch patch) {
        if (isEmpty() || patch.isEmpty())
            return this;

        FluidStack stack = innerStack.copy();
        stack.applyComponents(patch);
        return FluidResource.of(stack);
    }

    @Override
    public <D> FluidResource with(DataComponentType<D> type, @Nullable D data) {
        if (isEmpty()) return FluidResource.EMPTY;
        if (Objects.equals(get(type), data)) return this;

        FluidStack stack = innerStack.copy();
        stack.set(type, data);
        return FluidResource.of(stack);
    }

    @Override
    public <D> FluidResource with(Supplier<? extends DataComponentType<D>> type, @Nullable D data) {
        return with(type.get(), data);
    }

    @Override
    public FluidResource without(DataComponentType<?> type) {
        if (isEmpty()) return FluidResource.EMPTY;
        if (get(type) == null) return this;

        FluidStack stack = innerStack.copy();
        stack.remove(type);
        return FluidResource.of(stack);
    }

    @Override
    public FluidResource without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.immutableComponents();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return innerStack.getComponentsPatch();
    }

    public FluidStack toStack(int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0) return FluidStack.EMPTY;
        return this.innerStack.copyWithAmount(amount);
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return innerStack.isComponentsPatchEmpty();
    }

    public boolean is(FluidType fluidType) {
        return innerStack.is(fluidType);
    }

    public boolean matches(FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(stack, innerStack);
    }

    public boolean matches(@Nullable FluidStackTemplate template) {
        return FluidStack.isSameFluidSameComponents(innerStack, template);
    }

    public boolean test(Predicate<FluidStack> predicate) {
        return predicate.test(innerStack);
    }

    public Component getHoverName() {
        return innerStack.getHoverName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        FluidResource other = (FluidResource) obj;
        return FluidStack.isSameFluidSameComponents(this.innerStack, other.innerStack);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(innerStack);
    }

    @Override
    public String toString() {

        return getFluidType() + " [" + getComponentsPatch().size() + "]";
    }
}
