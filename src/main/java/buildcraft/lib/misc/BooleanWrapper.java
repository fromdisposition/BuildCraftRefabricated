package buildcraft.lib.misc;

public class BooleanWrapper {
    private boolean value;

    public BooleanWrapper(boolean defaultValue) {
        this.value = defaultValue;
    }

    public boolean evaluate() {
        return value;
    }

    public void set(boolean newValue) {
        this.value = newValue;
    }
}
