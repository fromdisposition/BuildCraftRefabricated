package buildcraft.lib.client.guide.parts.recipe;

import java.util.Arrays;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.recipe.ChangingItemStack;

public class GuideCraftingFactoryDirect implements GuidePartFactory {
    public final ChangingItemStack[][] input;
    public final ChangingItemStack output;

    private final int hash;

    public GuideCraftingFactoryDirect(ChangingItemStack[][] input, ChangingItemStack output) {
        this.input = input;
        this.output = output;
        hash = Arrays.deepHashCode(new Object[] { input, output });
    }

    @Override
    public GuidePart createNew(GuiGuide gui) {
        return new GuideCrafting(gui, input, output);
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
        GuideCraftingFactoryDirect other = (GuideCraftingFactoryDirect) obj;
        if (hash != other.hash) return false;
        return Arrays.deepEquals(input, other.input) && output.equals(other.output);
    }
}
