/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.tile.ItemHandlerSimple;
import net.minecraft.network.FriendlyByteBuf;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
//? if >= 1.21.10 {
import net.minecraft.world.entity.player.StackedItemContents;
//?} else {
/*import net.minecraft.world.entity.player.StackedContents;
*///?}
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
//? if >= 1.21.10 {
import net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction;
//?}
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

//? if >= 1.21.10 {
public abstract class BcMenuRecipeBook extends RecipeBookMenu implements IBcMenu {
//?} else {
/*public abstract class BcMenuRecipeBook extends RecipeBookMenu<net.minecraft.world.item.crafting.CraftingInput, net.minecraft.world.item.crafting.CraftingRecipe> implements IBcMenu {
*///?}
   public static final int NET_WIDGET = BcMenu.NET_WIDGET;
   public static final int NET_JEI_RECIPE_TRANSFER = 100;
   public static final int NET_GHOST_SLOT_SET = BcMenu.NET_GHOST_SLOT_SET;
   public static final int NET_BUCKET_TRANSFER = BcMenu.NET_BUCKET_TRANSFER;
   public final Player player;
   private final List<Widget_Neptune<?>> widgets = new ArrayList<>();

   protected BcMenuRecipeBook(MenuType<?> menuType, int containerId, Player player) {
      super(menuType, containerId);
      this.player = player;
   }

   protected void addFullPlayerInventory(int startX, int startY) {
      this.addFullPlayerInventory(startX, startY, this.player.getInventory());
   }

   protected void addFullPlayerInventory(int startX, int startY, Inventory inv) {
      // addStandardInventorySlots lets mods that mixin-extend the player inventory (e.g. Inventory Extended) add
      // their extra rows to BC GUIs too; 1.21.1 has no such helper, so it keeps the manual loop.
      //? if >= 1.21.10 {
      this.addStandardInventorySlots(inv, startX, startY);
      //?} else {
      /*for (int sy = 0; sy < 3; sy++) {
         for (int sx = 0; sx < 9; sx++) {
            this.addSlot(new Slot(inv, sx + sy * 9 + 9, startX + sx * 18, startY + sy * 18));
         }
      }

      for (int sx = 0; sx < 9; sx++) {
         this.addSlot(new Slot(inv, sx, startX + sx * 18, startY + 58));
      }
      *///?}
   }

   public <W extends Widget_Neptune<?>> W addWidget(W widget) {
      if (widget == null) {
         throw new NullPointerException("widget");
      }

      this.widgets.add(widget);
      return widget;
   }

   public ImmutableList<Widget_Neptune<?>> getWidgets() {
      return ImmutableList.copyOf(this.widgets);
   }

   @Override
   public final void sendMessage(int id, IPayloadWriter writer) {
      BcMenuSupport.sendMessage(this, this.player, id, writer);
   }

   @Override
   public Player getPlayer() { return player; }

   @Override
   public void sendWidgetData(Widget_Neptune<?> widget, IPayloadWriter writer) {
      BcMenuSupport.sendWidgetData(this, this.widgets, widget, writer);
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == NET_JEI_RECIPE_TRANSFER && !isClient) {
         Identifier recipeId = Identifier.tryParse(buffer.readUtf());
         if (recipeId == null) {
            return;
         }

         if (this.player.level() instanceof ServerLevel serverLevel) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, recipeId);
            //? if >= 1.21.10 {
            Optional<RecipeHolder<CraftingRecipe>> holder = serverLevel.recipeAccess()
               .byKey(key)
               .flatMap(r -> r.value() instanceof CraftingRecipe crafting ? Optional.of(new RecipeHolder<>(r.id(), crafting)) : Optional.empty());
            //?} else {
            /*Optional<RecipeHolder<CraftingRecipe>> holder = serverLevel.getRecipeManager()
               .byKey(recipeId)
               .flatMap(r -> r.value() instanceof CraftingRecipe crafting ? Optional.of(new RecipeHolder<>(r.id(), crafting)) : Optional.empty());
            *///?}
            holder.ifPresent(recipe -> this.handleRecipeTransfer(recipe, serverLevel, this.player.getInventory()));
         }
      } else {
         BcMenuSupport.readMessage(this, this.player, this.widgets, this::getBucketTransferSlots, id, buffer, isClient, ctx);
      }
   }

   /** Override to populate blueprint/grid when a recipe is transferred (JEI or vanilla recipe book). */
   protected void handleRecipeTransfer(RecipeHolder<CraftingRecipe> recipe, ServerLevel level, Inventory playerInv) {
   }

   @javax.annotation.Nullable
   protected ItemHandlerSimple getBucketTransferSlots() {
      return null;
   }

   @Override
   public void clicked(int slotId, int dragType, ContainerInput containerInput, Player player) {
      if (!BcMenuSupport.clickPhantom(this, slotId)) {
         super.clicked(slotId, dragType, containerInput, player);
      }
   }

   @Override
   public ItemStack quickMoveStack(Player playerIn, int index) {
      return BcMenuSupport.quickMoveStack(this, index, this::moveItemStackTo);
   }

   @Override
   public boolean stillValid(Player player) {
      return player.isAlive() && !player.isRemoved();
   }

   //? if >= 1.21.10 {
   @Override
   public PostPlaceAction handlePlacement(boolean useMaxItems, boolean isCreative, RecipeHolder<?> recipe, ServerLevel level, Inventory playerInv) {
      if (recipe.value() instanceof CraftingRecipe crafting) {
         handleRecipeTransfer(new RecipeHolder<>(recipe.id(), crafting), level, playerInv);
         return PostPlaceAction.PLACE_GHOST_RECIPE;
      }
      return PostPlaceAction.NOTHING;
   }

   @Override
   public void fillCraftSlotsStackedContents(StackedItemContents contents) {
   }
   //?} else {
   /*// 1.21.1 RecipeBookMenu<I,R> abstract surface. The recipe-book panel is largely non-functional on 1.21.1
   // (BC's components are versions/1.21.1 stubs); these satisfy the contract with safe defaults.
   @Override
   public void fillCraftSlotsStackedContents(StackedContents contents) {
   }

   @Override
   public void clearCraftingContent() {
   }

   @Override
   public boolean recipeMatches(RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe> recipe) {
      return false;
   }

   @Override
   public int getResultSlotIndex() {
      return -1;
   }

   @Override
   public int getGridWidth() {
      return 3;
   }

   @Override
   public int getGridHeight() {
      return 3;
   }

   @Override
   public int getSize() {
      return 10;
   }

   @Override
   public boolean shouldMoveToInventory(int slotIndex) {
      return true;
   }
   *///?}

   @Override
   public RecipeBookType getRecipeBookType() {
      return RecipeBookType.CRAFTING;
   }
}
