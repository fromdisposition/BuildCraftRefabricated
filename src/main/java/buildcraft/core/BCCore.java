package buildcraft.core;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.DyeColor;

import buildcraft.core.marker.PathCache;
import buildcraft.core.marker.VolumeCache;
import buildcraft.fabric.BCRegistries;
import buildcraft.lib.BCLib;
import buildcraft.lib.fluids.SimpleFluidContent;
import buildcraft.lib.marker.MarkerCache;

public final class BCCore {
    public static final String MODID = "buildcraftcore";
    public static final boolean DEV = Boolean.getBoolean("buildcraft.dev");

    public static DataComponentType<SimpleFluidContent> FLUID_CONTENT;
    public static DataComponentType<DyeColor> BRUSH_COLOR;
    public static DataComponentType<Integer> BRUSH_USES;

    private BCCore() {}

    public static void register() {
        registerDataComponents();
        BCCoreBlocks.register();
        BCCoreItems.register();
        BCCoreBlockEntities.register();
        BCCoreMenuTypes.register();
        BCCoreFeatures.register();
        BCCoreCreativeTabs.register();
        BCLib.init();
        preInit();
    }

    private static void registerDataComponents() {
        FLUID_CONTENT = BCRegistries.registerDataComponent(BCCore.MODID, "fluid_content",
                b -> b.persistent(SimpleFluidContent.CODEC).networkSynchronized(SimpleFluidContent.STREAM_CODEC));
        BRUSH_COLOR = BCRegistries.registerDataComponent(BCCore.MODID, "brush_color",
                b -> b.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC));
        BRUSH_USES = BCRegistries.registerDataComponent(BCCore.MODID, "brush_uses",
                b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT));
    }

    private static void preInit() {
        MarkerCache.registerCache(VolumeCache.INSTANCE);
        MarkerCache.registerCache(PathCache.INSTANCE);
        BCCoreStatements.preInit();
        buildcraft.lib.list.VanillaListHandlers.register();
        buildcraft.lib.block.VanillaPaintHandlers.init();
        buildcraft.lib.block.VanillaRotationHandlers.init();
    }
}
