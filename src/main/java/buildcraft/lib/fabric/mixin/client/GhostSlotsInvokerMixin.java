package buildcraft.lib.fabric.mixin.client;

import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GhostSlots.class)
public interface GhostSlotsInvokerMixin {
   @Invoker("setInput")
   void buildcraft$setInput(Slot var1, ContextMap var2, SlotDisplay var3);

   @Invoker("setResult")
   void buildcraft$setResult(Slot var1, ContextMap var2, SlotDisplay var3);
}
