package buildcraft.lib.fabric.transfer;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public final class BcTransfers {
   private static boolean initialized;
   // Per-position BlockApiCache pool. BlockApiCache skips the block-entity map lookup and the provider
   // resolution on repeat queries, and self-invalidates when the target block entity changes. Engines and
   // fluid/power pipes re-resolve their neighbours every tick, so routing these central helpers through the
   // pool removes that cost for every caller at once. Bounded per level; entries die with the level (weak
   // keys plus the explicit unload hook in BuildCraftFabricMod).
   private static final int MAX_CACHED_POSITIONS_PER_LEVEL = 8192;
   private static final Map<ServerLevel, Long2ObjectOpenHashMap<BlockApiCache<Storage<FluidVariant>, Direction>>> FLUID_CACHES = new WeakHashMap<>();
   private static final Map<ServerLevel, Long2ObjectOpenHashMap<BlockApiCache<Storage<ItemVariant>, Direction>>> ITEM_CACHES = new WeakHashMap<>();
   private static final Map<ServerLevel, Long2ObjectOpenHashMap<BlockApiCache<EnergyStorage, Direction>>> ENERGY_CACHES = new WeakHashMap<>();
   private static final Map<ServerLevel, Long2ObjectOpenHashMap<BlockApiCache<IMjReceiver, Direction>>> MJ_CACHES = new WeakHashMap<>();

   private BcTransfers() {
   }

   public static void onLevelUnload(ServerLevel level) {
      FLUID_CACHES.remove(level);
      ITEM_CACHES.remove(level);
      ENERGY_CACHES.remove(level);
      MJ_CACHES.remove(level);
   }

   private static <A> BlockApiCache<A, Direction> cacheFor(
      Map<ServerLevel, Long2ObjectOpenHashMap<BlockApiCache<A, Direction>>> pool,
      BlockApiLookup<A, Direction> lookup,
      ServerLevel level,
      BlockPos pos
   ) {
      Long2ObjectOpenHashMap<BlockApiCache<A, Direction>> perLevel = pool.computeIfAbsent(level, l -> new Long2ObjectOpenHashMap<>());
      if (perLevel.size() > MAX_CACHED_POSITIONS_PER_LEVEL) {
         perLevel.clear();
      }

      long key = pos.asLong();
      BlockApiCache<A, Direction> cache = perLevel.get(key);
      if (cache == null) {
         cache = BlockApiCache.create(lookup, level, pos.immutable());
         perLevel.put(key, cache);
      }

      return cache;
   }

   @Nullable
   private static <A> A findCached(
      Map<ServerLevel, Long2ObjectOpenHashMap<BlockApiCache<A, Direction>>> pool,
      BlockApiLookup<A, Direction> lookup,
      ServerLevel level,
      BlockPos pos,
      @Nullable BlockState state,
      @Nullable Direction side
   ) {
      BlockApiCache<A, Direction> cache = cacheFor(pool, lookup, level, pos);
      return state != null ? cache.find(state, side) : cache.find(side);
   }

   /** Block-entity read through the pool: cached alongside the capability, refreshed by Fabric on BE change. */
   @Nullable
   public static BlockEntity cachedBlockEntity(Level level, BlockPos pos) {
      return level instanceof ServerLevel sl ? cacheFor(MJ_CACHES, MjAPI.CAP_RECEIVER, sl, pos).getBlockEntity() : level.getBlockEntity(pos);
   }

   @Nullable
   public static IMjReceiver mjReceiver(Level level, BlockPos pos, @Nullable Direction side) {
      return level instanceof ServerLevel sl
         ? findCached(MJ_CACHES, MjAPI.CAP_RECEIVER, sl, pos, null, side)
         : MjAPI.CAP_RECEIVER.find(level, pos, null, null, side);
   }

   public static synchronized void init() {
      if (!initialized) {
         initialized = true;
         VanillaTransferFallbacks.register();
         ItemFluidNativeFallbacks.register();
      }
   }

   public static @Nullable Storage<FluidVariant> fluid(
      Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side
   ) {
      if (blockEntity == null && level instanceof ServerLevel sl) {
         return findCached(FLUID_CACHES, FluidStorage.SIDED, sl, pos, state, side);
      }

      return (Storage<FluidVariant>)FluidStorage.SIDED.find(level, pos, state, blockEntity, side);
   }

   public static @Nullable Storage<FluidVariant> fluid(Level level, BlockPos pos, @Nullable Direction side) {
      return fluid(level, pos, null, null, side);
   }

   public static @Nullable Storage<ItemVariant> item(
      Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side
   ) {
      if (blockEntity == null && level instanceof ServerLevel sl) {
         return findCached(ITEM_CACHES, ItemStorage.SIDED, sl, pos, state, side);
      }

      return (Storage<ItemVariant>)ItemStorage.SIDED.find(level, pos, state, blockEntity, side);
   }

   public static @Nullable Storage<ItemVariant> item(Level level, BlockPos pos, @Nullable Direction side) {
      return item(level, pos, null, null, side);
   }

   public static @Nullable EnergyStorage energy(
      Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction side
   ) {
      if (blockEntity == null && level instanceof ServerLevel sl) {
         return findCached(ENERGY_CACHES, EnergyStorage.SIDED, sl, pos, state, side);
      }

      return (EnergyStorage)EnergyStorage.SIDED.find(level, pos, state, blockEntity, side);
   }

   public static @Nullable EnergyStorage energy(Level level, BlockPos pos, @Nullable Direction side) {
      return energy(level, pos, null, null, side);
   }
}
