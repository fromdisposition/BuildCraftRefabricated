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
   private static final AABB[] BOXES = buildcraft.api.transport.pluggable.PluggableBoxes.faceBoxes(0.1875, 0.8125, 0.0, 0.25, 0.75, 1.0);

   public PluggablePowerAdaptor(PluggableDefinition definition, IPipeHolder holder, Direction side) {
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

}
