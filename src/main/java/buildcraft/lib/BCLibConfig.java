package buildcraft.lib;

public final class BCLibConfig {
    public static final PowerMode POWER_MODE = PowerMode.MJ_ONLY;
    public static final ColorBlindMode COLOR_BLIND_MODE = ColorBlindMode.AUTO;
    public static final double MJ_RF_CONVERSION = 0.1;
    public static final boolean CAN_ENGINES_EXPLODE = false;

    public static final EnumValue<PowerMode> powerMode = new EnumValue<>(POWER_MODE);
    public static final EnumValue<ColorBlindMode> colorBlindMode = new EnumValue<>(COLOR_BLIND_MODE);
    public static final DoubleValue mjRfConversionAmount = new DoubleValue(MJ_RF_CONVERSION);
    public static final BooleanValue canEnginesExplode = new BooleanValue(CAN_ENGINES_EXPLODE);

    private BCLibConfig() {}

    public enum PowerMode {
        MJ_ONLY(false),
        MJ_AUTOCONVERT_RF(true),
        DISPLAY_RF(true);

        public final boolean autoconvert;

        PowerMode(boolean autoconvert) {
            this.autoconvert = autoconvert;
        }
    }

    public enum ColorBlindMode {
        AUTO,
        ON,
        OFF
    }

    public static final class EnumValue<T> {
        private T value;

        public EnumValue(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    public static final class DoubleValue {
        private double value;

        public DoubleValue(double value) {
            this.value = value;
        }

        public double get() {
            return value;
        }

        public void set(double value) {
            this.value = value;
        }
    }

    public static final class BooleanValue {
        private boolean value;

        public BooleanValue(boolean value) {
            this.value = value;
        }

        public boolean get() {
            return value;
        }

        public void set(boolean value) {
            this.value = value;
        }
    }
}
