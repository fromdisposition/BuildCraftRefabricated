/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.plug;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.transport.BCTransportAttachments;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class PluggablePowerAdaptor extends PipePluggable {
   private static final AABB[] BOXES = new AABB[6];

   public PluggablePowerAdaptor(PluggableDefinition definition, IPipeHolder holder, Direction side) {
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
      return new ItemStack(BCTransportItems.PLUG_POWER_ADAPTOR);
   }

   @Override
   public void onPlacedBy(Player player) {
      super.onPlacedBy(player);
      BCTransportAttachments.recordPluggablePlacement(player, BCTransportAttachments.PluggablesPlaced.Kind.POWER_ADAPTOR);
   }

   @Nullable
   @Override
   public PluggableModelKey getModelRenderKey(Object layer) {
      return "cutout".equals(layer) ? new KeyPlugPowerAdaptor(this.side) : null;
   }

   @Override
   public <T> T getCapability(@Nonnull Object cap) {
      return cap != MjAPI.CAP_CONNECTOR && cap != MjAPI.CAP_RECEIVER && cap != MjAPI.CAP_REDSTONE_RECEIVER
         ? null
         : this.holder.getPipe().getBehaviour().getCapability(cap, this.side);
   }

   static {
      double ll = 0.0;
      double lu = 0.25;
      double ul = 0.75;
      double uu = 1.0;
      double min = 0.1875;
      double max = 0.8125;
      BOXES[Direction.DOWN.ordinal()] = new AABB(min, ll, min, max, lu, max);
      BOXES[Direction.UP.ordinal()] = new AABB(min, ul, min, max, uu, max);
      BOXES[Direction.NORTH.ordinal()] = new AABB(min, min, ll, max, max, lu);
      BOXES[Direction.SOUTH.ordinal()] = new AABB(min, min, ul, max, max, uu);
      BOXES[Direction.WEST.ordinal()] = new AABB(ll, min, min, lu, max, max);
      BOXES[Direction.EAST.ordinal()] = new AABB(ul, min, min, uu, max, max);
   }
}
