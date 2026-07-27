/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.tiles;

import java.util.Locale;

public interface IControllable {
   IControllable.Mode getControlMode();

   void setControlMode(IControllable.Mode var1);

   default boolean acceptsControlMode(IControllable.Mode mode) {
      return mode != null;
   }

   enum Mode {
      ON,
      OFF,
      LOOP;

      public static final IControllable.Mode[] VALUES = values();
      public final String lowerCaseName = this.name().toLowerCase(Locale.ROOT);
   }
}
