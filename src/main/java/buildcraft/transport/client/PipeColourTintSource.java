/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.BCTransportItems;
//? if >= 1.21.10 {
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
//?}
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

//? if >= 1.21.10 {
public final class PipeColourTintSource implements ItemTintSource {
//?} else {
/*public final class PipeColourTintSource {
*///?}
   public static final PipeColourTintSource INSTANCE = new PipeColourTintSource();
   //? if >= 1.21.10 {
   public static final MapCodec<PipeColourTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);
   //?}
   private static final int OVERLAY_ALPHA = 76;

   private PipeColourTintSource() {
   }

   public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
      DyeColor col = (DyeColor)stack.get(BCTransportItems.PIPE_COLOUR);
      return col != null ? 1275068416 | ColourUtil.getLightHex(col) : 1291845631;
   }

   //? if >= 1.21.10 {
   public MapCodec<? extends ItemTintSource> type() {
      return MAP_CODEC;
   }
   //?}
}
