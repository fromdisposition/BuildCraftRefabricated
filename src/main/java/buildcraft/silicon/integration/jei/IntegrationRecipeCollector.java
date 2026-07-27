/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.ItemRedstoneBoard;
import buildcraft.robotics.boards.BCBoardNBT;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.gate.EnumGateLogic;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.transport.BCTransportItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public final class IntegrationRecipeCollector {
   private static final int RING_SIZE = 8;

   private IntegrationRecipeCollector() {
   }

   public static List<IntegrationRecipeJei> collect() {
      List<IntegrationRecipeJei> out = new ArrayList<>();

      for (IntegrationRecipe recipe : IntegrationRecipeRegistry.INSTANCE.getAllRecipes()) {
         String recipeId = recipeId(recipe);
         if (recipeId.endsWith("gate_logic_toggle")) {
            collectGateToggle(recipe, recipeId, out);
         } else if (recipeId.endsWith("facade_phased")) {
            collectFacadePhased(recipe, recipeId, out);
         } else if (recipeId.endsWith("robot_integration")) {
            collectRobotIntegration(recipe, recipeId, out);
         }
      }

      out.sort(Comparator.comparing(IntegrationRecipeJei::id));
      return out;
   }

   private static void collectGateToggle(IntegrationRecipe recipe, String recipeId, List<IntegrationRecipeJei> out) {
      ItemStack chipset = new ItemStack(BCSiliconItems.CHIPSET_REDSTONE);

      for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
         if (!material.canBeModified) {
            continue;
         }

         for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
            for (EnumGateLogic logic : EnumGateLogic.VALUES) {
               ItemStack center = BCSiliconItems.PLUG_GATE.getStack(new GateVariant(logic, material, modifier));
               addIfValid(recipe, recipeId + ":" + material.tag + "_" + logic.tag + "_" + modifier.tag, center, ring(chipset), out);
            }
         }
      }
   }

   private static void collectFacadePhased(IntegrationRecipe recipe, String recipeId, List<IntegrationRecipeJei> out) {
      ItemStack center = BCSiliconItems.PLUG_FACADE.createItemStack(FacadeInstance.createSingle(FacadeStateManager.defaultState, false));
      List<FacadeBlockStateInfo> addCandidates = new ArrayList<>();

      for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
         if (info.isVisible && !info.requiredStack.isEmpty() && info != FacadeStateManager.defaultState) {
            addCandidates.add(info);
            if (addCandidates.size() >= 4) {
               break;
            }
         }
      }

      for (DyeColor color : DyeColor.values()) {
         ItemStack wire = new ItemStack(BCTransportItems.WIRE_ITEMS.get(color));
         addIfValid(recipe, recipeId + ":blocker:" + color.getName(), center, ring(wire, new ItemStack(BCTransportItems.PLUG_BLOCKER)), out);

         for (int i = 0; i < addCandidates.size(); i++) {
            FacadeBlockStateInfo info = addCandidates.get(i);
            ItemStack addFacade = BCSiliconItems.PLUG_FACADE.createItemStack(FacadeInstance.createSingle(info, false));
            addIfValid(recipe, recipeId + ":facade:" + color.getName() + ":" + i, center, ring(wire, addFacade), out);
         }
      }
   }

   private static void collectRobotIntegration(IntegrationRecipe recipe, String recipeId, List<IntegrationRecipeJei> out) {
      ItemStack center = new ItemStack(BCRoboticsItems.ROBOT);

      for (BCBoardNBT board : BCBoardNBT.REGISTRY.values()) {
         ItemStack boardStack = ItemRedstoneBoard.createStack(board);
         addIfValid(recipe, recipeId + ":" + board.getID(), center, ring(boardStack), out);
      }
   }

   private static void addIfValid(
      IntegrationRecipe recipe, String id, ItemStack center, List<ItemStack> ring, List<IntegrationRecipeJei> out
   ) {
      ItemStack output = recipe.getOutput(center.copy(), toIntegrate(ring));
      if (!output.isEmpty()) {
         out.add(new IntegrationRecipeJei(id, center.copy(), copyRing(ring), output, recipe.getRequiredMicroJoules(output)));
      }
   }

   private static String recipeId(IntegrationRecipe recipe) {
      Object name = recipe.name;
      return name instanceof Identifier id ? id.toString() : name.toString();
   }

   private static NonNullList<ItemStack> toIntegrate(List<ItemStack> ring) {
      NonNullList<ItemStack> list = NonNullList.withSize(RING_SIZE, ItemStack.EMPTY);

      for (int i = 0; i < RING_SIZE; i++) {
         if (!ring.get(i).isEmpty()) {
            list.set(i, ring.get(i));
         }
      }

      return list;
   }

   private static List<ItemStack> ring(ItemStack... stacks) {
      List<ItemStack> ring = new ArrayList<>(Collections.nCopies(RING_SIZE, ItemStack.EMPTY));

      for (int i = 0; i < stacks.length && i < RING_SIZE; i++) {
         if (!stacks[i].isEmpty()) {
            ring.set(i, stacks[i]);
         }
      }

      return ring;
   }

   private static List<ItemStack> copyRing(List<ItemStack> ring) {
      List<ItemStack> copy = ring();

      for (int i = 0; i < RING_SIZE; i++) {
         if (!ring.get(i).isEmpty()) {
            copy.set(i, ring.get(i).copy());
         }
      }

      return copy;
   }

   private static List<ItemStack> ring() {
      return new ArrayList<>(Collections.nCopies(RING_SIZE, ItemStack.EMPTY));
   }
}
