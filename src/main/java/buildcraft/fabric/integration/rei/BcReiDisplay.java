/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.fabric.integration.rei;

import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

class BcReiDisplay implements Display {
   private final CategoryIdentifier<? extends BcReiDisplay> category;
   private final List<EntryIngredient> inputs;
   private final List<EntryIngredient> outputs;
   private final List<EntryIngredient> lookupInputs;
   private final List<EntryIngredient> lookupOutputs;
   final List<net.minecraft.network.chat.Component> lines;
   final Object recipe;

   BcReiDisplay(
      CategoryIdentifier<? extends BcReiDisplay> category,
      List<EntryIngredient> inputs,
      List<EntryIngredient> outputs,
      List<net.minecraft.network.chat.Component> lines
   ) {
      this(category, inputs, outputs, lines, null);
   }

   BcReiDisplay(
      CategoryIdentifier<? extends BcReiDisplay> category,
      List<EntryIngredient> inputs,
      List<EntryIngredient> outputs,
      List<net.minecraft.network.chat.Component> lines,
      Object recipe
   ) {
      this.category = category;
      this.inputs = inputs;
      this.outputs = outputs;
      this.lookupInputs = BcRei.withFluidAliases(inputs);
      this.lookupOutputs = BcRei.withFluidAliases(outputs);
      this.lines = lines;
      this.recipe = recipe;
   }

   @Override
   public CategoryIdentifier<?> getCategoryIdentifier() {
      return this.category;
   }

   List<EntryIngredient> inputs() {
      return this.inputs;
   }

   List<EntryIngredient> outputs() {
      return this.outputs;
   }

   @Override
   public List<EntryIngredient> getInputEntries() {
      return this.lookupInputs;
   }

   @Override
   public List<EntryIngredient> getOutputEntries() {
      return this.lookupOutputs;
   }

   public me.shedaniel.rei.api.common.display.DisplaySerializer<? extends Display> getSerializer() {
      return null;
   }

   @Override
   public Optional<net.minecraft.resources.Identifier> getDisplayLocation() {
      return Optional.empty();
   }
}
