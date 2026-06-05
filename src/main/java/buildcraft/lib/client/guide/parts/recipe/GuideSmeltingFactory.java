package buildcraft.lib.client.guide.parts.recipe;

import java.util.Arrays;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartFactory;

public class GuideSmeltingFactory implements GuidePartFactory {
    @Nonnull
    private final ItemStack input, output;
    private final int hash;

    public GuideSmeltingFactory(ItemStack input, ItemStack output) {
        this.input = input.isEmpty() ? ItemStack.EMPTY : input;
        this.output = output.isEmpty() ? ItemStack.EMPTY : output;
        this.hash = Arrays.hashCode(new int[] {
            ItemStack.hashItemAndComponents(this.input),
            ItemStack.hashItemAndComponents(this.output)
        });
    }

    @Override
    public GuideSmelting createNew(GuiGuide gui) {
        return new GuideSmelting(gui, input, output);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        GuideSmeltingFactory other = (GuideSmeltingFactory) obj;
        if (hash != other.hash) return false;
        return ItemStack.isSameItemSameComponents(input, other.input)
            && ItemStack.isSameItemSameComponents(output, other.output);
    }
}
