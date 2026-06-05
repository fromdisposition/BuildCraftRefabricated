package buildcraft.silicon;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

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

public final class BCSiliconIntegrationRecipes {
    private static final long INTEGRATION_COST = 25_000L * MjAPI.MJ;
    private static boolean initialized;

    private BCSiliconIntegrationRecipes() {}

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        IntegrationRecipeRegistry.INSTANCE.addRecipe(new GateLogicToggleRecipe());
        IntegrationRecipeRegistry.INSTANCE.addRecipe(new FacadePhasedRecipe());
    }

    private static final class GateLogicToggleRecipe extends IntegrationRecipe {
        private static final IngredientStack CENTER =
                new IngredientStack(Ingredient.of(BCSiliconItems.PLUG_GATE.get()));
        private static final ImmutableList<IngredientStack> REQUIREMENTS = ImmutableList.of(
                new IngredientStack(Ingredient.of(BCSiliconItems.CHIPSET_REDSTONE.get())));

        GateLogicToggleRecipe() {
            super(Identifier.fromNamespaceAndPath(BCSilicon.MODID, "gate_logic_toggle"));
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
            EnumGateLogic newLogic = variant.logic == EnumGateLogic.AND
                    ? EnumGateLogic.OR
                    : EnumGateLogic.AND;
            return BCSiliconItems.PLUG_GATE.get()
                    .getStack(new GateVariant(newLogic, variant.material, variant.modifier));
        }

        private static boolean matchesIngredients(NonNullList<ItemStack> toIntegrate) {
            boolean hasChipset = false;
            for (ItemStack stack : toIntegrate) {
                if (stack.isEmpty()) {
                    continue;
                }
                if (stack.is(BCSiliconItems.CHIPSET_REDSTONE.get())) {
                    if (stack.getCount() < 1 || hasChipset) {
                        return false;
                    }
                    hasChipset = true;
                } else {
                    return false;
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
            return INTEGRATION_COST;
        }

        @Override
        public IngredientStack getCenterStack() {
            return CENTER;
        }
    }

    private static final class FacadePhasedRecipe extends IntegrationRecipe {
        private static final IngredientStack CENTER =
                new IngredientStack(Ingredient.of(BCSiliconItems.PLUG_FACADE.get()));

        FacadePhasedRecipe() {
            super(Identifier.fromNamespaceAndPath(BCSilicon.MODID, "facade_phased"));
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
                if (stack.isEmpty()) {
                    continue;
                }
                if (stack.getItem() instanceof ItemWire itemWire) {
                    if (wire != null) {
                        return ItemStack.EMPTY;
                    }
                    wire = itemWire;
                } else if (stack.is(BCSiliconItems.PLUG_FACADE.get())) {
                    FacadeInstance facade = ItemPluggableFacade.getStates(stack);
                    if (facade.phasedStates.length != 1) {
                        return ItemStack.EMPTY;
                    }
                    if (addFacade != null) {
                        return ItemStack.EMPTY;
                    }
                    addFacade = facade;
                } else if (stack.is(BCTransportItems.PLUG_BLOCKER.get())) {
                    if (hasBlocker) {
                        return ItemStack.EMPTY;
                    }
                    hasBlocker = true;
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (wire == null || (addFacade == null && !hasBlocker)) {
                return ItemStack.EMPTY;
            }

            FacadePhasedState additionalState = hasBlocker
                    ? new FacadePhasedState(FacadeStateManager.defaultState, wire.getColor())
                    : new FacadePhasedState(addFacade.phasedStates[0].stateInfo, wire.getColor());

            FacadeInstance result = mergePhasedState(targetFacade, additionalState, wire.getColor());
            return result == null ? ItemStack.EMPTY : BCSiliconItems.PLUG_FACADE.get().createItemStack(result);
        }

        @Nullable
        private static FacadeInstance mergePhasedState(FacadeInstance target, FacadePhasedState additionalState,
                net.minecraft.world.item.DyeColor wireColour) {
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
                    new IngredientStack(Ingredient.of(BCTransportItems.WIRE_ITEMS.values().stream()
                            .map(holder -> holder.get())
                            .toArray(net.minecraft.world.item.Item[]::new))),
                    new IngredientStack(Ingredient.of(
                            BCSiliconItems.PLUG_FACADE.get(),
                            BCTransportItems.PLUG_BLOCKER.get())));
        }

        @Override
        public long getRequiredMicroJoules(ItemStack output) {
            return INTEGRATION_COST;
        }

        @Override
        public IngredientStack getCenterStack() {
            return CENTER;
        }
    }
}
