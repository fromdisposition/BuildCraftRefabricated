package buildcraft.core.misc;

public class BooleanWrapper {
   private boolean value;

   public BooleanWrapper(boolean defaultValue) {
      this.value = defaultValue;
   }

   public boolean evaluate() {
      return this.value;
   }

   public void set(boolean newValue) {
      this.value = newValue;
   }
}
