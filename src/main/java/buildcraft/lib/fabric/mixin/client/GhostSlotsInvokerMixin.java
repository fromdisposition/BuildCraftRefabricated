package buildcraft.lib.fabric.mixin.client;

//? if >= 1.21.10 {
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.spongepowered.asm.mixin.gen.Invoker;
//?}
import org.spongepowered.asm.mixin.Mixin;

// 1.21.5 recipe-book ghost slots (GhostSlots + SlotDisplay/ContextMap) do not exist on 1.21.1 (which uses
// GhostRecipe); BC's recipe-book components are versions/1.21.1 stubs there, so this accessor is unused and
// degrades to an empty no-op mixin.
//? if >= 1.21.10 {
@Mixin(GhostSlots.class)
public interface GhostSlotsInvokerMixin {
   @Invoker("setInput")
   void buildcraft$setInput(Slot var1, ContextMap var2, SlotDisplay var3);

   @Invoker("setResult")
   void buildcraft$setResult(Slot var1, ContextMap var2, SlotDisplay var3);
}
//?} else {
/*@Mixin(net.minecraft.client.Minecraft.class)
public interface GhostSlotsInvokerMixin {
}
*///?}
