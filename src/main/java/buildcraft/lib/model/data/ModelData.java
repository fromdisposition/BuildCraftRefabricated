package buildcraft.lib.model.data;

public final class ModelData {
   public static final ModelData EMPTY = new ModelData();

   public static ModelData.Builder builder() {
      return new ModelData.Builder();
   }

   public static final class Builder {
      public <T> ModelData.Builder with(ModelProperty<T> property, T value) {
         return this;
      }

      public ModelData build() {
         return ModelData.EMPTY;
      }
   }
}
