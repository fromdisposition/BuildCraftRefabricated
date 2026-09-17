/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.nbt;

import net.minecraft.util.profiling.ProfilerFiller;
//? if >= 1.21.10 {
import net.minecraft.util.profiling.Profiler;
//?} else {
/*import net.minecraft.util.profiling.InactiveProfiler;
*///?}

// 1.21.5+ has a static Profiler.get() holder; 1.21.1 has none, so this falls back to the no-op InactiveProfiler
// there (BC's profiler sections are debug-only, so losing them on the legacy target is harmless).
public final class BcProfiler {
   private BcProfiler() {
   }

   public static ProfilerFiller get() {
      //? if >= 1.21.10 {
      return Profiler.get();
      //?} else {
      /*return InactiveProfiler.INSTANCE; *///?}
   }
}
