/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.plug;

import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.transport.BCTransportAttachments;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class PluggableBlocker extends PipePluggable {
   private static final AABB[] BOXES = buildcraft.api.transport.pluggable.PluggableBoxes.faceBoxes(0.25, 0.75, 0.125, 0.25, 0.75, 0.875);
   private static final Identifier ADVANCEMENT_PLACE_PLUG = Identifier.parse("buildcrafttransport:plugging_the_gap");

   public PluggableBlocker(PluggableDefinition definition, IPipeHolder holder, Direction side) {
      super(definition, holder, side);
   }

   public static AABB boundingBoxFor(Direction side) {
      return BOXES[side.get3DDataValue()];
   }

   @Override
   public AABB getBoundingBox() {
      return boundingBoxFor(this.side);
   }

   @Override
   public boolean isBlocking() {
      return true;
   }

   @Override
   public ItemStack getPickStack() {
      return new ItemStack(BCTransportItems.PLUG_BLOCKER);
   }

   @Override
   public void onPlacedBy(Player player) {
      super.onPlacedBy(player);
      if (!this.holder.getPipeWorld().isClientSide() && this.holder.getPipe().isConnected(this.side)) {
         AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_PLUG);
      }

      BCTransportAttachments.recordPluggablePlacement(player, BCTransportAttachments.PluggablesPlaced.Kind.BLOCKER);
   }

   @Override
   public PluggableModelKey getModelRenderKey(Object layer) {
      return "cutout".equals(layer) ? new KeyPlugBlocker(this.side) : null;
   }

}
