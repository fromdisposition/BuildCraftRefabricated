package buildcraft.lib.fabric;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.Container;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.Level;

import buildcraft.lib.fluids.FluidStack;

import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;

public final class Mc26Compat {
    private Mc26Compat() {}

    public static boolean componentsPatchEmpty(ItemStack stack) {
        return stack.isEmpty() || stack.getComponentsPatch().isEmpty();
    }

    public static boolean componentsPatchEmpty(FluidStack stack) {
        return stack.isEmpty() || stack.getComponentsPatch().isEmpty();
    }

    public static boolean componentsPatchEmpty(DataComponentPatch patch) {
        return patch == null || patch.isEmpty();
    }

    public static void containerSetItem(Container container, int slot, ItemStack stack) {
        container.setItem(slot, stack);
    }

    public static Fluid bucketFluid(BucketItem bucket) {
        return bucket.getContent();
    }

    public static Item fluidBucketItem(Fluid fluid) {
        if (fluid.isSame(Fluids.EMPTY)) {
            return Items.BUCKET;
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof BucketItem bucket && bucket.getContent().isSame(fluid)) {
                return item;
            }
        }
        return Items.AIR;
    }

    public static float composterValue(ItemStack stack) {
        return ComposterBlock.COMPOSTABLES.getFloat(stack.getItem());
    }

    public static DynamicOps<Tag> registryAwareOps() {
        HolderLookup.Provider client = clientLevelRegistryAccess();
        if (client != null) {
            return RegistryOps.create(NbtOps.INSTANCE, client);
        }
        return NbtOps.INSTANCE;
    }

    public static DynamicOps<Tag> registryAwareOps(Level level) {
        return RegistryOps.create(NbtOps.INSTANCE, level.registryAccess());
    }

    private static @Nullable HolderLookup.Provider clientLevelRegistryAccess() {
        try {
            var level = net.minecraft.client.Minecraft.getInstance().level;
            return level == null ? null : level.registryAccess();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
