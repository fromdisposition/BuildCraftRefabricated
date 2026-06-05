package buildcraft.core.item;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.fluids.SimpleFluidContent;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

import buildcraft.api.items.IItemFluidShard;
import buildcraft.core.BCCore;

@SuppressWarnings("deprecation")
public class ItemFragileFluidContainer extends Item implements IItemFluidShard {

    public static final int MAX_FLUID_HELD = 500;

    public ItemFragileFluidContainer(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        FluidStack fluid = getFluid(stack);
        if (fluid.isEmpty()) {

            return Component.translatable(getDescriptionId() + ".name.empty");
        } else {
            return Component.translatable(getDescriptionId() + ".name", fluid.getHoverName().getString());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, display, tooltip, flagIn);
        FluidStack fluid = getFluid(stack);
        if (!fluid.isEmpty() && fluid.getAmount() > 0) {
            tooltip.accept(Component.literal(fluid.getAmount() + " mB / " + MAX_FLUID_HELD + " mB"));
        }
    }

    @Override
    public void addFluidDrops(NonNullList<ItemStack> toDrop, @Nullable FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return;
        }
        int amount = fluid.getAmount();
        if (amount >= MAX_FLUID_HELD) {
            FluidStack fluid2 = fluid.copy();
            fluid2.setAmount(MAX_FLUID_HELD);
            while (amount >= MAX_FLUID_HELD) {
                ItemStack stack = new ItemStack(this);
                setFluid(stack, fluid2);
                amount -= MAX_FLUID_HELD;
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

    public static void setFluid(ItemStack container, FluidStack fluid) {
        if (fluid.isEmpty()) {
            container.remove(BCCore.FLUID_CONTENT);
            return;
        }
        container.set(BCCore.FLUID_CONTENT, SimpleFluidContent.copyOf(fluid));
    }

    public static FluidStack getFluid(ItemStack container) {
        if (container.isEmpty()) {
            return FluidStack.EMPTY;
        }
        SimpleFluidContent content = container.getOrDefault(BCCore.FLUID_CONTENT, SimpleFluidContent.EMPTY);
        return content.copy();
    }
}
