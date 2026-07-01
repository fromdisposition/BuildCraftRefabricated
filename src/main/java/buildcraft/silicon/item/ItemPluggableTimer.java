/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.item;

import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.plug.PluggableTimer;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemPluggableTimer extends Item implements IItemPluggable {
   public ItemPluggableTimer(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
      PluggableDefinition def = BCSiliconPlugs.timer;
      if (def != null && def.creator != null) {
         SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
         return new PluggableTimer(def, holder, side);
      } else {
         return null;
      }
   }
}
