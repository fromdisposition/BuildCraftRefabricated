package buildcraft.core.statements;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.fabric.transfer.TriggerFluidChecks;
import buildcraft.lib.fabric.transfer.TriggerTransferAccess;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.LocaleUtil;
import java.util.Locale;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TriggerFluidContainer extends BCStatement implements ITriggerExternal {
   public TriggerFluidContainer.State state;

   public TriggerFluidContainer(TriggerFluidContainer.State state) {
      super("buildcraft:fluid." + state.name().toLowerCase(Locale.ROOT), "buildcraft.fluid." + state.name().toLowerCase(Locale.ROOT));
      this.state = state;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCCoreSprites.TRIGGER_FLUID.get(this.state);
   }

   @Override
   public int maxParameters() {
      return this.state != TriggerFluidContainer.State.CONTAINS && this.state != TriggerFluidContainer.State.SPACE ? 0 : 1;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.fluid." + this.state.name().toLowerCase(Locale.ROOT));
   }

   @Override
   public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
      if (tile != null && tile.getLevel() != null) {
         Storage<FluidVariant> storage = TriggerTransferAccess.blockFluidStorage(tile.getLevel(), tile.getBlockPos(), side != null ? side.getOpposite() : null);
         FluidStack searchedFluid = FluidStack.EMPTY;
         if (parameters != null && parameters.length >= 1 && parameters[0] != null && !parameters[0].getItemStack().isEmpty()) {
            searchedFluid = TriggerTransferAccess.fluidFromItemParameter(parameters[0].getItemStack());
         }
         return switch (this.state) {
            case EMPTY -> TriggerFluidChecks.isEmpty(storage);
            case CONTAINS -> TriggerFluidChecks.contains(storage, searchedFluid);
            case SPACE -> TriggerFluidChecks.hasSpace(storage, searchedFluid);
            case FULL -> TriggerFluidChecks.isFull(storage, searchedFluid);
         };
      } else {
         return false;
      }
   }

   @Override
   public IStatementParameter createParameter(int index) {
      return new StatementParameterItemStack();
   }

   @Override
   public IStatement[] getPossible() {
      return BCCoreStatements.TRIGGER_FLUID_ALL;
   }

   public enum State {
      EMPTY,
      CONTAINS,
      SPACE,
      FULL;

      public static final TriggerFluidContainer.State[] VALUES = values();
   }
}
