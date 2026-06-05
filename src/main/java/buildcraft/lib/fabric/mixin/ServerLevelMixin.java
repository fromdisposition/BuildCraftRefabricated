package buildcraft.lib.fabric.mixin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import buildcraft.lib.attachments.IAttachmentInvalidationListener;
import buildcraft.lib.attachments.AttachmentQueries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements AttachmentQueries.CapabilityInvalidationLevel, AttachmentQueries.CapabilityListenerLevel {
    @Unique
    private final Map<BlockPos, List<WeakReference<IAttachmentInvalidationListener>>> buildcraft$capabilityListeners =
            new HashMap<>();

    @Override
    public void buildcraft$invalidateCapabilities(BlockPos pos) {
        List<WeakReference<IAttachmentInvalidationListener>> listeners = buildcraft$capabilityListeners.get(pos);
        if (listeners == null || listeners.isEmpty()) {
            return;
        }

        Iterator<WeakReference<IAttachmentInvalidationListener>> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            IAttachmentInvalidationListener listener = iterator.next().get();
            if (listener == null) {
                iterator.remove();
                continue;
            }
            boolean keep;
            try {
                keep = listener.onInvalidate();
            } catch (Throwable t) {

                keep = false;
            }
            if (!keep) {
                iterator.remove();
            }
        }

        if (listeners.isEmpty()) {
            buildcraft$capabilityListeners.remove(pos);
        }
    }

    @Override
    public void buildcraft$registerCapabilityListener(BlockPos pos, IAttachmentInvalidationListener invalidationListener) {
        BlockPos immutablePos = pos.immutable();
        List<WeakReference<IAttachmentInvalidationListener>> listeners =
                buildcraft$capabilityListeners.computeIfAbsent(immutablePos, ignored -> new ArrayList<>());
        listeners.add(new WeakReference<>(invalidationListener));
    }
}
