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
   private static final AABB[] BOXES = new AABB[6];
   private static final Identifier ADVANCEMENT_PLACE_PLUG = Identifier.parse("buildcrafttransport:plugging_the_gap");

   public PluggableBlocker(PluggableDefinition definition, IPipeHolder holder, Direction side) {
      super(definition, holder, side);
   }

   public static AABB boundingBoxFor(Direction side) {
      return BOXES[side.ordinal()];
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

   static {
      double ll = 0.125;
      double lu = 0.25;
      double ul = 0.75;
      double uu = 0.875;
      double min = 0.25;
      double max = 0.75;
      BOXES[Direction.DOWN.ordinal()] = new AABB(min, ll, min, max, lu, max);
      BOXES[Direction.UP.ordinal()] = new AABB(min, ul, min, max, uu, max);
      BOXES[Direction.NORTH.ordinal()] = new AABB(min, min, ll, max, max, lu);
      BOXES[Direction.SOUTH.ordinal()] = new AABB(min, min, ul, max, max, uu);
      BOXES[Direction.WEST.ordinal()] = new AABB(ll, min, min, lu, max, max);
      BOXES[Direction.EAST.ordinal()] = new AABB(ul, min, min, uu, max, max);
   }
}
