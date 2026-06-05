package buildcraft.api.lists;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

public abstract class ListMatchHandler {
    public enum Type {
        TYPE,
        MATERIAL,
        CLASS
    }

    public abstract boolean matches(Type type, @Nonnull ItemStack stack, @Nonnull ItemStack target, boolean precise);

    public abstract boolean isValidSource(Type type, @Nonnull ItemStack stack);

    @Nullable
    public NonNullList<ItemStack> getClientExamples(Type type, @Nonnull ItemStack stack) {
        return null;
    }

    @Nonnull
    public List<String> describeMatch(Type type, @Nonnull ItemStack stack) {
        return List.of();
    }
}
