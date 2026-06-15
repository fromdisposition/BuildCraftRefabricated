/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.lib.fabric.BcRegistryUtil;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.silicon.recipe.FacadeAssemblyRecipes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.util.context.ContextMap.Builder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public final class AssemblyRecipeCollector {
   private AssemblyRecipeCollector() {
   }

   public static List<AssemblyRecipeJei> collect() {
      List<AssemblyRecipeJei> out = new ArrayList<>();

      for (AssemblyRecipe recipe : AssemblyRecipeRegistry.REGISTRY.values()) {
         if (recipe instanceof FacadeAssemblyRecipes facade) {
            out.addAll(collectFacade(facade));
         } else {
            collectStandard(recipe, out);
         }
      }

      out.sort(Comparator.comparing(AssemblyRecipeJei::id));
      return out;
   }

   private static void collectStandard(AssemblyRecipe recipe, List<AssemblyRecipeJei> out) {
      ContextMap displayCtx = displayContext();

      for (ItemStack output : recipe.getOutputPreviews()) {
         if (!output.isEmpty()) {
            List<List<ItemStack>> inputSlots = new ArrayList<>();

            for (IngredientStack ing : recipe.getInputsFor(output)) {
               List<ItemStack> slot = new ArrayList<>();

               for (ItemStack template : ing.ingredient.display().resolveForStacks(displayCtx)) {
                  if (!template.isEmpty() && template.getItem() != Items.AIR) {
                     ItemStack stack = template.copy();
                     stack.setCount(ing.count);
                     slot.add(stack);
                  }
               }

               if (!slot.isEmpty()) {
                  inputSlots.add(slot);
               }
            }

            if (!inputSlots.isEmpty()) {
               String id = recipe.getRegistryName() + ":" + outputKey(output);
               out.add(new AssemblyRecipeJei(id, inputSlots, List.of(output), recipe.getRequiredMicroJoulesFor(output)));
            }
         }
      }
   }

   private static List<AssemblyRecipeJei> collectFacade(FacadeAssemblyRecipes facade) {
      Item structurePipe = BcRegistryUtil.getItem(Identifier.parse("buildcrafttransport:pipe_structure"));
      if (structurePipe == Items.AIR) {
         return List.of();
      }

      List<ItemStack> baseSlot = List.of(new ItemStack(structurePipe, 3));
      LinkedHashMap<ItemStackKey, FacadeBlockStateInfo> uniqueBlocks = new LinkedHashMap<>();

      for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
         if (info.isVisible && !info.requiredStack.isEmpty()) {
            uniqueBlocks.putIfAbsent(new ItemStackKey(info.requiredStack), info);
         }
      }

      if (uniqueBlocks.isEmpty()) {
         return List.of();
      }

      List<ItemStack> blockSlot = new ArrayList<>(uniqueBlocks.size());
      List<ItemStack> basicOutputs = new ArrayList<>(uniqueBlocks.size());
      List<ItemStack> hollowOutputs = new ArrayList<>(uniqueBlocks.size());

      for (FacadeBlockStateInfo info : uniqueBlocks.values()) {
         blockSlot.add(info.requiredStack.copy());
         basicOutputs.add(FacadeAssemblyRecipes.createFacadeStack(info, false));
         hollowOutputs.add(FacadeAssemblyRecipes.createFacadeStack(info, true));
      }

      long mjCost = 64L * MjAPI.MJ;
      List<List<ItemStack>> inputSlots = List.of(baseSlot, blockSlot);
      return List.of(
         new AssemblyRecipeJei(facade.getRegistryName() + ":basic", inputSlots, basicOutputs, mjCost, 1),
         new AssemblyRecipeJei(facade.getRegistryName() + ":hollow", inputSlots, hollowOutputs, mjCost, 1)
      );
   }

   private static String outputKey(ItemStack output) {
      Identifier id = BuiltInRegistries.ITEM.getKey(output.getItem());
      return id + "@" + output.getComponents().hashCode();
   }

   private static ContextMap displayContext() {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc == null ? null : mc.level;
      return level != null ? SlotDisplayContext.fromLevel(level) : new Builder().create(SlotDisplayContext.CONTEXT);
   }
}
