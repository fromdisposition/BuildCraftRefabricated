package buildcraft.core.item;

import buildcraft.api.items.IItemFluidShard;
import buildcraft.core.BCCore;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.fluids.SimpleFluidContent;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;

public class ItemFragileFluidContainer extends Item implements IItemFluidShard {
   public static final int MAX_FLUID_HELD = 500;

   public ItemFragileFluidContainer(Properties properties) {
      super(properties.stacksTo(1));
   }

   public Component getName(ItemStack stack) {
      FluidStack fluid = getFluid(stack);
      return fluid.isEmpty()
         ? Component.translatable(this.getDescriptionId() + ".name.empty")
         : Component.translatable(this.getDescriptionId() + ".name", new Object[]{fluid.getHoverName().getString()});
   }

   @Override
   @SuppressWarnings("deprecation")
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
      super.appendHoverText(stack, context, display, tooltip, flagIn);
      FluidStack fluid = getFluid(stack);
      if (!fluid.isEmpty() && fluid.getAmount() > 0) {
         tooltip.accept(Component.literal(fluid.getAmount() + " mB / 500 mB"));
      }
   }

   @Override
   public void addFluidDrops(NonNullList<ItemStack> toDrop, @Nullable FluidStack fluid) {
      if (fluid != null && !fluid.isEmpty()) {
         int amount = fluid.getAmount();
         if (amount >= 500) {
            FluidStack fluid2 = fluid.copy();
            fluid2.setAmount(500);

            while (amount >= 500) {
               ItemStack stack = new ItemStack(this);
               setFluid(stack, fluid2);
               amount -= 500;
               toDrop.add(stack);
            }
         }

         if (amount > 0) {
            ItemStack stack = new ItemStack(this);
            FluidStack fluid2 = fluid.copy();
            fluid2.setAmount(amount);
            setFluid(stack, fluid2);
            toDrop.add(stack);
         }
      }
   }

   public static void setFluid(ItemStack container, FluidStack fluid) {
      if (fluid.isEmpty()) {
         container.remove(BCCore.FLUID_CONTENT);
      } else {
         container.set(BCCore.FLUID_CONTENT, SimpleFluidContent.copyOf(fluid));
      }
   }

   public static FluidStack getFluid(ItemStack container) {
      if (container.isEmpty()) {
         return FluidStack.EMPTY;
      }

      SimpleFluidContent content = (SimpleFluidContent)container.getOrDefault(BCCore.FLUID_CONTENT, SimpleFluidContent.EMPTY);
      return content.copy();
   }
}
