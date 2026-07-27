/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

import buildcraft.transport.tile.TilePipeHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class PipeHolderRenderState extends BlockEntityRenderState {
   public TilePipeHolder pipe;
   public float partialTick;
   /**
    * Backing pool of entries — grown on demand and reused across frames (never holds more than its high-water
    * mark). Only the first {@link #itemEntryCount} are live this frame; iterate with that bound, not size().
    */
   public final List<PipeHolderRenderState.ItemRenderEntry> itemEntries = new ArrayList<>();
   public int itemEntryCount;
   private final List<ItemStackRenderState> itemStatePool = new ArrayList<>();
   private int itemStatePoolUsed;

   public void beginItemExtraction() {
      this.itemEntryCount = 0;
      this.itemStatePoolUsed = 0;
   }

   public ItemStackRenderState acquireItemState() {
      if (this.itemStatePoolUsed >= this.itemStatePool.size()) {
         this.itemStatePool.add(new ItemStackRenderState());
      }

      return this.itemStatePool.get(this.itemStatePoolUsed++);
   }

   /** Returns a pooled, reusable entry for this frame; the caller must populate it via {@link ItemRenderEntry#set}. */
   public PipeHolderRenderState.ItemRenderEntry acquireItemEntry() {
      if (this.itemEntryCount >= this.itemEntries.size()) {
         this.itemEntries.add(new PipeHolderRenderState.ItemRenderEntry());
      }

      return this.itemEntries.get(this.itemEntryCount++);
   }

   public static class ItemRenderEntry {
      public ItemStackRenderState renderState;
      public double posX;
      public double posY;
      public double posZ;
      public Direction direction;
      public DyeColor colour;
      public int stackCount;

      public void set(ItemStackRenderState renderState, double posX, double posY, double posZ, Direction direction, DyeColor colour, int stackCount) {
         this.renderState = renderState;
         this.posX = posX;
         this.posY = posY;
         this.posZ = posZ;
         this.direction = direction;
         this.colour = colour;
         this.stackCount = stackCount;
      }
   }
}
