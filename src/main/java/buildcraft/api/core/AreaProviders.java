/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class AreaProviders {
   public static final List<AreaProviders.IAreaProviderGetter> providers = new ArrayList<>();

   public static List<IAreaProvider> getAreaProviders(Level world, BlockPos at) {
      List<IAreaProvider> list = new ArrayList<>();

      for (AreaProviders.IAreaProviderGetter getter : providers) {
         list.addAll(getter.getAreaProviders(world, at));
      }

      return list;
   }

   public interface IAreaProviderGetter {
      List<IAreaProvider> getAreaProviders(Level var1, BlockPos var2);
   }
}
