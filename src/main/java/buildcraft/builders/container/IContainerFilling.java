package buildcraft.builders.container;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.lib.statement.FullStatement;
import net.minecraft.world.entity.player.Player;

public interface IContainerFilling {
   Player getPlayer();

   FullStatement<IFillerPattern> getPatternStatementClient();

   FullStatement<IFillerPattern> getPatternStatement();

   boolean isInverted();

   void setInverted(boolean var1);

   default boolean isLocked() {
      return false;
   }

   void valuesChanged();

   default void onStatementChange() {
   }
}
