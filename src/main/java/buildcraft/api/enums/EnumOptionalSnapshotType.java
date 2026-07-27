/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.enums;

import java.util.Locale;
import net.minecraft.util.StringRepresentable;

public enum EnumOptionalSnapshotType implements StringRepresentable {
   NONE(null),
   TEMPLATE(EnumSnapshotType.TEMPLATE),
   BLUEPRINT(EnumSnapshotType.BLUEPRINT);

   public final EnumSnapshotType type;

   EnumOptionalSnapshotType(EnumSnapshotType type) {
      this.type = type;
   }

   public static EnumOptionalSnapshotType fromNullable(EnumSnapshotType type) {
      if (type == null) {
         return NONE;
      }

      switch (type) {
         case TEMPLATE:
            return TEMPLATE;
         case BLUEPRINT:
            return BLUEPRINT;
         default:
            return NONE;
      }
   }

   public String getSerializedName() {
      return this.name().toLowerCase(Locale.ROOT);
   }
}
