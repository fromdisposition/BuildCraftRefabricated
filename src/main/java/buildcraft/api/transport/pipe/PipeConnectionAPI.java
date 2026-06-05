package buildcraft.api.transport.pipe;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.world.level.block.Block;

public final class PipeConnectionAPI {
    private static final Map<Block, ICustomPipeConnection> connections = Maps.newHashMap();
    private static final ICustomPipeConnection NOTHING = (world, pos, face, state) -> 0;

    public static void registerConnection(Block block, ICustomPipeConnection connection) {
        connections.put(block, connection);
    }

    public static void registerConnectionAsNothing(Block block) {
        connections.put(block, NOTHING);
    }

    public static ICustomPipeConnection getCustomConnection(Block block) {
        if (block instanceof ICustomPipeConnection) {
            return (ICustomPipeConnection) block;
        }
        ICustomPipeConnection connection = connections.get(block);
        if (connection != null) {
            return connection;
        }
        return null;
    }
}
