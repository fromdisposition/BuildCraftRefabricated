package buildcraft.silicon;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.silicon.gate.EnumGateLogic;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadePhasedState;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.item.ItemWire;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public final class BCSiliconIntegrationRecipes {
   private static final long INTEGRATION_COST = 25000L * MjAPI.MJ;
   private static boolean initialized;

   private BCSiliconIntegrationRecipes() {
   }

   public static void init() {
      if (!initialized) {
         initialized = true;
         IntegrationRecipeRegistry.INSTANCE.addRecipe(new BCSiliconIntegrationRecipes.GateLogicToggleRecipe());
         IntegrationRecipeRegistry.INSTANCE.addRecipe(new BCSiliconIntegrationRecipes.FacadePhasedRecipe());
      }
   }

   private static final class FacadePhasedRecipe extends IntegrationRecipe {
      private static final IngredientStack CENTER = new IngredientStack(Ingredient.of(BCSiliconItems.PLUG_FACADE));

      FacadePhasedRecipe() {
         super(Identifier.fromNamespaceAndPath("buildcraftsilicon", "facade_phased"));
      }

      @Override
      public ItemStack getOutput(@Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate) {
         if (!(target.getItem() instanceof ItemPluggableFacade)) {
            return ItemStack.EMPTY;
         }

         FacadeInstance targetFacade = ItemPluggableFacade.getStates(target);
         if (targetFacade.phasedStates.length == 0) {
            return ItemStack.EMPTY;
         }

         ItemWire wire = null;
         FacadeInstance addFacade = null;
         boolean hasBlocker = false;

         for (ItemStack stack : toIntegrate) {
            if (!stack.isEmpty()) {
               if (stack.getItem() instanceof ItemWire itemWire) {
                  if (wire != null) {
                     return ItemStack.EMPTY;
                  }

                  wire = itemWire;
               } else if (stack.is(BCSiliconItems.PLUG_FACADE)) {
                  FacadeInstance facade = ItemPluggableFacade.getStates(stack);
                  if (facade.phasedStates.length != 1) {
                     return ItemStack.EMPTY;
                  }

                  if (addFacade != null) {
                     return ItemStack.EMPTY;
                  }

                  addFacade = facade;
               } else {
                  if (!stack.is(BCTransportItems.PLUG_BLOCKER)) {
                     return ItemStack.EMPTY;
                  }

                  if (hasBlocker) {
                     return ItemStack.EMPTY;
                  }

                  hasBlocker = true;
               }
            }
         }

         if (wire != null && (addFacade != null || hasBlocker)) {
            FacadePhasedState additionalState = hasBlocker
               ? new FacadePhasedState(FacadeStateManager.defaultState, wire.getColor())
               : new FacadePhasedState(addFacade.phasedStates[0].stateInfo, wire.getColor());
            FacadeInstance result = mergePhasedState(targetFacade, additionalState, wire.getColor());
            return result == null ? ItemStack.EMPTY : BCSiliconItems.PLUG_FACADE.createItemStack(result);
         } else {
            return ItemStack.EMPTY;
         }
      }

      @Nullable
      private static FacadeInstance mergePhasedState(FacadeInstance target, FacadePhasedState additionalState, DyeColor wireColour) {
         FacadeInstance withAdded = target.withState(additionalState);
         if (withAdded != null) {
            return withAdded;
         }

         FacadePhasedState[] states = target.phasedStates;

         for (int i = 0; i < states.length; i++) {
            if (states[i].activeColour == wireColour) {
               FacadePhasedState[] newStates = Arrays.copyOf(states, states.length);
               newStates[i] = additionalState;
               return new FacadeInstance(newStates, target.isHollow);
            }
         }

         return null;
      }

      @Override
      public ImmutableList<IngredientStack> getRequirements(ItemStack output) {
         return ImmutableList.of(
            new IngredientStack(Ingredient.of((ItemLike[])BCTransportItems.WIRE_ITEMS.values().toArray(Item[]::new))),
            new IngredientStack(Ingredient.of(new ItemLike[]{BCSiliconItems.PLUG_FACADE, BCTransportItems.PLUG_BLOCKER}))
         );
      }

      @Override
      public long getRequiredMicroJoules(ItemStack output) {
         return BCSiliconIntegrationRecipes.INTEGRATION_COST;
      }

      @Override
      public IngredientStack getCenterStack() {
         return CENTER;
      }
   }

   private static final class GateLogicToggleRecipe extends IntegrationRecipe {
      private static final IngredientStack CENTER = new IngredientStack(Ingredient.of(BCSiliconItems.PLUG_GATE));
      private static final ImmutableList<IngredientStack> REQUIREMENTS = ImmutableList.of(new IngredientStack(Ingredient.of(BCSiliconItems.CHIPSET_REDSTONE)));

      GateLogicToggleRecipe() {
         super(Identifier.fromNamespaceAndPath("buildcraftsilicon", "gate_logic_toggle"));
      }

      @Override
      public ItemStack getOutput(@Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate) {
         if (!(target.getItem() instanceof ItemPluggableGate)) {
            return ItemStack.EMPTY;
         }

         GateVariant variant = ItemPluggableGate.getVariant(target);
         if (!variant.material.canBeModified) {
            return ItemStack.EMPTY;
         }

         if (!matchesIngredients(toIntegrate)) {
            return ItemStack.EMPTY;
         }

         EnumGateLogic newLogic = variant.logic == EnumGateLogic.AND ? EnumGateLogic.OR : EnumGateLogic.AND;
         return BCSiliconItems.PLUG_GATE.getStack(new GateVariant(newLogic, variant.material, variant.modifier));
      }

      private static boolean matchesIngredients(NonNullList<ItemStack> toIntegrate) {
         boolean hasChipset = false;

         for (ItemStack stack : toIntegrate) {
            if (!stack.isEmpty()) {
               if (!stack.is(BCSiliconItems.CHIPSET_REDSTONE)) {
                  return false;
               }

               if (stack.getCount() < 1 || hasChipset) {
                  return false;
               }

               hasChipset = true;
            }
         }

         return hasChipset;
      }

      @Override
      public ImmutableList<IngredientStack> getRequirements(ItemStack output) {
         return REQUIREMENTS;
      }

      @Override
      public long getRequiredMicroJoules(ItemStack output) {
         return BCSiliconIntegrationRecipes.INTEGRATION_COST;
      }

      @Override
      public IngredientStack getCenterStack() {
         return CENTER;
      }
   }
}
