/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory;

import buildcraft.fabric.BCRegistries;
import buildcraft.factory.entity.EntityMinerShaft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public final class BCFactoryEntities {
   public static EntityType<EntityMinerShaft> MINER_SHAFT;

   private BCFactoryEntities() {
   }

   public static void register() {
      MINER_SHAFT = BCRegistries.registerEntityType(
         "buildcraftfactory",
         "miner_shaft",
         Builder.of(EntityMinerShaft::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(1)
      );
   }
}
