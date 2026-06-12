package buildcraft.fabric.fluid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BcFluidTagSyncTest {
   @Test
   void bcFluidsTagMatchesGeneratedValues() {
      Set<String> tag = loadTagValues("data/buildcraftenergy/tags/fluid/bc_fluids.json");
      Set<String> expected = new HashSet<>(BcFluidWorldProperties.allFluidTagValues());
      assertEquals(expected, tag);
   }

   @Test
   void bcLiquidsTagMatchesGeneratedValues() {
      Set<String> tag = loadTagValues("data/buildcraftenergy/tags/fluid/bc_liquids.json");
      Set<String> expected = new HashSet<>(BcFluidWorldProperties.liquidFluidTagValues());
      assertEquals(expected, tag);
   }

   @Test
   void bcLiquidsAreSubsetOfBcFluidsTag() {
      Set<String> all = loadTagValues("data/buildcraftenergy/tags/fluid/bc_fluids.json");
      for (String id : BcFluidWorldProperties.liquidFluidTagValues()) {
         assertTrue(all.contains(id), "Liquid fluid missing from bc_fluids.json: " + id);
      }
   }

   @Test
   void gaseousFluidsAreExcludedFromBcLiquidsTag() {
      Set<String> liquids = loadTagValues("data/buildcraftenergy/tags/fluid/bc_liquids.json");

      for (int i = 0; i < BcFluidWorldProperties.FLUID_DATA.length; i++) {
         int[] data = BcFluidWorldProperties.FLUID_DATA[i];
         String baseName = BcFluidWorldProperties.FLUID_NAMES[i];

         for (int heat = 0; heat < 3; heat++) {
            BcFluidWorldProperties props = BcFluidWorldProperties.compute(
               baseName, heat, data[0], data[1], data[2], data[3], data[4], data[5], true, data[6], true, data[7]
            );
            if (!props.gaseous()) {
               continue;
            }

            String reg = BcFluidWorldProperties.regName(baseName, heat);
            assertFalse(liquids.contains("buildcraftenergy:" + reg), "Gaseous fluid must not be in bc_liquids.json: " + reg);
            assertFalse(liquids.contains("buildcraftenergy:" + reg + "_flowing"), "Gaseous fluid must not be in bc_liquids.json: " + reg + "_flowing");
         }
      }
   }

   private static Set<String> loadTagValues(String resourcePath) {
      InputStream stream = BcFluidTagSyncTest.class.getClassLoader().getResourceAsStream(resourcePath);
      assert stream != null : resourcePath + " missing from classpath";
      JsonElement root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      JsonArray values = root.getAsJsonObject().getAsJsonArray("values");
      Set<String> ids = new HashSet<>();
      for (JsonElement element : values) {
         ids.add(element.getAsString());
      }

      return ids;
   }
}
