package buildcraft.transport;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

import buildcraft.fabric.BCRegistries;
import buildcraft.lib.fabric.menu.ExtendedMenuTypes;
import buildcraft.transport.container.ContainerDiamondPipe;
import buildcraft.transport.container.ContainerDiamondWoodPipe;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.container.ContainerFilteredBuffer_BC8;

public final class BCTransportMenuTypes {
    public static MenuType<ContainerFilteredBuffer_BC8> FILTERED_BUFFER;
    public static MenuType<ContainerDiamondPipe> DIAMOND_PIPE;
    public static MenuType<ContainerDiamondWoodPipe> DIAMOND_WOOD_PIPE;
    public static MenuType<ContainerEmzuliPipe_BC8> EMZULI_PIPE;

    private BCTransportMenuTypes() {}

    public static void register() {
        FILTERED_BUFFER = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCTransport.MODID, "filtered_buffer"),
                ExtendedMenuTypes.<ContainerFilteredBuffer_BC8>create(ContainerFilteredBuffer_BC8::new));
        DIAMOND_PIPE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCTransport.MODID, "diamond_pipe"),
                ExtendedMenuTypes.<ContainerDiamondPipe>create(ContainerDiamondPipe::new));
        DIAMOND_WOOD_PIPE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCTransport.MODID, "diamond_wood_pipe"),
                ExtendedMenuTypes.<ContainerDiamondWoodPipe>create(ContainerDiamondWoodPipe::new));
        EMZULI_PIPE = Registry.register(
                BuiltInRegistries.MENU,
                BCRegistries.id(BCTransport.MODID, "emzuli_pipe"),
                ExtendedMenuTypes.<ContainerEmzuliPipe_BC8>create(ContainerEmzuliPipe_BC8::new));
    }
}
