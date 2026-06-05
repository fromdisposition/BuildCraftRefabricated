package buildcraft.core;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

import buildcraft.core.list.ContainerList;
import buildcraft.core.list.ListOpenContext;
import buildcraft.fabric.BCRegistries;

public final class BCCoreMenuTypes {
    public static MenuType<ContainerList> LIST;

    private BCCoreMenuTypes() {}

    public static void register() {
        LIST = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCCore.MODID, "list"),
                new MenuType<>((syncId, inv) -> {
                    net.minecraft.world.InteractionHand hand = ListOpenContext.consume(inv.player);
                    if (hand == null) {
                        hand = net.minecraft.world.InteractionHand.MAIN_HAND;
                    }
                    return new ContainerList(syncId, inv, hand);
                }, net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
    }
}
