/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface IAttachmentProvider<O, C extends @Nullable Object, T> {

    @Nullable
    T getCapability(O object, C context);
}
