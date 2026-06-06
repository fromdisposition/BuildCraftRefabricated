package buildcraft.transport.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportPipes;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import java.util.function.Supplier;

public abstract class ActionPowerLimit extends BCStatement implements IActionInternal {
   private final Supplier<PipeDefinition> pipeSupplier;
   public final int limitShift;

   public ActionPowerLimit(Supplier<PipeDefinition> pipeSupplier, int limitShift, String... uniqueTags) {
      super(uniqueTags);
      this.pipeSupplier = pipeSupplier;
      this.limitShift = limitShift;
   }

   public ActionPowerLimit(String suffix, Supplier<PipeDefinition> pipeSupplier, int limitShift) {
      this(pipeSupplier, limitShift, "buildcraft:pipe.power_limit." + suffix + "_s" + limitShift);
   }

   protected PipeDefinition resolvePipe() {
      return this.pipeSupplier.get();
   }

   protected boolean isRf() {
      return false;
   }

   @Override
   public String getDescription() {
      PipeDefinition pipe = this.resolvePipe();
      if (this.isRf()) {
         PipeApi.RedstoneFluxTransferInfo pipeInfo = PipeApi.rfTransferData.get(pipe);
         Object max;
         if (this.limitShift == 6) {
            max = 0;
         } else if (pipeInfo == null) {
            max = "??[INVALID_PIPE]??";
         } else {
            max = pipeInfo.transferPerTick >> this.limitShift;
         }

         return String.format(LocaleUtil.localize("gate.action.pipe.rf_limit"), max);
      } else {
         PipeApi.PowerTransferInfo pipeInfo = PipeApi.powerTransferData.get(pipe);
         Object max;
         if (this.limitShift == 6) {
            max = 0;
         } else if (pipeInfo == null) {
            max = "??[INVALID_PIPE]??";
         } else {
            max = (int)((pipeInfo.transferPerTick >> this.limitShift) / MjAPI.MJ);
         }

         return String.format(LocaleUtil.localize("gate.action.pipe.power_limit"), max);
      }
   }

   @Override
   public ISprite getSprite() {
      SpriteHolderRegistry.SpriteHolder[] sprites = this.isRf() ? BCTransportSprites.POWER_LIMIT_RF : BCTransportSprites.POWER_LIMIT;
      return sprites[this.limitShift];
   }

   @Override
   public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
   }

   @Override
   public abstract IStatement[] getPossible();

   public static class ActionDiamondPowerLimit extends ActionPowerLimit {
      public ActionDiamondPowerLimit(int limitShift) {
         super("diamond", () -> BCTransportPipes.diamondPower, limitShift);
      }

      @Override
      public IStatement[] getPossible() {
         return BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT;
      }
   }

   public static class ActionDiamondRfLimit extends ActionPowerLimit {
      public ActionDiamondRfLimit(int limitShift) {
         super("diamond_rf", () -> BCTransportPipes.diamondRf, limitShift);
      }

      @Override
      public IStatement[] getPossible() {
         return BCTransportStatements.ACTION_DIAMOND_RF_LIMIT;
      }

      @Override
      protected boolean isRf() {
         return true;
      }
   }

   public static class ActionIronPowerLimit extends ActionPowerLimit {
      public ActionIronPowerLimit(int limitShift) {
         super("iron", () -> BCTransportPipes.ironPower, limitShift);
      }

      @Override
      public IStatement[] getPossible() {
         return BCTransportStatements.ACTION_IRON_POWER_LIMIT;
      }
   }

   public static class ActionIronRfLimit extends ActionPowerLimit {
      public ActionIronRfLimit(int limitShift) {
         super("iron_rf", () -> BCTransportPipes.ironRf, limitShift);
      }

      @Override
      public IStatement[] getPossible() {
         return BCTransportStatements.ACTION_IRON_RF_LIMIT;
      }

      @Override
      protected boolean isRf() {
         return true;
      }
   }
}
