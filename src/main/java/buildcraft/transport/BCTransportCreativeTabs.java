package buildcraft.transport;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;

import buildcraft.fabric.BCRegistries;
import net.minecraft.core.registries.Registries;

public final class BCTransportCreativeTabs {
    public static CreativeModeTab PIPES_TAB;
    public static CreativeModeTab PLUGS_TAB;

    public static final ResourceKey<CreativeModeTab> PIPES_TAB_KEY =
            ResourceKey.create(
                    net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,
                    BCRegistries.id(BCTransport.MODID, "pipes"));
    public static final ResourceKey<CreativeModeTab> PLUGS_TAB_KEY =
            ResourceKey.create(
                    net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,
                    BCRegistries.id(BCTransport.MODID, "plugs"));

    private BCTransportCreativeTabs() {}

    public static void register() {
        PIPES_TAB = net.minecraft.core.Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                BCRegistries.id(BCTransport.MODID, "pipes"),
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 2)
                        .title(Component.translatable("itemGroup.buildcraft.pipes"))
                        .icon(() -> new net.minecraft.world.item.ItemStack(BCTransportItems.PIPE_DIAMOND_ITEM.get()))
                        .displayItems((parameters, output) -> addPipeItems(output))
                        .build());
        PLUGS_TAB = net.minecraft.core.Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                BCRegistries.id(BCTransport.MODID, "plugs"),
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 3)
                        .title(Component.translatable("itemGroup.buildcraft.plugs"))
                        .icon(() -> new net.minecraft.world.item.ItemStack(BCTransportItems.PLUG_BLOCKER.get()))
                        .displayItems((parameters, output) -> addPlugItems(output))
                        .build());
    }

    private static void addPlugItems(CreativeModeTab.Output output) {
        output.accept(BCTransportItems.PLUG_BLOCKER.get());
        output.accept(BCTransportItems.PLUG_POWER_ADAPTOR.get());
        for (DyeColor color : DyeColor.values()) {
            output.accept(BCTransportItems.WIRE_ITEMS.get(color).get());
        }
        buildcraft.fabric.BCSiliconCreativeEntries.addSiliconPlugItems(output);
    }

    private static void addPipeItems(CreativeModeTab.Output output) {
        output.accept(BCTransportItems.FILTERED_BUFFER.get());
        output.accept(BCTransportItems.WATERPROOF.get());
        output.accept(BCTransportItems.PIPE_STRUCTURE.get());
        output.accept(BCTransportItems.PIPE_WOOD_ITEM.get());
        output.accept(BCTransportItems.PIPE_COBBLE_ITEM.get());
        output.accept(BCTransportItems.PIPE_STONE_ITEM.get());
        output.accept(BCTransportItems.PIPE_QUARTZ_ITEM.get());
        output.accept(BCTransportItems.PIPE_IRON_ITEM.get());
        output.accept(BCTransportItems.PIPE_GOLD_ITEM.get());
        output.accept(BCTransportItems.PIPE_CLAY_ITEM.get());
        output.accept(BCTransportItems.PIPE_SANDSTONE_ITEM.get());
        output.accept(BCTransportItems.PIPE_VOID_ITEM.get());
        output.accept(BCTransportItems.PIPE_OBSIDIAN_ITEM.get());
        output.accept(BCTransportItems.PIPE_DIAMOND_ITEM.get());
        output.accept(BCTransportItems.PIPE_DIAMOND_WOOD_ITEM.get());
        output.accept(BCTransportItems.PIPE_LAPIS_ITEM.get());
        output.accept(BCTransportItems.PIPE_DAIZULI_ITEM.get());
        output.accept(BCTransportItems.PIPE_EMZULI_ITEM.get());
        output.accept(BCTransportItems.PIPE_STRIPES_ITEM.get());
        output.accept(BCTransportItems.PIPE_WOOD_FLUID.get());
        output.accept(BCTransportItems.PIPE_COBBLE_FLUID.get());
        output.accept(BCTransportItems.PIPE_STONE_FLUID.get());
        output.accept(BCTransportItems.PIPE_QUARTZ_FLUID.get());
        output.accept(BCTransportItems.PIPE_GOLD_FLUID.get());
        output.accept(BCTransportItems.PIPE_IRON_FLUID.get());
        output.accept(BCTransportItems.PIPE_CLAY_FLUID.get());
        output.accept(BCTransportItems.PIPE_SANDSTONE_FLUID.get());
        output.accept(BCTransportItems.PIPE_VOID_FLUID.get());
        output.accept(BCTransportItems.PIPE_DIAMOND_FLUID.get());
        output.accept(BCTransportItems.PIPE_DIAMOND_WOOD_FLUID.get());
        output.accept(BCTransportItems.PIPE_WOOD_POWER.get());
        output.accept(BCTransportItems.PIPE_COBBLE_POWER.get());
        output.accept(BCTransportItems.PIPE_STONE_POWER.get());
        output.accept(BCTransportItems.PIPE_QUARTZ_POWER.get());
        output.accept(BCTransportItems.PIPE_IRON_POWER.get());
        output.accept(BCTransportItems.PIPE_GOLD_POWER.get());
        output.accept(BCTransportItems.PIPE_SANDSTONE_POWER.get());
        output.accept(BCTransportItems.PIPE_DIAMOND_POWER.get());
        output.accept(BCTransportItems.PIPE_DIAMOND_WOOD_POWER.get());
        output.accept(BCTransportItems.PIPE_WOOD_RF.get());
        output.accept(BCTransportItems.PIPE_COBBLE_RF.get());
        output.accept(BCTransportItems.PIPE_STONE_RF.get());
        output.accept(BCTransportItems.PIPE_QUARTZ_RF.get());
        output.accept(BCTransportItems.PIPE_IRON_RF.get());
        output.accept(BCTransportItems.PIPE_GOLD_RF.get());
        output.accept(BCTransportItems.PIPE_SANDSTONE_RF.get());
        output.accept(BCTransportItems.PIPE_DIAMOND_RF.get());
        output.accept(BCTransportItems.PIPE_DIAMOND_WOOD_RF.get());
    }
}
