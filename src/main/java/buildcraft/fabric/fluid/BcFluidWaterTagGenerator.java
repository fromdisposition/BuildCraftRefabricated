package buildcraft.fabric.fluid;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class BcFluidWaterTagGenerator {
   private BcFluidWaterTagGenerator() {
   }

   public static void writeSwimmableWaterTag(Path outFile) throws Exception {
      List<String> values = BcFluidWorldProperties.swimmableFluidTagValues();
      String json = buildJson(values);
      Files.createDirectories(outFile.getParent());
      Files.writeString(outFile, json);
   }

   static String buildJson(List<String> values) {
      StringBuilder sb = new StringBuilder();
      sb.append("{\n  \"replace\": false,\n  \"values\": [\n");

      for (int i = 0; i < values.size(); i++) {
         sb.append("    \"").append(values.get(i)).append('"');
         if (i < values.size() - 1) {
            sb.append(',');
         }

         sb.append('\n');
      }

      sb.append("  ]\n}\n");
      return sb.toString();
   }

   public static void main(String[] args) throws Exception {
      if (args.length != 1) {
         throw new IllegalArgumentException("Usage: BcFluidWaterTagGenerator <water.json path>");
      }

      Path out = Path.of(args[0]);
      writeSwimmableWaterTag(out);
      System.out.println("Generated " + BcFluidWorldProperties.swimmableFluidTagValues().size() + " swimmable fluid tag entries in " + out);
   }
}
