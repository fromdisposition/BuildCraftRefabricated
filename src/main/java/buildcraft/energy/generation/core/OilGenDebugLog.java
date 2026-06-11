package buildcraft.energy.generation.core;

import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

/** Session debug logger — remove after oilgen border investigation. */
public final class OilGenDebugLog {
   private static final Gson GSON = new Gson();
   private static final String SESSION = "2da5ca";
   private static final Path[] LOG_PATHS = {
      Path.of("c:/Users/User/Desktop/lotr/debug-2da5ca.log"),
      Path.of(System.getProperty("user.dir", ".")).resolve("debug-2da5ca.log"),
      Path.of(System.getProperty("user.dir", ".")).getParent().resolve("debug-2da5ca.log")
   };

   private OilGenDebugLog() {
   }

   public static void log(String hypothesisId, String location, String message, Map<String, Object> data) {
      Map<String, Object> payload = new LinkedHashMap<>(data);
      Object runId = payload.remove("runId");
      Map<String, Object> entry = new LinkedHashMap<>();
      entry.put("sessionId", SESSION);
      entry.put("hypothesisId", hypothesisId);
      entry.put("location", location);
      entry.put("message", message);
      entry.put("data", payload);
      entry.put("timestamp", System.currentTimeMillis());
      entry.put("runId", runId != null ? runId : "pre-fix");
      String line = GSON.toJson(entry) + System.lineSeparator();
      for (Path path : LOG_PATHS) {
         try {
            if (path.getParent() != null) {
               Files.createDirectories(path.getParent());
            }
            Files.writeString(path, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return;
         } catch (Exception ignored) {
         }
      }
   }
}
