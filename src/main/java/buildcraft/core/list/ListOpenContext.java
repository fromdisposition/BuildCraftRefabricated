package buildcraft.core.list;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public final class ListOpenContext {
    private static final Map<UUID, InteractionHand> NEXT_HAND = new ConcurrentHashMap<>();

    private ListOpenContext() {}

    public static void remember(Player player, InteractionHand hand) {
        NEXT_HAND.put(player.getUUID(), hand);
    }

    public static InteractionHand consume(Player player) {
        return NEXT_HAND.remove(player.getUUID());
    }
}
