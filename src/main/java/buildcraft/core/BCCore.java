/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import buildcraft.core.marker.PathCache;
import buildcraft.core.marker.VolumeCache;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;
import buildcraft.lib.block.VanillaPaintHandlers;
import buildcraft.lib.block.VanillaRotationHandlers;
import buildcraft.lib.fluids.SimpleFluidContent;
import buildcraft.lib.list.VanillaListHandlers;
import buildcraft.lib.marker.MarkerCache;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.DyeColor;

public final class BCCore {
   public static final String MODID = "buildcraftcore";
   public static final boolean DEV = Boolean.getBoolean("buildcraft.dev");
   public static DataComponentType<SimpleFluidContent> FLUID_CONTENT;
   public static DataComponentType<DyeColor> BRUSH_COLOR;
   public static DataComponentType<Integer> BRUSH_USES;

   private BCCore() {
   }

   public static void register() {
      registerDataComponents();
      BCCoreBlocks.register();
      BCCoreItems.register();
      BCCoreBlockEntities.register();
      BCCoreMenuTypes.register();
      BCCoreFeatures.register();
      BCCoreCreativeTabs.register();
      BCLib.init();
      preInit();
   }

   private static void registerDataComponents() {
      FLUID_CONTENT = BCRegistries.registerDataComponent(
         "buildcraftcore", "fluid_content", b -> b.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC)
      );
      BRUSH_COLOR = BCRegistries.registerDataComponent(
         "buildcraftcore", "brush_color", b -> b.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
      );
      BRUSH_USES = BCRegistries.registerDataComponent("buildcraftcore", "brush_uses", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));
   }

   private static void preInit() {
      MarkerCache.registerCache(VolumeCache.INSTANCE);
      MarkerCache.registerCache(PathCache.INSTANCE);
      BCCoreStatements.preInit();
      VanillaListHandlers.register();
      VanillaPaintHandlers.init();
      VanillaRotationHandlers.init();
   }
}
