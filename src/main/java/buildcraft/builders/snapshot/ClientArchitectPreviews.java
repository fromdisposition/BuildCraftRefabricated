package buildcraft.builders.snapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;

import buildcraft.fabric.client.ClientPacketDistributor;

public enum ClientArchitectPreviews {
    INSTANCE;

    private final Map<BlockPos, Blueprint> previews = new HashMap<>();
    private final Set<BlockPos> pending = new HashSet<>();

    @Nullable
    public Blueprint get(BlockPos pos) {
        Blueprint cached = previews.get(pos);
        if (cached == null && !pending.contains(pos)) {
            pending.add(pos);
            ClientPacketDistributor.sendToServer(new ArchitectPreviewRequestPayload(pos.immutable()));
        }
        return cached;
    }

    public void requestRefresh(BlockPos pos) {
        BlockPos key = pos.immutable();
        if (!pending.contains(key)) {
            pending.add(key);
            ClientPacketDistributor.sendToServer(new ArchitectPreviewRequestPayload(key));
        }
    }

    public void onReceived(BlockPos pos, @Nullable Blueprint blueprint) {
        BlockPos key = pos.immutable();
        pending.remove(key);
        if (blueprint == null) {
            previews.remove(key);
            return;
        }
        Blueprint existing = previews.get(key);
        if (existing != null && sameContent(existing, blueprint)) {

            return;
        }
        previews.put(key, blueprint);
    }

    public void invalidate(BlockPos pos) {
        BlockPos key = pos.immutable();
        previews.remove(key);
        pending.remove(key);
    }

    private static boolean sameContent(Blueprint a, Blueprint b) {
        if (a.key == null || b.key == null) return false;
        byte[] ah = a.key.hash;
        byte[] bh = b.key.hash;
        if (ah == null || bh == null || ah.length == 0 || bh.length == 0) return false;
        return java.util.Arrays.equals(ah, bh);
    }
}
