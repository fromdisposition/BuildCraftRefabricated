package buildcraft.core.integration.jei;

import buildcraft.core.BCCore;
import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemFragileFluidContainer;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.gui.BcScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class BCCoreJeiPlugin implements IModPlugin {
   private static final Identifier UID = Identifier.parse("buildcraftrefabricated:core_jei_plugin");

   public Identifier getPluginUid() {
      return UID;
   }

   public void registerItemSubtypes(ISubtypeRegistration registration) {
      registration.registerFromDataComponentTypes(BCCoreItems.PAINTBRUSH, new DataComponentType[]{BCCore.BRUSH_COLOR});
      registration.registerSubtypeInterpreter(BCCoreItems.FRAGILE_FLUID_CONTAINER, (stack, context) -> {
         FluidStack fluid = ItemFragileFluidContainer.getFluid(stack);
         return fluid.isEmpty() ? null : BuiltInRegistries.FLUID.getKey(fluid.getFluid());
      });
   }

   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addGenericGuiContainerHandler(BcScreen.class, new BCGuiContainerHandler());
   }
}
