package buildcraft.lib.net;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerBatchQueue<T> {
   private final Map<ServerPlayer, T> byPlayer = new WeakHashMap<>();
   private final Function<ServerPlayer, T> factory;

   public PlayerBatchQueue(Function<ServerPlayer, T> factory) {
      this.factory = factory;
   }

   public T getOrCreate(ServerPlayer player) {
      return this.byPlayer.computeIfAbsent(player, this.factory);
   }

   public void forTracking(ServerLevel level, BlockPos pos, BiConsumer<ServerPlayer, T> action) {
      for (ServerPlayer player : PlayerLookup.tracking(level, pos)) {
         action.accept(player, this.getOrCreate(player));
      }
   }

   public void flushEach(Consumer<Entry<ServerPlayer, T>> flusher) {
      Iterator<Entry<ServerPlayer, T>> it = this.byPlayer.entrySet().iterator();

      while (it.hasNext()) {
         Entry<ServerPlayer, T> entry = it.next();
         flusher.accept(entry);
         it.remove();
      }
   }

   public Iterable<Entry<ServerPlayer, T>> entries() {
      return this.byPlayer.entrySet();
   }

   public void remove(ServerPlayer player) {
      this.byPlayer.remove(player);
   }

   public boolean isEmpty() {
      return this.byPlayer.isEmpty();
   }
}
