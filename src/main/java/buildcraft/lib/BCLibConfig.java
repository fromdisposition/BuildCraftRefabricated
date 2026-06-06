package buildcraft.lib;

public final class BCLibConfig {
   public static final BCLibConfig.PowerMode POWER_MODE = BCLibConfig.PowerMode.MJ_ONLY;
   public static final BCLibConfig.ColorBlindMode COLOR_BLIND_MODE = BCLibConfig.ColorBlindMode.AUTO;
   public static final double MJ_RF_CONVERSION = 0.1;
   public static final boolean CAN_ENGINES_EXPLODE = false;
   public static final BCLibConfig.EnumValue<BCLibConfig.PowerMode> powerMode = new BCLibConfig.EnumValue<>(POWER_MODE);
   public static final BCLibConfig.EnumValue<BCLibConfig.ColorBlindMode> colorBlindMode = new BCLibConfig.EnumValue<>(COLOR_BLIND_MODE);
   public static final BCLibConfig.DoubleValue mjRfConversionAmount = new BCLibConfig.DoubleValue(0.1);
   public static final BCLibConfig.BooleanValue canEnginesExplode = new BCLibConfig.BooleanValue(false);

   private BCLibConfig() {
   }

   public static final class BooleanValue {
      private boolean value;

      public BooleanValue(boolean value) {
         this.value = value;
      }

      public boolean get() {
         return this.value;
      }

      public void set(boolean value) {
         this.value = value;
      }
   }

   public enum ColorBlindMode {
      AUTO,
      ON,
      OFF;
   }

   public static final class DoubleValue {
      private double value;

      public DoubleValue(double value) {
         this.value = value;
      }

      public double get() {
         return this.value;
      }

      public void set(double value) {
         this.value = value;
      }
   }

   public static final class EnumValue<T> {
      private T value;

      public EnumValue(T value) {
         this.value = value;
      }

      public T get() {
         return this.value;
      }

      public void set(T value) {
         this.value = value;
      }
   }

   public enum PowerMode {
      MJ_ONLY(false),
      MJ_AUTOCONVERT_RF(true),
      DISPLAY_RF(true);

      public final boolean autoconvert;

      PowerMode(boolean autoconvert) {
         this.autoconvert = autoconvert;
      }
   }
}
