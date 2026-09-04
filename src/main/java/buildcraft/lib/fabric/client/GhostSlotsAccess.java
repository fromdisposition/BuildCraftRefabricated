package buildcraft.lib.fabric.client;

//? if >= 1.21.10 {
import buildcraft.lib.fabric.mixin.client.GhostSlotsInvokerMixin;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.SlotDisplay;
//?}

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
