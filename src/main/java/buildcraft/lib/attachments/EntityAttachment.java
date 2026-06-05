/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class EntityAttachment<T, C extends @Nullable Object> extends BaseAttachment<T, C> {

    public static <T, C extends @Nullable Object> EntityAttachment<T, C> create(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        return (EntityAttachment<T, C>) registry.create(name, typeClass, contextClass);
    }

    public static <T> EntityAttachment<T, @Nullable Void> createVoid(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    public static <T> EntityAttachment<T, @Nullable Direction> createSided(Identifier name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    public static synchronized List<EntityAttachment<?, ?>> getAll() {
        return registry.getAll();
    }

    private static final AttachmentRegistry<EntityAttachment<?, ?>> registry = new AttachmentRegistry<EntityAttachment<?, ?>>(EntityAttachment::new);

    private EntityAttachment(Identifier name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    final Map<EntityType<?>, List<IAttachmentProvider<Entity, C, T>>> providers = new IdentityHashMap<>();

    @ApiStatus.Internal
    @Nullable
    public T getCapability(Entity entity, C context) {
        for (var provider : providers.getOrDefault(entity.getType(), List.of())) {
            var ret = provider.getCapability(entity, context);
            if (ret != null)
                return ret;
        }
        return null;
    }
}
