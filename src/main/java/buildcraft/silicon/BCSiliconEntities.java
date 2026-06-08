/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import buildcraft.fabric.BCRegistries;
import buildcraft.silicon.entity.EntityPackage;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public final class BCSiliconEntities {
   public static EntityType<EntityPackage> PACKAGE;

   private BCSiliconEntities() {
   }

   public static void register() {
      PACKAGE = BCRegistries.registerEntityType(
         "buildcraftsilicon", "package", Builder.of(EntityPackage::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
      );
   }
}
