package buildcraft.core;

public final class BCCoreConfig {

    public static final BooleanValue worldGen = new BooleanValue(true);
    public static final BooleanValue minePlayerProtected = new BooleanValue(false);
    public static final BooleanValue pumpsConsumeWater = new BooleanValue(false);
    public static final IntValue markerMaxDistance = new IntValue(64);
    public static final IntValue pumpMaxDistance = new IntValue(64);
    public static final IntValue networkUpdateRate = new IntValue(10);
    public static final DoubleValue miningMultiplier = new DoubleValue(1.0);
    public static final IntValue miningMaxDepth = new IntValue(512);

    private BCCoreConfig() {}

    public static void buildGeneral(Object builder) {}

    public static void buildWorldgen(Object builder) {}

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

    public static final class IntValue {
        private int value;

        public IntValue(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }

        public void set(int value) {
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

    public static final class StringListValue {
        private java.util.List<String> value;

        public StringListValue(java.util.List<String> value) {
            this.value = new java.util.ArrayList<>(value);
        }

        public java.util.List<? extends String> get() {
            return value;
        }

        public void set(java.util.List<String> value) {
            this.value = new java.util.ArrayList<>(value);
        }
    }

    public static final class EnumValue<T extends Enum<T>> {
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
}
