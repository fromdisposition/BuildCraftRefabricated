package buildcraft.lib.client.guide.parts.recipe;

import java.util.Arrays;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.ArrayUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;

public class GuideAssemblyFactory implements GuidePartFactory {
    private final ChangingItemStack[] input;
    private final ChangingItemStack output;
    private final ChangingObject<Long> mjCost;
    private final int hash;

    public GuideAssemblyFactory(ChangingItemStack[] input, ChangingItemStack output, ChangingObject<Long> mjCost) {
        this.input = input;
        this.output = output;
        this.mjCost = mjCost;
        this.hash = computeHash();
    }

    public GuideAssemblyFactory(ItemStack[] input, ItemStack output, long mjCost) {
        this.input = ArrayUtil.map(input, ChangingItemStack::new, ChangingItemStack[]::new);
        this.output = new ChangingItemStack(output);
        this.mjCost = new ChangingObject<>(new Long[] { mjCost });
        this.hash = computeHash();
    }

    private int computeHash() {
        return Arrays.deepHashCode(new Object[] { input, output, mjCost });
    }

    @Override
    public GuideAssembly createNew(GuiGuide gui) {
        return new GuideAssembly(gui, input, output, mjCost);
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
        GuideAssemblyFactory other = (GuideAssemblyFactory) obj;
        if (hash != other.hash) return false;
        if (input.length != other.input.length) return false;
        return Arrays.equals(input, other.input) && output.equals(other.output);
    }
}
