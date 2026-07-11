/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.fabric.integration.rei;

import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.factory.container.ContainerDistiller;
import buildcraft.factory.container.ContainerHeatExchange;
import buildcraft.factory.integration.jei.HeatExchangerRecipePair;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.gui.IBcMenu;
import buildcraft.lib.integration.jei.BucketJeiTransfer;
import buildcraft.lib.integration.jei.JeiTransferUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.silicon.container.ContainerAssemblyTable;
import buildcraft.silicon.integration.jei.AssemblyRecipeJei;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class BcReiTransfer {
   private BcReiTransfer() {
   }

   static void registerAll(TransferHandlerRegistry registry) {
      registry.register(BcReiTransfer::craftingBlueprint);
      registry.register(BcReiTransfer::assembly);
      registry.register(BcReiTransfer::distiller);
      registry.register(BcReiTransfer::heatExchanger);
   }

   private static TransferHandler.Result craftingBlueprint(TransferHandler.Context context) {
      if (!(context.getMenu() instanceof ContainerAutoCraftItems) && !(context.getMenu() instanceof ContainerAdvancedCraftingTable)) {
         return TransferHandler.Result.createNotApplicable();
      }
      if (!BuiltinPlugin.CRAFTING.equals(context.getDisplay().getCategoryIdentifier())) {
         return TransferHandler.Result.createNotApplicable();
      }
      var recipeId = context.getDisplay().getDisplayLocation();
      if (recipeId.isEmpty()) {
         return TransferHandler.Result.createNotApplicable();
      }
      if (context.isActuallyCrafting()) {
         String idStr = recipeId.get().toString();
         ((IBcMenu) context.getMenu()).sendMessage(100, buf -> buf.writeUtf(idStr));
      }
      return TransferHandler.Result.createSuccessful().blocksFurtherHandling();
   }

   private static TransferHandler.Result assembly(TransferHandler.Context context) {
      if (!(context.getMenu() instanceof ContainerAssemblyTable container)
         || !(context.getDisplay() instanceof BcReiDisplay display)
         || !(display.recipe instanceof AssemblyRecipeJei recipe)) {
         return TransferHandler.Result.createNotApplicable();
      }
      Inventory inv = context.getMinecraft().player.getInventory();
      List<ItemStack> need = new ArrayList<>();
      for (List<ItemStack> alternatives : recipe.inputSlots()) {
         if (alternatives.isEmpty()) {
            continue;
         }
         ItemStack pick = null;
         for (ItemStack alt : alternatives) {
            if (!alt.isEmpty() && JeiTransferUtil.countMatching(inv, alt) >= alt.getCount()) {
               pick = alt;
               break;
            }
         }
         if (pick == null) {
            pick = alternatives.get(0);
         }
         mergeInto(need, pick);
      }
      if (need.isEmpty()) {
         return TransferHandler.Result.createNotApplicable();
      }
      for (ItemStack n : need) {
         if (JeiTransferUtil.countMatching(inv, n) < n.getCount()) {
            return missing();
         }
      }
      if (context.isActuallyCrafting()) {
         boolean maxTransfer = context.isStackedCrafting();
         container.sendMessage(102, buf -> {
            buf.writeBoolean(maxTransfer);
            buf.writeVarInt(need.size());
            for (ItemStack n : need) {
               buf.writeNbt(NBTUtilBC.itemStackToNBT(n));
            }
         });
      }
      return TransferHandler.Result.createSuccessful().blocksFurtherHandling();
   }

   private static TransferHandler.Result distiller(TransferHandler.Context context) {
      if (!(context.getMenu() instanceof ContainerDistiller container)
         || !(context.getDisplay() instanceof BcReiDisplay display)
         || !(display.recipe instanceof IRefineryRecipeManager.IDistillationRecipe recipe)) {
         return TransferHandler.Result.createNotApplicable();
      }
      Item bucket = bucketOf(recipe.in());
      if (bucket == Items.AIR) {
         return TransferHandler.Result.createNotApplicable();
      }
      if (JeiTransferUtil.countMatching(context.getMinecraft().player.getInventory(), new ItemStack(bucket)) < 1) {
         return missing();
      }
      if (context.isActuallyCrafting()) {
         BucketJeiTransfer.sendSingle(container, 0, bucket);
      }
      return TransferHandler.Result.createSuccessful().blocksFurtherHandling();
   }

   private static TransferHandler.Result heatExchanger(TransferHandler.Context context) {
      if (!(context.getMenu() instanceof ContainerHeatExchange container)
         || !(context.getDisplay() instanceof BcReiDisplay display)
         || !(display.recipe instanceof HeatExchangerRecipePair pair)) {
         return TransferHandler.Result.createNotApplicable();
      }
      Item slot0Bucket = bucketOf(pair.coolable().in());
      Item slot1Bucket = bucketOf(pair.heatable().in());
      if (slot0Bucket == Items.AIR || slot1Bucket == Items.AIR) {
         return TransferHandler.Result.createNotApplicable();
      }
      Inventory inv = context.getMinecraft().player.getInventory();
      if (slot0Bucket == slot1Bucket) {
         if (JeiTransferUtil.countMatching(inv, new ItemStack(slot0Bucket)) < 2) {
            return missing();
         }
      } else if (JeiTransferUtil.countMatching(inv, new ItemStack(slot0Bucket)) < 1
         || JeiTransferUtil.countMatching(inv, new ItemStack(slot1Bucket)) < 1) {
         return missing();
      }
      if (context.isActuallyCrafting()) {
         BucketJeiTransfer.sendPair(container, 0, slot0Bucket, 1, slot1Bucket);
      }
      return TransferHandler.Result.createSuccessful().blocksFurtherHandling();
   }

   private static Item bucketOf(FluidStack fluid) {
      return fluid != null && !fluid.isEmpty() ? fluid.getFluid().getBucket() : Items.AIR;
   }

   private static TransferHandler.Result missing() {
      return TransferHandler.Result.createFailed(Component.translatable("gui.jei.transfer.buildcraft.missing"));
   }

   private static void mergeInto(List<ItemStack> list, ItemStack stack) {
      for (ItemStack existing : list) {
         if (ItemStack.isSameItemSameComponents(existing, stack)) {
            existing.grow(stack.getCount());
            return;
         }
      }
      list.add(stack.copy());
   }
}
