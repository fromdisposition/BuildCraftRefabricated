/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.common;

public final class SoundActions {
   public static final SoundAction BUCKET_FILL = SoundAction.get("bucket_fill");
   public static final SoundAction BUCKET_EMPTY = SoundAction.get("bucket_empty");
   public static final SoundAction FLUID_VAPORIZE = SoundAction.get("fluid_vaporize");
   public static final SoundAction CAULDRON_DRIP = SoundAction.get("cauldron_drip");

   private SoundActions() {
      throw new AssertionError("SoundActions should not be instantiated.");
   }
}
