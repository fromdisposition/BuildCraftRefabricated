/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.transfer.resource;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.jspecify.annotations.Nullable;

public interface DataComponentHolderResource<T> extends RegisteredResource<T>, DataComponentHolder {

    boolean isComponentsPatchEmpty();

    DataComponentHolderResource<T> withMergedPatch(DataComponentPatch patch);

    <D> DataComponentHolderResource<T> with(DataComponentType<D> type, @Nullable D data);

    DataComponentHolderResource<T> without(DataComponentType<?> type);

    DataComponentPatch getComponentsPatch();

    default <D> DataComponentHolderResource<T> with(Supplier<? extends DataComponentType<D>> type, @Nullable D data) {
        return with(type.get(), data);
    }

    default DataComponentHolderResource<T> without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }
}
