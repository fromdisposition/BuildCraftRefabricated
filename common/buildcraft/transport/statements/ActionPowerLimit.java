package buildcraft.transport.statements;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.misc.LocaleUtil;

import buildcraft.core.statements.BCStatement;
import buildcraft.transport.BCTransportPipes;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.pipe.behaviour.PipeBehaviourLimiter;

public abstract class ActionPowerLimit extends BCStatement implements IActionInternal {

    public final PipeDefinition pipe;

    /** Behaves identically to {@link PipeBehaviourLimiter} */
    public final int limitShift;

    public ActionPowerLimit(PipeDefinition pipe, int limitShift, String... uniqueTags) {
        super(uniqueTags);
        this.pipe = pipe;
        this.limitShift = limitShift;
    }

    public ActionPowerLimit(String suffix, PipeDefinition pipe, int limitShift) {
        this(pipe, limitShift, "buildcraft:pipe.power_limit." + suffix + "_s" + limitShift);
    }

    @Override
    public String getDescription() {
        PowerTransferInfo pipeInfo = PipeApi.powerTransferData.get(pipe);
        final Object max;
        if (limitShift == PipeBehaviourLimiter.MAX_SHIFT) {
            max = 0;
        } else if (pipeInfo == null) {
            max = "??[INVALID_PIPE]??";
        } else {
            max = (int) ((pipeInfo.transferPerTick >> limitShift) / MjAPI.MJ);
        }
        return LocaleUtil.localize("gate.action.pipe.power_limit", max);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        return BCTransportSprites.POWER_LIMIT[limitShift];
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
        // The behaviour handles this
    }

    @Override
    public abstract IStatement[] getPossible();

    public static class ActionIronPowerLimit extends ActionPowerLimit {

        public ActionIronPowerLimit(int limitShift) {
            super("iron", BCTransportPipes.ironPower, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_IRON_POWER_LIMIT;
        }
    }

    public static class ActionDiamondPowerLimit extends ActionPowerLimit {

        public ActionDiamondPowerLimit(int limitShift) {
            super("diamond", BCTransportPipes.diamondPower, limitShift);
        }

        @Override
        public IStatement[] getPossible() {
            return BCTransportStatements.ACTION_DIAMOND_POWER_LIMIT;
        }
    }
}
