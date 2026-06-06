package buildcraft.fabric;

import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.generation.OilGenerator;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.PositionUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents.Load;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class BCEnergyWorldGenFabric {
   private static final Identifier ADVANCEMENT_FINE_RICHES = Identifier.parse("buildcraftenergy:fine_riches");
   private static final int FINE_RICHES_TICK_STRIDE = 20;
   private static final int FINE_RICHES_SCAN_RADIUS = 3;
   private static final int OILGEN_CHUNKS_PER_TICK = 2;
   private static final Map<ServerLevel, Deque<ChunkPos>> PENDING_OILGEN = new WeakHashMap<>();
   private static final Map<ServerLevel, Set<Long>> PENDING_OILGEN_KEYS = new WeakHashMap<>();

   private BCEnergyWorldGenFabric() {
   }

   public static void init() {
      if (BCCoreConfig.worldGen.get() && BCEnergyConfig.enableOilGeneration.get()) {
         ServerChunkEvents.CHUNK_LOAD.register((Load)(serverLevel, chunk, newChunk) -> {
            ChunkPos chunkPos = chunk.getPos();
            BCEnergyWorldGenFabric.OilGenSavedData data = BCEnergyWorldGenFabric.OilGenSavedData.getOrCreate(serverLevel);
            if (!data.hasGenerated(chunkPos)) {
               data.markGenerated(chunkPos);
               enqueueOilGeneration(serverLevel, chunkPos);
            }
         });
         ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
            for (ServerLevel level : server.getAllLevels()) {
               processOilGenerationQueue(level);
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
               onPlayerTick(player);
            }
         });
         BCLog.logger.info("[energy.oilgen] Registered oil world generation (Fabric).");
      } else {
         BCLog.logger.info("[energy.oilgen] Oil generation is disabled by config.");
      }
   }

   private static void enqueueOilGeneration(ServerLevel level, ChunkPos pos) {
      long key = PositionUtil.chunkPack(pos);
      Set<Long> keys = PENDING_OILGEN_KEYS.computeIfAbsent(level, ignored -> new HashSet<>());
      if (keys.add(key)) {
         PENDING_OILGEN.computeIfAbsent(level, ignored -> new ArrayDeque<>()).addLast(pos);
      }
   }

   private static void processOilGenerationQueue(ServerLevel level) {
      Deque<ChunkPos> queue = PENDING_OILGEN.get(level);
      if (queue != null && !queue.isEmpty()) {
         Set<Long> keys = PENDING_OILGEN_KEYS.computeIfAbsent(level, ignored -> new HashSet<>());

         for (int i = 0; i < 2 && !queue.isEmpty(); i++) {
            ChunkPos pos = queue.pollFirst();
            if (pos != null) {
               keys.remove(PositionUtil.chunkPack(pos));
               OilGenerator.generateForChunk(level, PositionUtil.chunkX(pos), PositionUtil.chunkZ(pos));
            }
         }
      }
   }

   private static void onPlayerTick(ServerPlayer player) {
      if (player.tickCount % 20 == 0) {
         if (player.level() instanceof ServerLevel level) {
            if (OilGenerator.canGenerateOilIn(level)) {
               ChunkPos current = player.chunkPosition();
               int cx = PositionUtil.chunkX(current);
               int cz = PositionUtil.chunkZ(current);
               if (OilGenerator.isOilDesignBiomeAt(level, cx, cz)) {
                  for (int dx = -3; dx <= 3; dx++) {
                     for (int dz = -3; dz <= 3; dz++) {
                        if (OilGenerator.wouldGenerateOilForOriginChunk(level, cx + dx, cz + dz)) {
                           AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_FINE_RICHES);
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static final class OilGenSavedData extends SavedData {
      private static final String DATA_NAME = "buildcraft_oil_gen";
      private final Set<Long> generatedChunks;
      private static final Codec<BCEnergyWorldGenFabric.OilGenSavedData> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(Codec.LONG.listOf().optionalFieldOf("chunks", List.of()).forGetter(d -> new ArrayList<>(d.generatedChunks)))
            .apply(instance, BCEnergyWorldGenFabric.OilGenSavedData::new)
      );
      public static final SavedDataType<BCEnergyWorldGenFabric.OilGenSavedData> TYPE = new SavedDataType<>(
         Identifier.withDefaultNamespace("buildcraft_oil_gen"), BCEnergyWorldGenFabric.OilGenSavedData::new, CODEC, DataFixTypes.LEVEL
      );

      public OilGenSavedData() {
         this.generatedChunks = new HashSet<>();
      }

      private OilGenSavedData(List<Long> chunks) {
         this.generatedChunks = new HashSet<>(chunks);
      }

      public boolean hasGenerated(ChunkPos pos) {
         return this.generatedChunks.contains(PositionUtil.chunkPack(pos));
      }

      public void markGenerated(ChunkPos pos) {
         this.generatedChunks.add(PositionUtil.chunkPack(pos));
         this.setDirty();
      }

      public static BCEnergyWorldGenFabric.OilGenSavedData getOrCreate(ServerLevel level) {
         return (BCEnergyWorldGenFabric.OilGenSavedData)level.getDataStorage().computeIfAbsent(TYPE);
      }
   }
}
