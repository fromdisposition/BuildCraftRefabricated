/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.common;

public final class SoundActions {
    private SoundActions() {
        throw new AssertionError("SoundActions should not be instantiated.");
    }

    public static final SoundAction BUCKET_FILL = SoundAction.get("bucket_fill");

    public static final SoundAction BUCKET_EMPTY = SoundAction.get("bucket_empty");

    public static final SoundAction FLUID_VAPORIZE = SoundAction.get("fluid_vaporize");

    public static final SoundAction CAULDRON_DRIP = SoundAction.get("cauldron_drip");
}
