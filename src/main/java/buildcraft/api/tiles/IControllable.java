package buildcraft.api.tiles;

import java.util.Locale;

public interface IControllable {
   IControllable.Mode getControlMode();

   void setControlMode(IControllable.Mode var1);

   default boolean acceptsControlMode(IControllable.Mode mode) {
      return mode != null;
   }

   enum Mode {
      ON,
      OFF,
      LOOP;

      public static final IControllable.Mode[] VALUES = values();
      public final String lowerCaseName = this.name().toLowerCase(Locale.ROOT);
   }
}
