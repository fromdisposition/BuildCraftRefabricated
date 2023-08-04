package buildcraft.transport.pipe.behaviour;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IFlowPower;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventPower;

import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.MathUtil;

import buildcraft.transport.pipe.flow.PipeFlowPower;

public class PipeBehaviourLimiter extends PipeBehaviour {

    private static final int MAX_SHIFT = 5;

    private int limitShift = 0;

    public PipeBehaviourLimiter(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourLimiter(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        limitShift = MathUtil.clamp(nbt.getInteger("limitShift"), 0, MAX_SHIFT);
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setInteger("limitShift", limitShift);
        return nbt;
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        limitShift = buffer.readUnsignedByte();
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        buffer.writeByte(limitShift);
    }

    @PipeEventHandler
    public void configurePower(PipeEventPower.Configure event) {
        event.setMaxPower(event.getMaxPower() >> limitShift);
    }

    @Override
    public boolean onPipeActivate(
        EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part
    ) {
        if (EntityUtil.getWrenchHand(player) == null) {
            return false;
        }

        if (!player.world.isRemote) {
            EntityUtil.activateWrench(player, trace);
            limitShift++;
            if (limitShift > MAX_SHIFT) {
                limitShift = 0;
            }

            if (pipe.getFlow() instanceof PipeFlowPower) {
                PipeFlowPower flow = (PipeFlowPower) pipe.getFlow();
                flow.reconfigure();
                pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            }
        }
        return true;
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return limitShift;
    }
}
