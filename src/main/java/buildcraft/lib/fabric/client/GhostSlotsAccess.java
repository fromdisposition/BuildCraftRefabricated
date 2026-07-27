package buildcraft.lib.fabric.client;

//? if >= 1.21.10 {
import buildcraft.lib.fabric.mixin.client.GhostSlotsInvokerMixin;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.SlotDisplay;
//?}

/**
 * Bridges to the 1.21.5 recipe-book ghost-slot invoker. GhostSlots/SlotDisplay/ContextMap do not exist on
 * 1.21.1 (which uses GhostRecipe), and BC's recipe-book components are versions/1.21.1 stubs there, so this
 * helper degrades to an empty class on 1.21.1.
 */
public final class GhostSlotsAccess {
   private GhostSlotsAccess() {
   }

   //? if >= 1.21.10 {
   public static void setInput(GhostSlots ghostSlots, Slot slot, ContextMap context, SlotDisplay display) {
      ((GhostSlotsInvokerMixin)ghostSlots).buildcraft$setInput(slot, context, display);
   }

   public static void setResult(GhostSlots ghostSlots, Slot slot, ContextMap context, SlotDisplay display) {
      ((GhostSlotsInvokerMixin)ghostSlots).buildcraft$setResult(slot, context, display);
   }
   //?}
}
