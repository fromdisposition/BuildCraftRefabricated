package buildcraft.fabric;

import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.generation.OilGenerator;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.PositionUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class BCEnergyWorldGenFabric {
   private static final Identifier ADVANCEMENT_FINE_RICHES = Identifier.parse("buildcraftenergy:fine_riches");
   private static final int FINE_RICHES_SCAN_RADIUS = 3;
   private static final int FINE_RICHES_TICK_STRIDE = 20;

   private BCEnergyWorldGenFabric() {
   }

   public static void init() {
      if (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableOilGeneration.get()) {
         BCLog.logger.info("[energy.oilgen] Oil generation is disabled by config.");
         return;
      }

      ServerChunkEvents.FULL_CHUNK_STATUS_CHANGE.register((level, chunk, oldStatus, newStatus) -> {
         if (newStatus.isOrAfter(FullChunkStatus.FULL) && !oldStatus.isOrAfter(FullChunkStatus.FULL)) {
            populateOilIfNeeded(level, chunk);
         }
      });

      ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
         if (!generated) {
            populateOilIfNeeded(level, chunk);
         }
      });

      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            onPlayerTick(player);
         }
      });

      BCLog.logger.info("[energy.oilgen] Registered oil world generation (Fabric FULL_CHUNK_STATUS_CHANGE).");
   }

   private static void populateOilIfNeeded(ServerLevel level, LevelChunk chunk) {
      if (!OilGenerator.canGenerateOilIn(level)) {
         return;
      }

      ChunkPos chunkPos = chunk.getPos();
      OilGenSavedData data = OilGenSavedData.getOrCreate(level);
      if (!data.hasGenerated(chunkPos)) {
         OilGenerator.generateForChunk(level, PositionUtil.chunkX(chunkPos), PositionUtil.chunkZ(chunkPos));
         data.markGenerated(chunkPos);
      }
   }

   private static void onPlayerTick(ServerPlayer player) {
      if (player.tickCount % FINE_RICHES_TICK_STRIDE != 0) {
         return;
      }

      if (!(player.level() instanceof ServerLevel level) || !OilGenerator.canGenerateOilIn(level)) {
         return;
      }

      ChunkPos current = player.chunkPosition();
      int cx = PositionUtil.chunkX(current);
      int cz = PositionUtil.chunkZ(current);
      if (!OilGenerator.isOilDesignBiomeAt(level, cx, cz)) {
         return;
      }

      for (int dx = -FINE_RICHES_SCAN_RADIUS; dx <= FINE_RICHES_SCAN_RADIUS; dx++) {
         for (int dz = -FINE_RICHES_SCAN_RADIUS; dz <= FINE_RICHES_SCAN_RADIUS; dz++) {
            if (OilGenerator.wouldGenerateOilForOriginChunk(level, cx + dx, cz + dz)) {
               AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_FINE_RICHES);
               return;
            }
         }
      }
   }

   public static final class OilGenSavedData extends SavedData {
      private static final int CURRENT_VERSION = 3;
      private int version;
      private final Set<Long> generatedChunks;
      private static final Codec<OilGenSavedData> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               Codec.INT.optionalFieldOf("version", 0).forGetter(data -> data.version),
               Codec.LONG.listOf().optionalFieldOf("chunks", List.of()).forGetter(data -> new ArrayList<>(data.generatedChunks))
            )
            .apply(instance, OilGenSavedData::fromCodec)
      );
      public static final SavedDataType<OilGenSavedData> TYPE = new SavedDataType<>(
         Identifier.fromNamespaceAndPath("buildcraftenergy", "oil_gen"), OilGenSavedData::new, CODEC, DataFixTypes.LEVEL
      );

      public OilGenSavedData() {
         this(CURRENT_VERSION, List.of());
      }

      private static OilGenSavedData fromCodec(int version, List<Long> chunks) {
         return new OilGenSavedData(version, chunks);
      }

      private OilGenSavedData(int version, List<Long> chunks) {
         this.version = version;
         this.generatedChunks = new HashSet<>(chunks);
         if (this.version < CURRENT_VERSION) {
            this.generatedChunks.clear();
            this.version = CURRENT_VERSION;
            this.setDirty();
         }
      }

      public boolean hasGenerated(ChunkPos pos) {
         return this.generatedChunks.contains(PositionUtil.chunkPack(pos));
      }

      public void markGenerated(ChunkPos pos) {
         this.generatedChunks.add(PositionUtil.chunkPack(pos));
         this.setDirty();
      }

      public static OilGenSavedData getOrCreate(ServerLevel level) {
         return level.getDataStorage().computeIfAbsent(TYPE);
      }
   }
}
