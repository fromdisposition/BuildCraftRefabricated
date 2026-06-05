/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface IAttachmentInvalidationListener {

    boolean onInvalidate();
}
