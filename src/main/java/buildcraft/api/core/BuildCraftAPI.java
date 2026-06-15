/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class BuildCraftAPI {
   public static IFakePlayerProvider fakePlayerProvider;
   public static final Set<Block> softBlocks = Sets.newHashSet();
   public static final HashMap<String, IWorldProperty> worldProperties = Maps.newHashMap();

   private BuildCraftAPI() {
   }

   public static String getVersion() {
      return "26.1.2-beta-2-upstream3";
   }

   public static IWorldProperty getWorldProperty(String name) {
      return worldProperties.get(name);
   }

   public static void registerWorldProperty(String name, IWorldProperty property) {
      if (worldProperties.containsKey(name)) {
         BCLog.logger.warn("The WorldProperty key '" + name + "' is being overridden with " + property.getClass().getSimpleName() + "!");
      }

      worldProperties.put(name, property);
   }

   public static boolean isSoftBlock(Level world, BlockPos pos) {
      return worldProperties.get("soft").get(world, pos);
   }

   public static Identifier nameToResourceLocation(String name) {
      if (name.indexOf(58) > 0) {
         return Identifier.parse(name);
      } else {
         throw new IllegalStateException("Illegal name " + name + ". Provide domain id (namespace:path) to register it correctly.");
      }
   }
}
