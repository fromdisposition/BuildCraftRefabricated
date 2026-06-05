package buildcraft.lib.client.model.data;

public final class ModelData {
    public static final ModelData EMPTY = new ModelData();

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        public <T> Builder with(ModelProperty<T> property, T value) {
            return this;
        }

        public ModelData build() {
            return EMPTY;
        }
    }
}
