package buildcraft.api.tiles;

import java.util.List;

import net.minecraft.core.Direction;

public interface IDebuggable {

    void getDebugInfo(List<String> left, List<String> right, Direction side);

    default void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
    }
}
