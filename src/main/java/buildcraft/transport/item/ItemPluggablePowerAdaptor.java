/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.plug.PluggablePowerAdaptor;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.phys.AABB;

public class ItemPluggablePowerAdaptor extends Item implements IItemPluggable {
   public ItemPluggablePowerAdaptor(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
      IPipe pipe = holder.getPipe();
      if (pipe == null) {
         return null;
      } else {
         PipeBehaviour behaviour = pipe.getBehaviour();
         if (!(behaviour instanceof IMjRedstoneReceiver)) {
            return null;
         } else {
            PluggableDefinition def = BCTransportPlugs.powerAdaptor;
            if (def != null && def.creator != null) {
               SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
               return new PluggablePowerAdaptor(def, holder, side);
            } else {
               return null;
            }
         }
      }
   }

   @Override
   public AABB getPlacementBoundingBox(ItemStack stack, Direction side) {
      return PluggablePowerAdaptor.boundingBoxFor(side);
   }
}
