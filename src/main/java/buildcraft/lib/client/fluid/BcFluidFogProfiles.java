package buildcraft.lib.client.fluid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

/** Data-driven fog/overlay tuning loaded from {@code assets/buildcraftenergy/bc_fluid_fog_profiles.json}. */
public final class BcFluidFogProfiles {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Identifier PROFILE_ID = Identifier.fromNamespaceAndPath("buildcraftenergy", "bc_fluid_fog_profiles.json");

   private static Profile defaultLiquid = bakedDefaults().liquid();
   private static Profile defaultGaseous = bakedDefaults().gaseous();
   private static float defaultHeatClarityLiquid = 0.06F;
   private static float defaultHeatClarityGaseous = 0.03F;
   private static Map<String, FluidProfiles> fluids = Map.of();

   private BcFluidFogProfiles() {
   }

   public static Profile resolve(String baseName, boolean gaseous) {
      FluidProfiles fluid = fluids.get(baseName);
      if (fluid != null) {
         Profile profile = gaseous ? fluid.gaseous() : fluid.liquid();
         if (profile != null) {
            return profile;
         }
      }

      return gaseous ? defaultGaseous : defaultLiquid;
   }

   public static float heatClarityMultiplier(String baseName, boolean gaseous) {
      if (gaseous) {
         return defaultHeatClarityGaseous;
      }

      FluidProfiles fluid = fluids.get(baseName);
      return fluid != null && fluid.heatClarity() != null ? fluid.heatClarity() : defaultHeatClarityLiquid;
   }

   public static void reload(ResourceManager resourceManager) {
      try {
         Resource resource = resourceManager.getResourceOrThrow(PROFILE_ID);
         try (InputStream stream = resource.open()) {
            apply(JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject());
         }
      } catch (Exception exception) {
         LOGGER.warn("Failed to load BC fluid fog profiles, using baked defaults", exception);
         applyDefaults();
      }
   }

   public static void loadFromClasspath() {
      InputStream stream = BcFluidFogProfiles.class.getClassLoader().getResourceAsStream("assets/buildcraftenergy/bc_fluid_fog_profiles.json");
      if (stream == null) {
         applyDefaults();
         return;
      }

      try (InputStream in = stream) {
         apply(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject());
      } catch (Exception exception) {
         applyDefaults();
      }
   }

   private static void apply(JsonObject root) {
      JsonObject defaults = root.getAsJsonObject("defaults");
      defaultLiquid = readProfile(defaults.getAsJsonObject("liquid"), bakedDefaults().liquid());
      defaultGaseous = readProfile(defaults.getAsJsonObject("gaseous"), bakedDefaults().gaseous());
      defaultHeatClarityLiquid = readFloat(defaults, "heat_clarity_liquid", 0.06F);
      defaultHeatClarityGaseous = readFloat(defaults, "heat_clarity_gaseous", 0.03F);

      Map<String, FluidProfiles> parsed = new HashMap<>();
      JsonObject fluidsJson = root.getAsJsonObject("fluids");
      if (fluidsJson != null) {
         for (Map.Entry<String, JsonElement> entry : fluidsJson.entrySet()) {
            JsonObject fluid = entry.getValue().getAsJsonObject();
            Profile liquid = fluid.has("liquid") ? readProfile(fluid.getAsJsonObject("liquid"), null) : null;
            Profile gaseous = fluid.has("gaseous") ? readProfile(fluid.getAsJsonObject("gaseous"), null) : null;
            Float heatClarity = fluid.has("heat_clarity") ? fluid.get("heat_clarity").getAsFloat() : null;
            parsed.put(entry.getKey(), new FluidProfiles(liquid, gaseous, heatClarity));
         }
      }

      fluids = Map.copyOf(parsed);
   }

   private static void applyDefaults() {
      Defaults defaults = bakedDefaults();
      defaultLiquid = defaults.liquid();
      defaultGaseous = defaults.gaseous();
      defaultHeatClarityLiquid = 0.06F;
      defaultHeatClarityGaseous = 0.03F;
      fluids = Map.of();
   }

   private static Profile readProfile(JsonObject json, Profile fallback) {
      if (json == null) {
         return fallback;
      }

      return new Profile(
         readFloat(json, "environmental_start", fallback != null ? fallback.environmentalStart() : 0.18F),
         readFloat(json, "environmental_end", fallback != null ? fallback.environmentalEnd() : 1.5F),
         readFloat(json, "liquid_alpha", fallback != null ? fallback.liquidAlpha() : 0.95F),
         readFloat(json, "gaseous_alpha", fallback != null ? fallback.gaseousAlpha() : 0.55F),
         readFloat(json, "overlay_alpha", fallback != null ? fallback.overlayAlpha() : 0.10F)
      );
   }

   private static float readFloat(JsonObject json, String key, float fallback) {
      return json.has(key) ? json.get(key).getAsFloat() : fallback;
   }

   private static Defaults bakedDefaults() {
      return new Defaults(
         new Profile(0.18F, 1.5F, 0.95F, 0.55F, 0.10F),
         new Profile(0.20F, 1.5F, 0.55F, 0.58F, 0.10F)
      );
   }

   public record Profile(float environmentalStart, float environmentalEnd, float liquidAlpha, float gaseousAlpha, float overlayAlpha) {
   }

   private record FluidProfiles(Profile liquid, Profile gaseous, Float heatClarity) {
   }

   private record Defaults(Profile liquid, Profile gaseous) {
   }
}
