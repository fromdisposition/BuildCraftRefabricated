package buildcraft.lib.platform.client;

import buildcraft.lib.platform.mixin.client.GhostSlotsInvokerMixin;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public final class GhostSlotsAccess {
   private GhostSlotsAccess() {
   }

   public static void setInput(GhostSlots ghostSlots, Slot slot, ContextMap context, SlotDisplay display) {
      ((GhostSlotsInvokerMixin)ghostSlots).buildcraft$setInput(slot, context, display);
   }

   public static void setResult(GhostSlots ghostSlots, Slot slot, ContextMap context, SlotDisplay display) {
      ((GhostSlotsInvokerMixin)ghostSlots).buildcraft$setResult(slot, context, display);
   }
}
