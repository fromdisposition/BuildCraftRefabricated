package buildcraft.lib.client.model.data;

public final class ModelProperty<T> {
   private ModelProperty() {
   }

   public static <T> ModelProperty<T> create() {
      return new ModelProperty<>();
   }
}
