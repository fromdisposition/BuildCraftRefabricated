/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.container;

import buildcraft.lib.gui.ContainerBCTileRecipeBook;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ContainerAdvancedCraftingTable extends ContainerBCTileRecipeBook<TileAdvancedCraftingTable> {
   private final List<Slot> blueprintSlots = new ArrayList<>();

   public ContainerAdvancedCraftingTable(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv.player, getTile(playerInv, pos));
   }

   public ContainerAdvancedCraftingTable(int containerId, Player player, TileAdvancedCraftingTable tile) {
      super(BCSiliconMenuTypes.ADVANCED_CRAFTING_TABLE, containerId, player, tile);
      // Null tile = the client factory could not resolve the block entity yet (not synced); dereferencing
      // tile.resultClient / tile.invMaterials here NPEs the client. Still add the player inventory so the slot
      // count matches the server, else the content packet indexes past an empty slot list and disconnects.
      if (tile == null) {
         this.addFullPlayerInventory(8, 153);
      } else {
         this.addSlot(new SlotDisplay(i -> tile.resultClient, 0, 127, 33));

         for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 5; x++) {
               this.addSlot(new SlotBase(tile.invMaterials, x + y * 5, 15 + x * 18, 85 + y * 18));
            }
         }

         for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
               this.addSlot(new SlotOutput(tile.invResults, x + y * 3, 109 + x * 18, 85 + y * 18));
            }
         }

         for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
               Slot slot = new SlotPhantom(tile.invBlueprint, x + y * 3, 33 + x * 18, 16 + y * 18, false);
               this.addSlot(slot);
               this.blueprintSlots.add(slot);
            }
         }

         this.addFullPlayerInventory(8, 153);
      }
   }

   public List<Slot> getInputGridSlots() {
      return this.blueprintSlots;
   }

   public int getGridWidth() {
      return 3;
   }

   public int getGridHeight() {
      return 3;
   }

   public Slot getResultSlot() {
      return (Slot)this.slots.get(0);
   }

   @Override
   protected void handleRecipeTransfer(RecipeHolder<CraftingRecipe> recipe, ServerLevel level, Inventory playerInv) {
      if (recipe.value() instanceof CraftingRecipe craftingRecipe) {
         CraftingUtil.placeRecipeInBlueprint(craftingRecipe, this.tile.invBlueprint, level);
      }
   }

   private static TileAdvancedCraftingTable getTile(Inventory inv, BlockPos pos) {
      return inv.player.level().getBlockEntity(pos) instanceof TileAdvancedCraftingTable t ? t : null;
   }
}
