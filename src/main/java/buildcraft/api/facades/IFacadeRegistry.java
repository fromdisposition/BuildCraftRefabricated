package buildcraft.api.facades;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface IFacadeRegistry {
   Collection<? extends IFacadeState> getValidFacades();

   IFacadePhasedState createPhasedState(IFacadeState var1, @Nullable DyeColor var2);

   IFacade createPhasedFacade(IFacadePhasedState[] var1, boolean var2);

   default IFacade createBasicFacade(IFacadeState state, boolean isHollow) {
      return this.createPhasedFacade(new IFacadePhasedState[]{this.createPhasedState(state, null)}, isHollow);
   }

   default void disableBlock(Block block, String source) {
   }

   default void mapStateToStack(BlockState state, ItemStack stack) {
   }
}
