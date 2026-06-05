/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.resources.Identifier;

public class AttachmentRegistry<C> {
    private final ConcurrentMap<Identifier, StoredCap<C>> caps = new ConcurrentHashMap<>();
    private final CapabilityConstructor<C> constructor;

    public AttachmentRegistry(CapabilityConstructor<C> constructor) {
        Objects.requireNonNull(constructor);
        this.constructor = constructor;
    }

    public C create(Identifier name, Class<?> typeClass, Class<?> contextClass) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(typeClass);
        Objects.requireNonNull(contextClass);

        StoredCap<C> ret = caps.get(name);
        if (ret == null) {
            ret = caps.computeIfAbsent(name, n -> new StoredCap<>(constructor.create(n, typeClass, contextClass), typeClass, contextClass));
        }

        if (ret.typeClass != typeClass) {
            throw new IllegalStateException("Attempted to register capability " + name + " with existing type class " + ret.typeClass + " != " + typeClass);
        } else if (ret.contextClass != contextClass) {
            throw new IllegalStateException("Attempted to register capability " + name + " with existing context class " + ret.contextClass + " != " + contextClass);
        }

        return ret.cap;
    }

    public List<C> getAll() {
        return caps.values().stream().map(StoredCap::cap).toList();
    }

    @FunctionalInterface
    public interface CapabilityConstructor<C> {

        C create(Identifier name, Class<?> typeClass, Class<?> contextClass);
    }

    private record StoredCap<C>(C cap, Class<?> typeClass, Class<?> contextClass) {}
}
