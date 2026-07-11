package buildcraft.fabric.integration.rei;

import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

/**
 * Shared display base: precomputed input/output ingredients plus optional per-recipe text lines the
 * category layout renders below the slots (MJ cost, burn time, ...). No serializer is registered —
 * displays are rebuilt client-side from the recipe registries, exactly like the JEI integration.
 */
class BcReiDisplay implements Display {
   private final CategoryIdentifier<? extends BcReiDisplay> category;
   private final List<EntryIngredient> inputs;
   private final List<EntryIngredient> outputs;
   final List<net.minecraft.network.chat.Component> lines;
   /** Source recipe object, kept for the transfer handlers (null for view-only displays). */
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
      this.lines = lines;
      this.recipe = recipe;
   }

   @Override
   public CategoryIdentifier<?> getCategoryIdentifier() {
      return this.category;
   }

   @Override
   public List<EntryIngredient> getInputEntries() {
      return this.inputs;
   }

   @Override
   public List<EntryIngredient> getOutputEntries() {
      return this.outputs;
   }

   // No @Override on getSerializer: REI 26.x made it abstract on Display, REI 16.x has no such method.
   // Null/empty = not synced or serialized; displays are rebuilt client-side, same as the JEI integration.
   public me.shedaniel.rei.api.common.display.DisplaySerializer<? extends Display> getSerializer() {
      return null;
   }

   @Override
   public Optional<net.minecraft.resources.Identifier> getDisplayLocation() {
      return Optional.empty();
   }
}
