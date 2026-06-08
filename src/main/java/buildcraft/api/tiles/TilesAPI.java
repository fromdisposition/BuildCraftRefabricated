/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.tiles;

import javax.annotation.Nonnull;

public class TilesAPI {
   @Nonnull
   public static final Object CAP_CONTROLLABLE = IControllable.class;
   @Nonnull
   public static final Object CAP_HAS_WORK = IHasWork.class;
   @Nonnull
   public static final Object CAP_HEATABLE = IHeatable.class;
   @Nonnull
   public static final Object CAP_TILE_AREA_PROVIDER = ITileAreaProvider.class;
}
