package buildcraft.fabric.fluid;

import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

/** Session debug logger — remove after oil fluid physics verification. */
public final class OilFluidDebugLog {
   private static final Gson GSON = new Gson();
   private static final Path LOG = Path.of("debug-d80670.log");

   private OilFluidDebugLog() {
   }

   public static void log(String location, String message, String hypothesisId, Map<String, Object> data) {
      // #region agent log
      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("sessionId", "d80670");
      payload.put("timestamp", System.currentTimeMillis());
      payload.put("location", location);
      payload.put("message", message);
      payload.put("hypothesisId", hypothesisId);
      payload.put("runId", "pre-fix");
      payload.put("data", data);
      try {
         Files.writeString(LOG, GSON.toJson(payload) + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      } catch (Exception ignored) {
      }
      // #endregion
   }
}
