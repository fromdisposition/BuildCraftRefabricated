package buildcraft.api.transport;

import net.minecraft.world.item.DyeColor;

import buildcraft.api.transport.pipe.IPipeHolder;

public interface IWireManager {

    IPipeHolder getHolder();

    void updateBetweens(boolean recursive);

    DyeColor getColorOfPart(EnumWirePart part);

    DyeColor removePart(EnumWirePart part);

    boolean addPart(EnumWirePart part, DyeColor colour);

    boolean hasPartOfColor(DyeColor color);

    boolean isPowered(EnumWirePart part);

    boolean isAnyPowered(DyeColor color);
}
