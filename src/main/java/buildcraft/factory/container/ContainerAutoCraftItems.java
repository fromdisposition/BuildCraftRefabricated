/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.container;

import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TileAutoWorkbenchItems;
import buildcraft.lib.fabric.menu.MenuBlockEntityLookup;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotDisplay;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.misc.CraftingUtil;
import buildcraft.lib.tile.ItemHandlerSimple;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ContainerAutoCraftItems extends ContainerBCTile<TileAutoWorkbenchItems> {
   private static final ItemHandlerSimple FALLBACK_RESULT = createFallbackHandler(1, false);
   private static final ItemHandlerSimple FALLBACK_BLUEPRINT = createFallbackHandler(9, true);
   private static final ItemHandlerSimple FALLBACK_MATERIAL_FILTER = createFallbackHandler(9, true);
   private static final ItemHandlerSimple FALLBACK_MATERIALS = createFallbackHandler(9, true);
   private final List<Slot> blueprintSlots = new ArrayList<>();
   private final Slot resultSlot;
   public final SlotBase[] materialSlots;

   public ContainerAutoCraftItems(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, MenuBlockEntityLookup.get(playerInv, pos, TileAutoWorkbenchItems.class));
   }

   public ContainerAutoCraftItems(int containerId, Inventory playerInv, final TileAutoWorkbenchItems tile) {
      super(BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS, containerId, playerInv.player, tile);
      ItemHandlerSimple invResult = tile != null ? tile.invResult : FALLBACK_RESULT;
      ItemHandlerSimple invBlueprint = tile != null ? tile.invBlueprint : FALLBACK_BLUEPRINT;
      ItemHandlerSimple invMaterialFilter = tile != null ? tile.invMaterialFilter : FALLBACK_MATERIAL_FILTER;
      ItemHandlerSimple invMaterials = tile != null ? tile.invMaterials : FALLBACK_MATERIALS;
      this.resultSlot = this.addSlot(new SlotOutput(invResult, 0, 124, 35));

      for (int y = 0; y < 3; y++) {
         for (int x = 0; x < 3; x++) {
            Slot slot = new SlotPhantom(invBlueprint, x + y * 3, 30 + x * 18, 17 + y * 18, false);
            this.addSlot(slot);
            this.blueprintSlots.add(slot);
         }
      }

      for (int x = 0; x < 9; x++) {
         this.addSlot(new SlotPhantom(invMaterialFilter, x, -1000000, -1000000));
      }

      this.materialSlots = new SlotBase[9];

      for (int x = 0; x < 9; x++) {
         this.materialSlots[x] = new SlotBase(invMaterials, x, 8 + x * 18, 84);
         this.addSlot(this.materialSlots[x]);
      }

      this.addSlot(new SlotDisplay(i -> tile != null ? tile.resultClient : ItemStack.EMPTY, 0, 93, 27));
      this.addDataSlot(new DataSlot() {
         public int get() {
            return tile != null ? (int)(tile.getPowerStored() & 4294967295L) : 0;
         }

         public void set(int value) {
            if (tile != null) {
               long current = tile.getPowerStored();
               tile.setPowerStored(current & -4294967296L | value & 4294967295L);
            }
         }
      });
      this.addDataSlot(new DataSlot() {
         public int get() {
            return tile != null ? (int)(tile.getPowerStored() >>> 32) : 0;
         }

         public void set(int value) {
            if (tile != null) {
               long current = tile.getPowerStored();
               tile.setPowerStored(current & 4294967295L | (long)value << 32);
            }
         }
      });
      this.addFullPlayerInventory(8, 115);
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
      return this.resultSlot;
   }

   @Override
   public PostPlaceAction handlePlacement(boolean useMaxItems, boolean isCreative, RecipeHolder<?> recipe, ServerLevel level, Inventory playerInv) {
      if (this.tile == null) {
         return PostPlaceAction.NOTHING;
      } else if (recipe.value() instanceof CraftingRecipe craftingRecipe) {
         CraftingUtil.placeRecipeInBlueprint(craftingRecipe, this.tile.invBlueprint, level);
         return PostPlaceAction.PLACE_GHOST_RECIPE;
      } else {
         return PostPlaceAction.NOTHING;
      }
   }

   @Override
   public void fillCraftSlotsStackedContents(StackedItemContents contents) {
      if (this.tile != null) {
         for (int i = 0; i < this.tile.invMaterials.getSlots(); i++) {
            contents.accountStack(this.tile.invMaterials.getStackInSlot(i));
         }
      }
   }

   @Override
   public RecipeBookType getRecipeBookType() {
      return RecipeBookType.CRAFTING;
   }

   private static ItemHandlerSimple createFallbackHandler(int slots, boolean allowInput) {
      ItemHandlerSimple handler = new ItemHandlerSimple(slots, 1);
      handler.setChecker((slot, stack) -> allowInput);
      return handler;
   }
}
