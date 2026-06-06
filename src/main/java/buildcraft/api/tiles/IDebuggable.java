package buildcraft.api.tiles;

import java.util.List;
import net.minecraft.core.Direction;

public interface IDebuggable {
   void getDebugInfo(List<String> var1, List<String> var2, Direction var3);

   default void getClientDebugInfo(List<String> left, List<String> right, Direction side) {
   }
}
