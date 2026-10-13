/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders;

import buildcraft.builders.entity.EntityQuarryRig;
import buildcraft.fabric.BCRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public final class BCBuildersEntities {
   public static EntityType<EntityQuarryRig> QUARRY_RIG;

   private BCBuildersEntities() {
   }

   public static void register() {
      QUARRY_RIG = BCRegistries.registerEntityType(
         "buildcraftbuilders", "quarry_rig", Builder.of(EntityQuarryRig::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(1)
      );
   }
}
