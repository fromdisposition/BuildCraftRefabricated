package buildcraft.fabric.config;

import buildcraft.builders.BCBuildersConfig;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.BCUnifiedClientConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.fabric.BCBuildersFabric;
import buildcraft.fabric.BCEnergyFabric;
import buildcraft.fabric.BCFactoryFabric;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.transport.BCTransportConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

public final class BCFabricConfig {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final String FILE_NAME = "buildcraftrefabricated-common.json";

   private BCFabricConfig() {
   }

   public static void load() {
      Path path = FabricLoader.getInstance().getConfigDir().resolve("buildcraftrefabricated-common.json");
      JsonObject root;
      if (Files.exists(path)) {
         try (Reader reader = Files.newBufferedReader(path)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
         } catch (Exception e) {
            LOGGER.error("Failed to read {}, using defaults", path, e);
            root = defaults();
            write(path, root);
         }
      } else {
         root = defaults();
         write(path, root);
         LOGGER.info("Created default config at {}", path.toAbsolutePath());
      }

      apply(root);
   }

   public static void reload() {
      load();
      BCFactoryFabric.onConfigReloaded();
      BCEnergyFabric.onConfigReloaded();
      BCBuildersFabric.onConfigReloaded();
   }

   private static void apply(JsonObject root) {
      BCTransportConfig.ensureLoaded();
      applyCore(root.getAsJsonObject("core"));
      applyLib(root.getAsJsonObject("lib"));
      applyEnergy(root.getAsJsonObject("energy"));
      applyFactory(root.getAsJsonObject("factory"));
      applyBuilders(root.getAsJsonObject("builders"));
      applyTransport(root.getAsJsonObject("transport"));
   }

   private static void applyBuilders(JsonObject builders) {
      if (builders != null) {
         BCBuildersConfig.applyQuarry(builders.getAsJsonObject("quarry"));
      }
   }

   private static void applyCore(JsonObject core) {
      if (core != null) {
         BCCoreConfig.worldGen.set(bool(core, "worldGen", BCCoreConfig.worldGen.get()));
         BCCoreConfig.minePlayerProtected.set(bool(core, "minePlayerProtected", BCCoreConfig.minePlayerProtected.get()));
         BCCoreConfig.pumpsConsumeWater.set(bool(core, "pumpsConsumeWater", BCCoreConfig.pumpsConsumeWater.get()));
         BCCoreConfig.markerMaxDistance.set(intVal(core, "markerMaxDistance", BCCoreConfig.markerMaxDistance.get()));
         BCCoreConfig.pumpMaxDistance.set(intVal(core, "pumpMaxDistance", BCCoreConfig.pumpMaxDistance.get()));
         BCCoreConfig.networkUpdateRate.set(intVal(core, "networkUpdateRate", BCCoreConfig.networkUpdateRate.get()));
         BCCoreConfig.miningMultiplier.set(doubleVal(core, "miningMultiplier", BCCoreConfig.miningMultiplier.get()));
         BCCoreConfig.miningMaxDepth.set(intVal(core, "miningMaxDepth", BCCoreConfig.miningMaxDepth.get()));
         BlockUtil.miningMultiplier = BCCoreConfig.miningMultiplier.get();
      }
   }

   private static void applyLib(JsonObject lib) {
      if (lib != null) {
         String mode = string(lib, "powerMode", BCLibConfig.powerMode.get().name());

         try {
            BCLibConfig.powerMode.set(BCLibConfig.PowerMode.valueOf(mode));
         } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown powerMode '{}', keeping {}", mode, BCLibConfig.powerMode.get());
         }

         BCLibConfig.mjRfConversionAmount.set(doubleVal(lib, "mjRfConversion", BCLibConfig.mjRfConversionAmount.get()));
         BCLibConfig.canEnginesExplode.set(bool(lib, "canEnginesExplode", BCLibConfig.canEnginesExplode.get()));
         BCLibConfig.ColorBlindMode previousColorBlind = BCLibConfig.colorBlindMode.get();
         String colorBlind = string(lib, "colorBlindMode", previousColorBlind.name());

         try {
            BCLibConfig.colorBlindMode.set(BCLibConfig.ColorBlindMode.valueOf(colorBlind));
         } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown colorBlindMode '{}', keeping {}", colorBlind, previousColorBlind);
         }

         if (previousColorBlind != BCLibConfig.colorBlindMode.get()) {
            notifyClientDisplayConfigReloaded();
         }
      }
   }

   private static void notifyClientDisplayConfigReloaded() {
      if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
         BCUnifiedClientConfig.onDisplayConfigReloaded();
      }
   }

   private static void applyFactory(JsonObject factory) {
      if (factory != null) {
         if (factory.has("pumpsConsumeWater")) {
            BCCoreConfig.pumpsConsumeWater.set(bool(factory, "pumpsConsumeWater", BCCoreConfig.pumpsConsumeWater.get()));
         }

         if (factory.has("pumpMaxDistance")) {
            BCCoreConfig.pumpMaxDistance.set(intVal(factory, "pumpMaxDistance", BCCoreConfig.pumpMaxDistance.get()));
         }

         if (factory.has("miningMaxDepth")) {
            BCCoreConfig.miningMaxDepth.set(intVal(factory, "miningMaxDepth", BCCoreConfig.miningMaxDepth.get()));
         }

         if (factory.has("miningMultiplier")) {
            BCCoreConfig.miningMultiplier.set(doubleVal(factory, "miningMultiplier", BCCoreConfig.miningMultiplier.get()));
            BlockUtil.miningMultiplier = BCCoreConfig.miningMultiplier.get();
         }
      }
   }

   private static void applyTransport(JsonObject transport) {
      if (transport != null) {
         BCTransportConfig.disableRfPipe.set(bool(transport, "disableRfPipe", BCTransportConfig.disableRfPipe.get()));
         BCTransportConfig.mjPerItem.set(longVal(transport, "mjPerItem", BCTransportConfig.mjPerItem.get()));
         BCTransportConfig.mjPerMillibucket.set(longVal(transport, "mjPerMillibucket", BCTransportConfig.mjPerMillibucket.get()));
         BCTransportConfig.basePowerRate.set(intVal(transport, "basePowerRate", BCTransportConfig.basePowerRate.get()));
         BCTransportConfig.baseRfRate.set(intVal(transport, "baseRfRate", BCTransportConfig.baseRfRate.get()));
         BCTransportConfig.baseFlowRate.set(intVal(transport, "baseFlowRate", BCTransportConfig.baseFlowRate.get()));
      }
   }

   private static void applyEnergy(JsonObject energy) {
      if (energy != null) {
         BCEnergyConfig.oilIsSticky.set(bool(energy, "oilIsSticky", BCEnergyConfig.oilIsSticky.get()));
         BCEnergyConfig.enableOilBurn.set(bool(energy, "enableOilBurn", BCEnergyConfig.enableOilBurn.get()));
         BCEnergyConfig.useRfNaming.set(bool(energy, "useRfNaming", BCEnergyConfig.useRfNaming.get()));
         BCEnergyConfig.useFullUnitNames.set(bool(energy, "useFullUnitNames", BCEnergyConfig.useFullUnitNames.get()));
         BCEnergyConfig.enableOilGeneration.set(bool(energy, "enableOilGeneration", BCEnergyConfig.enableOilGeneration.get()));
         BCEnergyConfig.enableNetherOilGeneration.set(bool(energy, "enableNetherOilGeneration", BCEnergyConfig.enableNetherOilGeneration.get()));
         BCEnergyConfig.netherOilGenRateMultiplier.set(doubleVal(energy, "netherOilGenRateMultiplier", BCEnergyConfig.netherOilGenRateMultiplier.get()));
         BCEnergyConfig.oilWellGenerationRate.set(doubleVal(energy, "oilWellGenerationRate", BCEnergyConfig.oilWellGenerationRate.get()));
         BCEnergyConfig.enableOilSpouts.set(bool(energy, "enableOilSpouts", BCEnergyConfig.enableOilSpouts.get()));
         BCEnergyConfig.finiteSpoutMinHeight.set(intVal(energy, "finiteSpoutMinHeight", BCEnergyConfig.finiteSpoutMinHeight.get()));
         BCEnergyConfig.finiteSpoutMaxHeight.set(intVal(energy, "finiteSpoutMaxHeight", BCEnergyConfig.finiteSpoutMaxHeight.get()));
         BCEnergyConfig.largeSpoutMinHeight.set(intVal(energy, "largeSpoutMinHeight", BCEnergyConfig.largeSpoutMinHeight.get()));
         BCEnergyConfig.largeSpoutMaxHeight.set(intVal(energy, "largeSpoutMaxHeight", BCEnergyConfig.largeSpoutMaxHeight.get()));
         BCEnergyConfig.mediumOilGenProb.set(doubleVal(energy, "mediumOilGenProb", BCEnergyConfig.mediumOilGenProb.get()));
         BCEnergyConfig.largeOilGenProb.set(doubleVal(energy, "largeOilGenProb", BCEnergyConfig.largeOilGenProb.get()));
         if (energy.has("forceExcessiveOilBiomes")) {
            BCEnergyConfig.forceExcessiveOilBiomes.set(stringList(energy.getAsJsonArray("forceExcessiveOilBiomes")));
         }

         if (energy.has("richSurfaceDepositBiomes")) {
            BCEnergyConfig.richSurfaceDepositBiomes.set(stringList(energy.getAsJsonArray("richSurfaceDepositBiomes")));
         }

         if (energy.has("surfaceDepositBiomes")) {
            BCEnergyConfig.surfaceDepositBiomes.set(stringList(energy.getAsJsonArray("surfaceDepositBiomes")));
         }

         if (energy.has("standardSurfaceDepositBiomes")) {
            BCEnergyConfig.standardSurfaceDepositBiomes.set(stringList(energy.getAsJsonArray("standardSurfaceDepositBiomes")));
         }

         if (energy.has("mountainousSurfaceDepositBiomes")) {
            BCEnergyConfig.mountainousSurfaceDepositBiomes.set(stringList(energy.getAsJsonArray("mountainousSurfaceDepositBiomes")));
         }

         if (energy.has("excludedBiomes")) {
            BCEnergyConfig.excludedBiomes.set(stringList(energy.getAsJsonArray("excludedBiomes")));
         }

         if (energy.has("excludedDimensions")) {
            BCEnergyConfig.excludedDimensions.set(stringList(energy.getAsJsonArray("excludedDimensions")));
         }

         String biomeMode = string(energy, "biomeListMode", BCEnergyConfig.biomeListMode.get().name());
         String dimMode = string(energy, "dimensionListMode", BCEnergyConfig.dimensionListMode.get().name());

         try {
            BCEnergyConfig.biomeListMode.set(BCEnergyConfig.ListMode.valueOf(biomeMode));
         } catch (IllegalArgumentException ignored) {
            LOGGER.warn("Unknown biomeListMode '{}'", biomeMode);
         }

         try {
            BCEnergyConfig.dimensionListMode.set(BCEnergyConfig.ListMode.valueOf(dimMode));
         } catch (IllegalArgumentException ignored) {
            LOGGER.warn("Unknown dimensionListMode '{}'", dimMode);
         }
      }
   }

   private static JsonObject defaults() {
      JsonObject root = new JsonObject();
      JsonObject core = new JsonObject();
      core.addProperty("worldGen", true);
      core.addProperty("minePlayerProtected", false);
      core.addProperty("pumpsConsumeWater", false);
      core.addProperty("markerMaxDistance", 64);
      core.addProperty("pumpMaxDistance", 64);
      core.addProperty("networkUpdateRate", 10);
      core.addProperty("miningMultiplier", 1.0);
      core.addProperty("miningMaxDepth", 512);
      root.add("core", core);
      JsonObject lib = new JsonObject();
      lib.addProperty("powerMode", "MJ_ONLY");
      lib.addProperty("colorBlindMode", "AUTO");
      lib.addProperty("mjRfConversion", 0.1);
      lib.addProperty("canEnginesExplode", false);
      root.add("lib", lib);
      JsonObject energy = new JsonObject();
      energy.addProperty("oilIsSticky", true);
      energy.addProperty("enableOilBurn", true);
      energy.addProperty("useRfNaming", false);
      energy.addProperty("useFullUnitNames", true);
      energy.addProperty("enableOilGeneration", true);
      energy.addProperty("enableNetherOilGeneration", true);
      energy.addProperty("netherOilGenRateMultiplier", 4.0);
      energy.addProperty("oilWellGenerationRate", 1.0);
      energy.addProperty("enableOilSpouts", true);
      energy.addProperty("finiteSpoutMinHeight", 7);
      energy.addProperty("finiteSpoutMaxHeight", 10);
      energy.addProperty("largeSpoutMinHeight", 13);
      energy.addProperty("largeSpoutMaxHeight", 20);
      energy.addProperty("mediumOilGenProb", 0.001);
      energy.addProperty("largeOilGenProb", 4.0E-4);
      energy.add("forceExcessiveOilBiomes", GSON.toJsonTree(List.of()));
      energy.add("richSurfaceDepositBiomes", GSON.toJsonTree(BCEnergyConfig.getRichSurfaceDepositBiomes().stream().map(id -> id.toString()).sorted().toList()));
      energy.add("surfaceDepositBiomes", GSON.toJsonTree(BCEnergyConfig.getSurfaceDepositBiomes().stream().map(id -> id.toString()).sorted().toList()));
      energy.add(
         "standardSurfaceDepositBiomes", GSON.toJsonTree(BCEnergyConfig.getStandardSurfaceDepositBiomes().stream().map(id -> id.toString()).sorted().toList())
      );
      energy.add(
         "mountainousSurfaceDepositBiomes",
         GSON.toJsonTree(BCEnergyConfig.getMountainousSurfaceDepositBiomes().stream().map(id -> id.toString()).sorted().toList())
      );
      energy.add("excludedBiomes", GSON.toJsonTree(BCEnergyConfig.getExcludedBiomes().stream().map(id -> id.toString()).sorted().toList()));
      energy.add("excludedDimensions", GSON.toJsonTree(BCEnergyConfig.getExcludedDimensions().stream().map(id -> id.toString()).sorted().toList()));
      energy.addProperty("biomeListMode", "BLACKLIST");
      energy.addProperty("dimensionListMode", "BLACKLIST");
      root.add("energy", energy);
      JsonObject factory = new JsonObject();
      factory.addProperty("pumpsConsumeWater", false);
      factory.addProperty("pumpMaxDistance", 64);
      factory.addProperty("miningMaxDepth", 512);
      factory.addProperty("miningMultiplier", 1.0);
      root.add("factory", factory);
      JsonObject builders = new JsonObject();
      JsonObject quarry = new JsonObject();
      quarry.addProperty("quarryFrameMinHeight", 4);
      quarry.addProperty("quarryMaxTasksPerTick", 4);
      quarry.addProperty("quarryTaskPowerDivisor", 2);
      quarry.addProperty("quarryMaxFrameMoveSpeed", 0.0);
      quarry.addProperty("quarryMaxBlockMineRate", 0.0);
      builders.add("quarry", quarry);
      root.add("builders", builders);
      JsonObject transport = new JsonObject();
      transport.addProperty("disableRfPipe", false);
      transport.addProperty("mjPerItem", 1000000L);
      transport.addProperty("mjPerMillibucket", 1000L);
      transport.addProperty("basePowerRate", 4);
      transport.addProperty("baseRfRate", 40);
      transport.addProperty("baseFlowRate", 10);
      root.add("transport", transport);
      return root;
   }

   private static void write(Path path, JsonObject root) {
      try {
         Files.createDirectories(path.getParent());

         try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(root, writer);
         }
      } catch (IOException e) {
         LOGGER.error("Failed to write {}", path, e);
      }
   }

   private static boolean bool(JsonObject obj, String key, boolean fallback) {
      return obj.has(key) ? obj.get(key).getAsBoolean() : fallback;
   }

   private static int intVal(JsonObject obj, String key, int fallback) {
      return obj.has(key) ? obj.get(key).getAsInt() : fallback;
   }

   private static long longVal(JsonObject obj, String key, long fallback) {
      return obj.has(key) ? obj.get(key).getAsLong() : fallback;
   }

   private static double doubleVal(JsonObject obj, String key, double fallback) {
      return obj.has(key) ? obj.get(key).getAsDouble() : fallback;
   }

   private static String string(JsonObject obj, String key, String fallback) {
      return obj.has(key) ? obj.get(key).getAsString() : fallback;
   }

   private static List<String> stringList(JsonArray array) {
      return new ArrayList<>(array.asList().stream().map(e -> e.getAsString()).toList());
   }
}
