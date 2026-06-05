package buildcraft.lib.config;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigSpec {
    public static class BooleanValue {
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

    public static class IntValue {
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

    public static class LongValue {
        private long value;

        public LongValue(long value) {
            this.value = value;
        }

        public long get() {
            return value;
        }

        public void set(long value) {
            this.value = value;
        }
    }

    public static class DoubleValue {
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

    public static class ConfigValue<T> {
        private T value;

        public ConfigValue(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    public static class EnumValue<T extends Enum<T>> {
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

    public static class Builder {
        public Builder push(String path) { return this; }
        public Builder pop() { return this; }
        public Builder comment(String... comment) { return this; }

        public BooleanValue define(String path, boolean defaultValue) {
            return new BooleanValue(defaultValue);
        }

        public IntValue defineInRange(String path, int defaultValue, int min, int max) {
            return new IntValue(defaultValue);
        }

        public LongValue defineInRange(String path, long defaultValue, long min, long max) {
            return new LongValue(defaultValue);
        }

        public DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
            return new DoubleValue(defaultValue);
        }

        public <T extends Enum<T>> EnumValue<T> defineEnum(String path, T defaultValue) {
            return new EnumValue<>(defaultValue);
        }

        public ConfigValue<List<? extends String>> defineListAllowEmpty(
                String path,
                List<? extends String> defaultValue,
                Supplier<String> elementSupplier,
                Predicate<Object> validator) {
            @SuppressWarnings("unchecked")
            ConfigValue<List<? extends String>> value = new ConfigValue<>((List<? extends String>) defaultValue);
            return value;
        }

        public ConfigSpec build() { return new ConfigSpec(); }
    }
}
