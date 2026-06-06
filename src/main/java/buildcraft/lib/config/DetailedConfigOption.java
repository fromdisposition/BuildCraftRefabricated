package buildcraft.lib.config;

public class DetailedConfigOption {
   private final String key;
   private final String defaultValue;

   public DetailedConfigOption(String key, String defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
   }

   public float getAsFloat() {
      try {
         return Float.parseFloat(this.defaultValue);
      } catch (NumberFormatException e) {
         return 0.725F;
      }
   }

   public int getAsInt() {
      try {
         return Integer.parseInt(this.defaultValue);
      } catch (NumberFormatException e) {
         return 0;
      }
   }

   public String get() {
      return this.defaultValue;
   }
}
