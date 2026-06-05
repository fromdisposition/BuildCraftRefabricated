package buildcraft.fabric;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import buildcraft.api.core.BCLog;
import buildcraft.core.BCCoreConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.energy.generation.OilGenerator;
import buildcraft.lib.misc.AdvancementUtil;

public final class BCEnergyWorldGenFabric {
    private static final Identifier ADVANCEMENT_FINE_RICHES =
            Identifier.parse("buildcraftenergy:fine_riches");

    private static final int FINE_RICHES_TICK_STRIDE = 20;
    private static final int FINE_RICHES_SCAN_RADIUS = 3;
    private static final int OILGEN_CHUNKS_PER_TICK = 2;
    private static final Map<ServerLevel, Deque<ChunkPos>> PENDING_OILGEN = new WeakHashMap<>();
    private static final Map<ServerLevel, Set<Long>> PENDING_OILGEN_KEYS = new WeakHashMap<>();

    private BCEnergyWorldGenFabric() {}

    public static void init() {
        if (!BCCoreConfig.worldGen.get() || !BCEnergyConfig.enableOilGeneration.get()) {
            BCLog.logger.info("[energy.oilgen] Oil generation is disabled by config.");
            return;
        }

        ServerChunkEvents.CHUNK_LOAD.register((serverLevel, chunk, newChunk) -> {
            ChunkPos chunkPos = chunk.getPos();
            OilGenSavedData data = OilGenSavedData.getOrCreate(serverLevel);
            if (data.hasGenerated(chunkPos)) {
                return;
            }
            data.markGenerated(chunkPos);
            enqueueOilGeneration(serverLevel, chunkPos);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                processOilGenerationQueue(level);
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                onPlayerTick(player);
            }
        });

        BCLog.logger.info("[energy.oilgen] Registered oil world generation (Fabric).");
    }

    private static void enqueueOilGeneration(ServerLevel level, ChunkPos pos) {
        long key = buildcraft.lib.misc.PositionUtil.chunkPack(pos);
        Set<Long> keys = PENDING_OILGEN_KEYS.computeIfAbsent(level, ignored -> new HashSet<>());
        if (!keys.add(key)) {
            return;
        }
        PENDING_OILGEN.computeIfAbsent(level, ignored -> new ArrayDeque<>()).addLast(pos);
    }

    private static void processOilGenerationQueue(ServerLevel level) {
        Deque<ChunkPos> queue = PENDING_OILGEN.get(level);
        if (queue == null || queue.isEmpty()) {
            return;
        }
        Set<Long> keys = PENDING_OILGEN_KEYS.computeIfAbsent(level, ignored -> new HashSet<>());
        for (int i = 0; i < OILGEN_CHUNKS_PER_TICK && !queue.isEmpty(); i++) {
            ChunkPos pos = queue.pollFirst();
            if (pos == null) {
                continue;
            }
            keys.remove(buildcraft.lib.misc.PositionUtil.chunkPack(pos));
            OilGenerator.generateForChunk(
                    level,
                    buildcraft.lib.misc.PositionUtil.chunkX(pos),
                    buildcraft.lib.misc.PositionUtil.chunkZ(pos));
        }
    }

    private static void onPlayerTick(ServerPlayer player) {
        if (player.tickCount % FINE_RICHES_TICK_STRIDE != 0) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!OilGenerator.canGenerateOilIn(level)) {
            return;
        }

        ChunkPos current = player.chunkPosition();
        int cx = buildcraft.lib.misc.PositionUtil.chunkX(current);
        int cz = buildcraft.lib.misc.PositionUtil.chunkZ(current);

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
        private static final String DATA_NAME = "buildcraft_oil_gen";
        private final Set<Long> generatedChunks;

        public OilGenSavedData() {
            this.generatedChunks = new HashSet<>();
        }

        private OilGenSavedData(List<Long> chunks) {
            this.generatedChunks = new HashSet<>(chunks);
        }

        private static final Codec<OilGenSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.listOf().optionalFieldOf("chunks", List.of())
                        .forGetter(d -> new ArrayList<>(d.generatedChunks))
        ).apply(instance, OilGenSavedData::new));

        public static final SavedDataType<OilGenSavedData> TYPE = new SavedDataType<>(
                Identifier.withDefaultNamespace(DATA_NAME),
                OilGenSavedData::new,
                CODEC,
                net.minecraft.util.datafix.DataFixTypes.LEVEL
        );

        public boolean hasGenerated(ChunkPos pos) {
            return generatedChunks.contains(buildcraft.lib.misc.PositionUtil.chunkPack(pos));
        }

        public void markGenerated(ChunkPos pos) {
            generatedChunks.add(buildcraft.lib.misc.PositionUtil.chunkPack(pos));
            setDirty();
        }

        public static OilGenSavedData getOrCreate(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(TYPE);
        }
    }
}
