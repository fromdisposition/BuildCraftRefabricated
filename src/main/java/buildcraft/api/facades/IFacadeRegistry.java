package buildcraft.api.facades;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.world.item.DyeColor;

public interface IFacadeRegistry {
   Collection<? extends IFacadeState> getValidFacades();

   IFacadePhasedState createPhasedState(IFacadeState var1, @Nullable DyeColor var2);

   IFacade createPhasedFacade(IFacadePhasedState[] var1, boolean var2);

   default IFacade createBasicFacade(IFacadeState state, boolean isHollow) {
      return this.createPhasedFacade(new IFacadePhasedState[]{this.createPhasedState(state, null)}, isHollow);
   }
}
