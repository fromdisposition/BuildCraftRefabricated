package buildcraft.api.enums;

import net.minecraft.util.StringRepresentable;

import buildcraft.api.core.IEngineType;

public enum EnumEngineType implements StringRepresentable, IEngineType {
    WOOD("core", "wood"),
    STONE("energy", "stone"),
    IRON("energy", "iron"),
    CREATIVE("energy", "creative"),
    RF("energy", "rf");

    public final String unlocalizedTag;
    public final String resourceLocation;

    public static final EnumEngineType[] VALUES = values();

    EnumEngineType(String mod, String loc) {
        unlocalizedTag = loc;
        resourceLocation = "buildcraft" + mod + ":block/engine/inv/" + loc;
    }

    @Override
    public String getItemModelLocation() {
        return resourceLocation;
    }

    @Override
    public String getSerializedName() {
        return unlocalizedTag;
    }

    public static EnumEngineType fromMeta(int meta) {
        if (meta < 0 || meta >= VALUES.length) {
            meta = 0;
        }
        return VALUES[meta];
    }
}
