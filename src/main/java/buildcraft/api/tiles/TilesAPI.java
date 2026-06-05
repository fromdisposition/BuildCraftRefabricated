package buildcraft.api.tiles;

import javax.annotation.Nonnull;

import buildcraft.api.core.CapabilitiesHelper;

public class TilesAPI {
    @Nonnull
    public static final Object CAP_CONTROLLABLE;

    @Nonnull
    public static final Object CAP_HAS_WORK;

    @Nonnull
    public static final Object CAP_HEATABLE;

    @Nonnull
    public static final Object CAP_TILE_AREA_PROVIDER;

    static {
        CAP_CONTROLLABLE = CapabilitiesHelper.registerCapability(IControllable.class);
        CAP_HAS_WORK = CapabilitiesHelper.registerCapability(IHasWork.class);
        CAP_HEATABLE = CapabilitiesHelper.registerCapability(IHeatable.class);
        CAP_TILE_AREA_PROVIDER = CapabilitiesHelper.registerCapability(ITileAreaProvider.class);
    }
}
