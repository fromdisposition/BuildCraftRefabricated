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

public class TriggerFluidContainerLevel extends BCStatement implements ITriggerExternal {
   public final TriggerFluidContainerLevel.TriggerType type;

   public TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType type) {
      super("buildcraft:fluid." + type.name().toLowerCase(Locale.ROOT), "buildcraft.fluid." + type.name().toLowerCase(Locale.ROOT));
      this.type = type;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCCoreSprites.TRIGGER_FLUID_LEVEL.get(this.type);
   }

   @Override
   public int maxParameters() {
      return 1;
   }

   @Override
   public String getDescription() {
      return String.format(LocaleUtil.localize("gate.trigger.fluidlevel.below"), (int)(this.type.level * 100.0F));
   }

   @Override
   public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer statementContainer, IStatementParameter[] parameters) {
      if (tile != null && tile.getLevel() != null) {
         Storage<FluidVariant> storage = TriggerTransferAccess.blockFluidStorage(tile.getLevel(), tile.getBlockPos(), side != null ? side.getOpposite() : null);
         FluidStack searchedFluid = FluidStack.EMPTY;
         if (parameters != null && parameters.length >= 1 && parameters[0] != null && !parameters[0].getItemStack().isEmpty()) {
            searchedFluid = TriggerTransferAccess.fluidFromItemParameter(parameters[0].getItemStack());
         }

         return TriggerFluidChecks.belowLevel(storage, searchedFluid, this.type.level);
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

   public enum TriggerType {
      BELOW25(0.25F),
      BELOW50(0.5F),
      BELOW75(0.75F);

      public static final TriggerFluidContainerLevel.TriggerType[] VALUES = values();
      public final float level;

      TriggerType(float level) {
         this.level = level;
      }
   }
}
