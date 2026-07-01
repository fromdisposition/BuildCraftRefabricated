/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import net.minecraft.resources.Identifier;

public final class PaperAdvancement {
   public static final Identifier ID = Identifier.parse("buildcraftcore:paper");
   public static final String WRITE_TO_LIST = "write_to_list";
   public static final String WRITE_TO_BLUEPRINT = "write_to_blueprint";
   public static final String WRITE_TO_TEMPLATE = "write_to_template";
   public static final String CAPTURE_WITH_SCHEMATIC = "capture_with_schematic";

   private PaperAdvancement() {
   }
}
