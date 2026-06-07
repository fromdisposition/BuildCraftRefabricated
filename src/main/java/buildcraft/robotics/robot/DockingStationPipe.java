package buildcraft.robotics.robot;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IFlowPower;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.silicon.gate.GateLogic;
import buildcraft.silicon.plug.PluggableGate;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DockingStationPipe extends DockingStation implements IRequestProvider {
   private IPipeHolder pipe;

   public DockingStationPipe() {
   }

   public DockingStationPipe(IPipeHolder pipe, Direction side) {
      super(pipe.getPipePos(), side);
      this.pipe = pipe;
      this.world = pipe.getPipeWorld();
   }

   public IPipeHolder getPipe() {
      if (this.pipe == null && this.world != null) {
         BlockEntity tile = this.world.getBlockEntity(this.getPos());
         if (tile instanceof IPipeHolder holder) {
            this.pipe = holder;
         }
      }

      if (this.pipe == null || ((BlockEntity) this.pipe).isRemoved()) {
         if (this.world != null && !this.world.isClientSide()) {
            RobotManager.registryProvider.getRegistry(this.world).removeStation(this);
         }

         this.pipe = null;
      }

      return this.pipe;
   }

   @Override
   public Iterable<StatementSlot> getActiveActions() {
      List<StatementSlot> actions = new ArrayList<>();
      IPipeHolder holder = this.getPipe();
      if (holder != null) {
         for (Direction face : Direction.values()) {
            PipePluggable plug = holder.getPluggable(face);
            if (plug instanceof PluggableGate gate) {
               GateLogic logic = gate.logic;
               if (logic != null) {
                  actions.addAll(logic.getActiveActions());
               }
            }
         }
      }

      return actions;
   }

   @Override
   public IInjectable getItemOutput() {
      IPipeHolder holder = this.getPipe();
      if (holder == null) {
         return null;
      }

      IPipe ipipe = holder.getPipe();
      return ipipe != null && ipipe.getFlow() instanceof IFlowItems flow ? flow : null;
   }

   @Override
   public EnumPipePart getItemOutputSide() {
      return EnumPipePart.fromFacing(this.side().getOpposite());
   }

   @Override
   public Container getItemInput() {
      IPipeHolder holder = this.getPipe();
      if (holder == null) {
         return null;
      }

      BlockEntity neighbour = holder.getNeighbourTile(this.side());
      return neighbour instanceof Container container ? container : null;
   }

   @Override
   public EnumPipePart getItemInputSide() {
      return EnumPipePart.fromFacing(this.side().getOpposite());
   }

   @Override
   public boolean providesPower() {
      IPipeHolder holder = this.getPipe();
      if (holder == null) {
         return false;
      }

      IPipe ipipe = holder.getPipe();
      return ipipe != null && ipipe.getFlow() instanceof IFlowPower;
   }

   @Override
   public IRequestProvider getRequestProvider() {
      IPipeHolder holder = this.getPipe();
      if (holder != null) {
         for (Direction dir : Direction.values()) {
            BlockEntity nearby = holder.getNeighbourTile(dir);
            if (nearby instanceof IRequestProvider provider) {
               return provider;
            }
         }
      }

      return this;
   }

   @Override
   public boolean take(EntityRobotBase robot) {
      if (this.getPipe() == null) {
         return false;
      }

      boolean result = super.take(robot);
      if (result) {
         this.pipe.scheduleRenderUpdate();
      }

      return result;
   }

   @Override
   public boolean takeAsMain(EntityRobotBase robot) {
      if (this.getPipe() == null) {
         return false;
      }

      boolean result = super.takeAsMain(robot);
      if (result) {
         this.pipe.scheduleRenderUpdate();
      }

      return result;
   }

   @Override
   public void unsafeRelease(EntityRobotBase robot) {
      super.unsafeRelease(robot);
      if (this.robotTaking() == null && this.getPipe() != null) {
         this.pipe.scheduleRenderUpdate();
      }
   }

   @Override
   public void onChunkUnload() {
      this.pipe = null;
   }

   @Override
   public int getRequestsCount() {
      return 0;
   }

   @Override
   public ItemStack getRequest(int slot) {
      return ItemStack.EMPTY;
   }

   @Override
   public ItemStack offerItem(int slot, ItemStack stack) {
      IInjectable output = this.getItemOutput();
      if (output == null) {
         return stack;
      }

      Direction from = this.side().getOpposite();
      ItemStack remaining = output.injectItem(stack.copy(), true, from, null, 0.0);
      return remaining;
   }
}
