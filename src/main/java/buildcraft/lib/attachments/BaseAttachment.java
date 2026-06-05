/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class BaseAttachment<T, C extends @Nullable Object> {
    private final Identifier name;
    private final Class<T> typeClass;
    private final Class<C> contextClass;

    protected BaseAttachment(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        this.name = name;
        this.typeClass = typeClass;
        this.contextClass = contextClass;
    }

    public final Identifier name() {
        return name;
    }

    public final Class<T> typeClass() {
        return typeClass;
    }

    public final Class<C> contextClass() {
        return contextClass;
    }
}
