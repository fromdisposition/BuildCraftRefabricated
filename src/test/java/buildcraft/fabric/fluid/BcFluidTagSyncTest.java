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
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BcFluidTagSyncTest {
   @Test
   void bcFluidsTagMatchesGeneratedValues() {
      Set<String> tag = loadTagValues();
      Set<String> expected = new HashSet<>(BcFluidWorldProperties.allFluidTagValues());
      assertEquals(expected, tag);
   }

   @Test
   void swimmableFluidsAreSubsetOfBcFluidsTag() {
      Set<String> tag = loadTagValues();
      for (String id : BcFluidWorldProperties.swimmableFluidTagValues()) {
         assertTrue(tag.contains(id), "Swimmable fluid missing from bc_fluids.json: " + id);
      }
   }

   @Test
   void gaseousFluidsAreNotSwimmable() {
      Set<String> swimmable = new HashSet<>(BcFluidWorldProperties.swimmableFluidTagValues());
      for (String id : BcFluidWorldProperties.allFluidTagValues()) {
         if (id.contains("fuel_gaseous")) {
            assertFalse(swimmable.contains(id), "Gaseous fluid must not be swimmable: " + id);
         }
      }
   }

   private static Set<String> loadTagValues() {
      InputStream stream = BcFluidTagSyncTest.class.getClassLoader()
         .getResourceAsStream("data/buildcraftenergy/tags/fluid/bc_fluids.json");
      assert stream != null : "bc_fluids.json missing from classpath";
      JsonElement root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      JsonArray values = root.getAsJsonObject().getAsJsonArray("values");
      Set<String> ids = new HashSet<>();
      for (JsonElement element : values) {
         ids.add(element.getAsString());
      }

      return ids;
   }
}
