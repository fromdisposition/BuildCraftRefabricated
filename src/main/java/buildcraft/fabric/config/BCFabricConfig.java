package buildcraft.fabric.config;

import buildcraft.builders.BCBuildersConfig;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.BCUnifiedClientConfig;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.builders.platform.BCBuildersFabric;
import buildcraft.energy.platform.BCEnergyFabric;
import buildcraft.factory.platform.BCFactoryFabric;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.fabric.loader.GamePaths;
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
      BCObjectsConfig.load();
      Path path = GamePaths.BUILDCRAFT_CONFIG_DIR.resolve(FILE_NAME);
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

      if (migrateLegacyCoreFactoryKeys(root)) {
         write(path, root);
         LOGGER.info("Moved pump/mining settings from core to factory in {}", path);
      }

      apply(root);
   }

   public static void reload() {
      BCObjectsConfig.load();
      load();
      BCFactoryFabric.onConfigReloaded();
      BCEnergyFabric.onConfigReloaded();
      BCBuildersFabric.onConfigReloaded();
   }

   private static void apply(JsonObject root) {
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
         BCCoreConfig.hidePower.set(bool(core, "hidePowerValues", BCCoreConfig.hidePower.get()));
         BCCoreConfig.hideFluid.set(bool(core, "hideFluidValues", BCCoreConfig.hideFluid.get()));
         BCCoreConfig.minePlayerProtected.set(bool(core, "minePlayerProtected", BCCoreConfig.minePlayerProtected.get()));
         BCCoreConfig.markerMaxDistance.set(intVal(core, "markerMaxDistance", BCCoreConfig.markerMaxDistance.get()));
         BCCoreConfig.networkUpdateRate.set(intVal(core, "networkUpdateRate", BCCoreConfig.networkUpdateRate.get()));
      }
   }

   private static void applyLib(JsonObject lib) {
      if (lib != null) {
         BCLibConfig.mjRfConversionAmount.set(doubleVal(lib, "mjRfConversion", BCLibConfig.mjRfConversionAmount.get()));
         BCLibConfig.canEnginesExplode.set(bool(lib, "canEnginesExplode", BCLibConfig.canEnginesExplode.get()));
         BCLibConfig.useColouredLabels.set(bool(lib, "useColouredLabels", BCLibConfig.useColouredLabels.get()));
         BCLibConfig.useHighContrastLabelColours.set(bool(lib, "useHighContrastLabelColours", BCLibConfig.useHighContrastLabelColours.get()));
         BCLibConfig.useBucketsStatic.set(bool(lib, "useBucketsStatic", BCLibConfig.useBucketsStatic.get()));
         BCLibConfig.useBucketsFlow.set(bool(lib, "useBucketsFlow", BCLibConfig.useBucketsFlow.get()));
         BCLibConfig.useLongLocalizedName.set(bool(lib, "useLongLocalizedName", BCLibConfig.useLongLocalizedName.get()));
         BCLibConfig.useSwappableSprites.set(bool(lib, "useSwappableSprites", BCLibConfig.useSwappableSprites.get()));
         BCLibConfig.enableAnimatedSprites.set(bool(lib, "enableAnimatedSprites", BCLibConfig.enableAnimatedSprites.get()));
         BCLibConfig.guideShowDetail.set(bool(lib, "guideBookEnableDetail", BCLibConfig.guideShowDetail.get()));
         BCLibConfig.itemLifespan.set(intVal(lib, "itemLifespan", BCLibConfig.itemLifespan.get()));
         BCLibConfig.guideItemSearchLimit.set(intVal(lib, "guideItemSearchLimit", BCLibConfig.guideItemSearchLimit.get()));
         BCLibConfig.maxGuideSearchCount.set(intVal(lib, "maxGuideSearchCount", BCLibConfig.maxGuideSearchCount.get()));
         String timeGap = string(lib, "displayTimeGap", BCLibConfig.displayTimeGap.get().name());

         try {
            BCLibConfig.displayTimeGap.set(BCLibConfig.TimeGap.valueOf(timeGap.toUpperCase()));
         } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown displayTimeGap '{}', keeping {}", timeGap, BCLibConfig.displayTimeGap.get());
         }

         String itemRotation = string(lib, "itemRenderRotation", BCLibConfig.rotateTravelingItems.get().name());

         try {
            BCLibConfig.rotateTravelingItems.set(BCLibConfig.RenderRotation.valueOf(itemRotation.toUpperCase()));
         } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown itemRenderRotation '{}', keeping {}", itemRotation, BCLibConfig.rotateTravelingItems.get());
         }

         String chunkLoadLevel = string(lib, "chunkLoadLevel", BCLibConfig.chunkLoadingLevel.get().name());

         try {
            BCLibConfig.chunkLoadingLevel.set(BCLibConfig.ChunkLoaderLevel.valueOf(chunkLoadLevel.toUpperCase()));
         } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown chunkLoadLevel '{}', keeping {}", chunkLoadLevel, BCLibConfig.chunkLoadingLevel.get());
         }

         XmlPageLoader.SHOW_DETAIL = BCLibConfig.guideShowDetail.get();
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
         BCCoreConfig.pumpsConsumeWater.set(bool(factory, "pumpsConsumeWater", BCCoreConfig.pumpsConsumeWater.get()));
         BCCoreConfig.pumpMaxDistance.set(intVal(factory, "pumpMaxDistance", BCCoreConfig.pumpMaxDistance.get()));
         BCCoreConfig.miningMaxDepth.set(intVal(factory, "miningMaxDepth", BCCoreConfig.miningMaxDepth.get()));
         BCCoreConfig.miningMultiplier.set(doubleVal(factory, "miningMultiplier", BCCoreConfig.miningMultiplier.get()));
         BlockUtil.miningMultiplier = BCCoreConfig.miningMultiplier.get();
      }
   }

   private static boolean migrateLegacyCoreFactoryKeys(JsonObject root) {
      if (!root.has("core")) {
         return false;
      }

      JsonObject core = root.getAsJsonObject("core");
      JsonObject factory = root.has("factory") ? root.getAsJsonObject("factory") : null;
      if (factory == null) {
         factory = new JsonObject();
         root.add("factory", factory);
      }

      boolean changed = false;

      for (String key : new String[]{"pumpsConsumeWater", "pumpMaxDistance", "miningMaxDepth", "miningMultiplier"}) {
         if (core.has(key)) {
            if (!factory.has(key)) {
               factory.add(key, core.get(key));
            }

            core.remove(key);
            changed = true;
         }
      }

      return changed;
   }

   private static void applyTransport(JsonObject transport) {
      if (transport != null) {
         BCTransportConfig.disableRfPipe = bool(transport, "disableRfPipe", BCTransportConfig.disableRfPipe);
         BCTransportConfig.mjPerItem = longVal(transport, "mjPerItem", BCTransportConfig.mjPerItem);
         BCTransportConfig.mjPerMillibucket = longVal(transport, "mjPerMillibucket", BCTransportConfig.mjPerMillibucket);
         BCTransportConfig.basePowerRate = intVal(transport, "basePowerRate", BCTransportConfig.basePowerRate);
         BCTransportConfig.baseRfRate = intVal(transport, "baseRfRate", BCTransportConfig.baseRfRate);
         BCTransportConfig.baseFlowRate = intVal(transport, "baseFlowRate", BCTransportConfig.baseFlowRate);
      }
   }

   private static void applyOilGeneratorConfig(JsonObject generator) {
      if (generator == null) {
         return;
      }

      BCCoreConfig.worldGen.set(bool(generator, "worldGen", BCCoreConfig.worldGen.get()));
      BCEnergyConfig.enableOilOnWater.set(bool(generator, "enableOilOnWater", BCEnergyConfig.enableOilOnWater.get()));
      BCEnergyConfig.enableOilGeneration.set(bool(generator, "enableOilGeneration", BCEnergyConfig.enableOilGeneration.get()));
      BCEnergyConfig.enableOilSprings.set(bool(generator, "enableOilSprings", BCEnergyConfig.enableOilSprings.get()));
      BCEnergyConfig.enableWaterSprings.set(bool(generator, "enableWaterSprings", BCEnergyConfig.enableWaterSprings.get()));
      BCEnergyConfig.refreshWaterSpringFlag();
      BCEnergyConfig.oilDesertRichChancePercent.set(clampPercent(intVal(
         generator,
         "oilDesertRichChancePercent",
         BCEnergyConfig.oilDesertRichChancePercent.get()
      )));
      BCEnergyConfig.oilOceanPatchChancePercent.set(clampPercent(intVal(
         generator,
         "oilOceanPatchChancePercent",
         BCEnergyConfig.oilOceanPatchChancePercent.get()
      )));

      if (generator.has("excludedDimensions")) {
         BCEnergyConfig.excludedDimensions.set(stringList(generator.getAsJsonArray("excludedDimensions")));
      }

      String dimMode = string(generator, "dimensionListMode", BCEnergyConfig.dimensionListMode.get().name());

      try {
         BCEnergyConfig.dimensionListMode.set(BCEnergyConfig.ListMode.valueOf(dimMode));
      } catch (IllegalArgumentException ignored) {
         LOGGER.warn("Unknown dimensionListMode '{}'", dimMode);
      }
   }

   private static void applyEnergy(JsonObject energy) {
      if (energy != null) {
         BCEnergyConfig.oilIsSticky.set(bool(energy, "oilIsSticky", BCEnergyConfig.oilIsSticky.get()));
         BCEnergyConfig.enableOilBurn.set(bool(energy, "enableOilBurn", BCEnergyConfig.enableOilBurn.get()));
         applyOilGeneratorConfig(energy.has("generator") ? energy.getAsJsonObject("generator") : null);
      }
   }

   private static JsonObject defaults() {
      JsonObject root = new JsonObject();
      JsonObject core = new JsonObject();
      core.addProperty("hidePowerValues", false);
      core.addProperty("hideFluidValues", false);
      core.addProperty("minePlayerProtected", false);
      core.addProperty("markerMaxDistance", 64);
      core.addProperty("networkUpdateRate", 10);
      root.add("core", core);
      JsonObject lib = new JsonObject();
      lib.addProperty("colorBlindMode", "AUTO");
      lib.addProperty("mjRfConversion", 0.1);
      lib.addProperty("canEnginesExplode", true);
      lib.addProperty("useColouredLabels", true);
      lib.addProperty("useHighContrastLabelColours", false);
      lib.addProperty("useBucketsStatic", true);
      lib.addProperty("useBucketsFlow", true);
      lib.addProperty("useLongLocalizedName", true);
      lib.addProperty("useSwappableSprites", true);
      lib.addProperty("enableAnimatedSprites", true);
      lib.addProperty("guideBookEnableDetail", false);
      lib.addProperty("itemLifespan", 60);
      lib.addProperty("guideItemSearchLimit", 10000);
      lib.addProperty("maxGuideSearchCount", 1200);
      lib.addProperty("displayTimeGap", "SECONDS");
      lib.addProperty("itemRenderRotation", "ENABLED");
      lib.addProperty("chunkLoadLevel", "SELF_TILES");
      root.add("lib", lib);
      JsonObject energy = new JsonObject();
      energy.addProperty("oilIsSticky", false);
      energy.addProperty("enableOilBurn", true);
      JsonObject generator = new JsonObject();
      generator.addProperty("worldGen", true);
      generator.addProperty("enableOilGeneration", true);
      generator.addProperty("enableOilOnWater", true);
      generator.addProperty("enableOilSprings", true);
      generator.addProperty("enableWaterSprings", true);
      generator.addProperty("oilDesertRichChancePercent", BCEnergyConfig.oilDesertRichChancePercent.get());
      generator.addProperty("oilOceanPatchChancePercent", BCEnergyConfig.oilOceanPatchChancePercent.get());
      generator.add("excludedDimensions", GSON.toJsonTree(BCEnergyConfig.getExcludedDimensions().stream().map(id -> id.toString()).sorted().toList()));
      generator.addProperty("dimensionListMode", "BLACKLIST");
      energy.add("generator", generator);
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

   /** Config values are BC-style percents (0.1 = 0.1%, 2 = 2%). Legacy absolute fractions (< 1%) are still accepted. */
   private static double percentVal(JsonObject obj, String key, double fallbackFraction) {
      if (!obj.has(key)) {
         return fallbackFraction;
      }

      double raw = obj.get(key).getAsDouble();
      if (raw > 0.0 && raw < 0.01) {
         return raw;
      }

      return raw / 100.0;
   }

   private static String string(JsonObject obj, String key, String fallback) {
      return obj.has(key) ? obj.get(key).getAsString() : fallback;
   }

   private static List<String> stringList(JsonArray array) {
      return new ArrayList<>(array.asList().stream().map(e -> e.getAsString()).toList());
   }

   private static int clampPercent(int value) {
      return Math.max(0, Math.min(100, value));
   }
}
