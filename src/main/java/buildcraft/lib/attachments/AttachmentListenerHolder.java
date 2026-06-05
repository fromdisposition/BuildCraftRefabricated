/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class AttachmentListenerHolder {

    private final Long2ReferenceMap<Long2ReferenceMap<Set<ListenerReference>>> byChunkThenBlock = new Long2ReferenceOpenHashMap<>();

    private final ReferenceQueue<IAttachmentInvalidationListener> queue = new ReferenceQueue<>();

    public void addListener(BlockPos pos, IAttachmentInvalidationListener listener) {
        pos = pos.immutable();
        var chunkHolder = byChunkThenBlock.computeIfAbsent(ChunkPos.pack(pos), l -> new Long2ReferenceOpenHashMap<>());
        var listenersSet = chunkHolder.computeIfAbsent(pos.asLong(), l -> new ObjectOpenHashSet<>());

        var reference = new ListenerReference(queue, pos, listener);
        if (!listenersSet.add(reference)) {

            reference.clear();
        }
    }

    public void invalidatePos(BlockPos pos) {
        var chunkHolder = byChunkThenBlock.get(ChunkPos.pack(pos));
        if (chunkHolder != null) {
            var caches = chunkHolder.get(pos.asLong());
            if (caches != null)
                invalidateList(caches);
        }
    }

    public void invalidateChunk(ChunkPos chunkPos) {
        var chunkHolder = byChunkThenBlock.get(chunkPos.pack());
        if (chunkHolder != null) {
            for (var caches : chunkHolder.values())
                invalidateList(caches);
        }
    }

    private void invalidateList(Set<ListenerReference> caches) {
        caches.removeIf(ref -> {
            var listener = ref.get();
            return listener == null || !listener.onInvalidate();
        });
    }

    public void clean() {
        while (true) {
            ListenerReference ref = (ListenerReference) queue.poll();
            if (ref == null)
                return;

            var chunkHolder = byChunkThenBlock.get(ChunkPos.pack(ref.pos));
            if (chunkHolder == null)
                continue;

            var set = chunkHolder.get(ref.pos.asLong());
            if (set == null)
                continue;

            boolean removed = set.remove(ref);

            if (removed && set.isEmpty()) {
                chunkHolder.remove(ref.pos.asLong());
                if (chunkHolder.isEmpty()) {
                    byChunkThenBlock.remove(ChunkPos.pack(ref.pos));
                }
            }
        }
    }

    private static class ListenerReference extends WeakReference<IAttachmentInvalidationListener> {
        private final BlockPos pos;
        private final int listenerHashCode;

        private ListenerReference(ReferenceQueue<IAttachmentInvalidationListener> queue, BlockPos pos, IAttachmentInvalidationListener listener) {
            super(listener, queue);
            this.pos = pos;
            this.listenerHashCode = System.identityHashCode(listener);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ListenerReference otherRef) {

                return otherRef.listenerHashCode == listenerHashCode && otherRef.get() == get();
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return listenerHashCode;
        }
    }
}
