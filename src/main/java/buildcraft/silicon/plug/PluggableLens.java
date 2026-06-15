/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.api.transport.pipe.IPipeEventBus;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.model.key.KeyPlugLens;
import buildcraft.transport.BCTransportAttachments;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class PluggableLens extends PipePluggable {
   private static final AABB[] BOXES = new AABB[6];
   @Nullable
   public final DyeColor colour;
   public final boolean isFilter;

   public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, @Nullable DyeColor colour, boolean isFilter) {
      super(def, holder, side);
      this.colour = colour;
      this.isFilter = isFilter;
   }

   public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt) {
      super(def, holder, side);
      DyeColor loaded = null;
      if (nbt.contains("colour")) {
         String name = nbt.getStringOr("colour", "");
         loaded = DyeColor.byName(name, null);
      }

      this.colour = loaded;
      this.isFilter = nbt.getBooleanOr("f", false);
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      if (this.colour != null) {
         nbt.putString("colour", this.colour.getName());
      }

      nbt.putBoolean("f", this.isFilter);
      return nbt;
   }

   public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
      super(def, holder, side);
      int colourId = buffer.readByte();
      this.colour = colourId >= 0 ? DyeColor.byId(colourId) : null;
      this.isFilter = buffer.readBoolean();
   }

   @Override
   public void writeCreationPayload(FriendlyByteBuf buffer) {
      buffer.writeByte(this.colour != null ? this.colour.getId() : -1);
      buffer.writeBoolean(this.isFilter);
   }

   public static AABB boundingBoxFor(Direction side) {
      return BOXES[side.ordinal()];
   }

   @Override
   public AABB getBoundingBox() {
      return boundingBoxFor(this.side);
   }

   @Override
   public ItemStack getPickStack() {
      return BCSiliconItems.PLUG_LENS.getStack(this.colour, this.isFilter);
   }

   @Override
   public PluggableModelKey getModelRenderKey(Object layer) {
      if (layer == null) {
         return null;
      }

      String name = layer.toString().toLowerCase();
      return !name.contains("cutout") && !name.contains("translucent") ? null : new KeyPlugLens(layer, this.side, this.colour, this.isFilter);
   }

   @Override
   public boolean isBlocking() {
      return false;
   }

   @Override
   public void onPlacedBy(Player player) {
      super.onPlacedBy(player);
      BCTransportAttachments.recordPluggablePlacement(player, BCTransportAttachments.PluggablesPlaced.Kind.LENS);
   }

   public void tryInsert(PipeEventItem.TryInsert tryInsert) {
      if (this.isFilter && tryInsert.from == this.side) {
         DyeColor itemColour = tryInsert.colour;
         if (itemColour != null && itemColour != this.colour) {
            tryInsert.cancel();
         }
      }
   }

   public void sideCheck(PipeEventItem.SideCheck event) {
      if (this.isFilter) {
         if (event.colour == this.colour) {
            event.increasePriority(this.side);
         } else if (event.colour != null) {
            event.disallow(this.side);
         } else {
            event.decreasePriority(this.side);
         }
      }
   }

   public void beforeInsert(PipeEventItem.OnInsert event) {
      if (!this.isFilter && event.from == this.side) {
         event.colour = this.colour;
      }
   }

   public void reachEnd(PipeEventItem.ReachEnd event) {
      if (!this.isFilter && event.to == this.side) {
         event.colour = this.colour;
      }
   }

   @Override
   public void registerEventHandlers(IPipeEventBus bus) {
      bus.on(PipeEventItem.TryInsert.class, this, this::tryInsert);
      bus.on(PipeEventItem.SideCheck.class, this, this::sideCheck);
      bus.on(PipeEventItem.OnInsert.class, this, this::beforeInsert);
      bus.on(PipeEventItem.ReachEnd.class, this, this::reachEnd);
   }

   static {
      double ll = 0.0;
      double lu = 0.125;
      double ul = 0.875;
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
