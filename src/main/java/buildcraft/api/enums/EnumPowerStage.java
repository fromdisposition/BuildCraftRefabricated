/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.enums;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum EnumPowerStage implements StringRepresentable {
   BLUE,
   GREEN,
   YELLOW,
   RED,
   OVERHEAT,
   BLACK;

   public static final EnumPowerStage[] VALUES = values();
   private final String modelName = this.name().toLowerCase(Locale.ROOT);

   public String getModelName() {
      return this.modelName;
   }

   public String getSerializedName() {
      return this.getModelName();
   }
}
