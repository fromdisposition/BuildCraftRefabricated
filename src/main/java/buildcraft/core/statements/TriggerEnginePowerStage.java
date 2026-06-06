package buildcraft.core.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TriggerEnginePowerStage extends BCStatement implements ITriggerExternal {
   public final EnumPowerStage stage;

   public TriggerEnginePowerStage(EnumPowerStage stage) {
      super("buildcraft:engine.stage." + stage.getSerializedName());
      this.stage = stage;
   }

   public static boolean isTriggeringTile(BlockEntity tile) {
      return tile instanceof TileEngineBase_BC8;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.engine." + this.stage.getSerializedName());
   }

   @Override
   public ISprite getSprite() {
      return BCCoreSprites.TRIGGER_POWER_STAGE.get(this.stage);
   }

   @Override
   public boolean isTriggerActive(BlockEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
      return target instanceof TileEngineBase_BC8 engine ? engine.getPowerStage() == this.stage : false;
   }

   @Override
   public IStatement[] getPossible() {
      return BCCoreStatements.TRIGGER_POWER_STAGES;
   }
}
