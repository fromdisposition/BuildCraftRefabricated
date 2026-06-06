package buildcraft.transport.pipe.flow;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.lib.misc.VecUtil;
import java.util.EnumMap;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public final class PipeEnergyDisplaySupport {
   private PipeEnergyDisplaySupport() {
   }

   public static PipeEnergyDisplaySupport.ClientAnimationState tickClientAnimation(
      Vec3 centre, Vec3 centreLast, EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections
   ) {
      Vec3 newCentreLast = centre;
      Vec3 newCentre = centre;

      for (Direction face : Direction.values()) {
         PipeEnergyDisplaySupport.DisplaySection section = sections.get(face);
         section.setClientDisplayFlowLast(section.getClientDisplayFlow());
         double diff = section.getDisplayFlow().value * 2.4 * face.getAxisDirection().getStep();
         section.setClientDisplayFlow((section.getClientDisplayFlow() + 16.0 + diff) % 16.0);
         double centreValue = VecUtil.getValue(newCentre, face.getAxis());
         centreValue = (centreValue + 16.0 + diff / 2.0) % 16.0;
         newCentre = VecUtil.replaceValue(newCentre, face.getAxis(), centreValue);
      }

      return new PipeEnergyDisplaySupport.ClientAnimationState(newCentre, newCentreLast);
   }

   public static void writeDisplayState(FriendlyByteBuf buffer, EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections) {
      for (Direction face : Direction.values()) {
         PipeEnergyDisplaySupport.DisplaySection section = sections.get(face);
         buffer.writeInt(section.getDisplayPower());
         buffer.writeEnum(section.getDisplayFlow());
      }
   }

   public static void readDisplayState(FriendlyByteBuf buffer, EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections) {
      for (Direction face : Direction.values()) {
         PipeEnergyDisplaySupport.DisplaySection section = sections.get(face);
         section.setDisplayPower(buffer.readInt());
         section.setDisplayFlow((PipeEnergyEnumFlow)buffer.readEnum(PipeEnergyEnumFlow.class));
      }
   }

   public static void captureDisplaySnapshot(
      EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections, PipeEnergyEnumFlow[] lastFlows, int[] lastDisplayPower
   ) {
      for (Direction face : Direction.values()) {
         PipeEnergyDisplaySupport.DisplaySection section = sections.get(face);
         int index = face.ordinal();
         lastFlows[index] = section.getDisplayFlow();
         lastDisplayPower[index] = section.getDisplayPower();
      }
   }

   public static boolean displayStateChanged(
      EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections, PipeEnergyEnumFlow[] lastFlows, int[] lastDisplayPower
   ) {
      for (Direction face : Direction.values()) {
         PipeEnergyDisplaySupport.DisplaySection section = sections.get(face);
         int index = face.ordinal();
         if (lastFlows[index] != section.getDisplayFlow() || lastDisplayPower[index] != section.getDisplayPower()) {
            return true;
         }
      }

      return false;
   }

   public static void propagateQueriesToNeighbourPipes(
      IPipe pipe, long[] transferQuery, boolean disabled, Class<? extends PipeFlow> flowType, PipeEnergyDisplaySupport.NeighbourPowerRequest request
   ) {
      for (Direction face : Direction.values()) {
         if (!disabled) {
            long query = transferQuery[face.ordinal()];
            if (query > 0L && pipe.isConnected(face)) {
               IPipe neighbour = pipe.getHolder().getNeighbourPipe(face);
               if (neighbour != null && neighbour.getFlow() != null && flowType.isInstance(neighbour.getFlow())) {
                  request.request(neighbour.getFlow(), face.getOpposite(), query);
               }
            }
         }
      }
   }

   public record ClientAnimationState(Vec3 centre, Vec3 centreLast) {
   }

   public interface DisplaySection {
      int getDisplayPower();

      void setDisplayPower(int var1);

      PipeEnergyEnumFlow getDisplayFlow();

      void setDisplayFlow(PipeEnergyEnumFlow var1);

      double getClientDisplayFlow();

      double getClientDisplayFlowLast();

      void setClientDisplayFlow(double var1);

      void setClientDisplayFlowLast(double var1);
   }

   public static final class DisplaySnapshot {
      private final PipeEnergyEnumFlow[] flows = new PipeEnergyEnumFlow[6];
      private final int[] power = new int[6];

      public void capture(EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections) {
         PipeEnergyDisplaySupport.captureDisplaySnapshot(sections, this.flows, this.power);
      }

      public boolean changed(EnumMap<Direction, ? extends PipeEnergyDisplaySupport.DisplaySection> sections) {
         return PipeEnergyDisplaySupport.displayStateChanged(sections, this.flows, this.power);
      }
   }

   @FunctionalInterface
   public interface NeighbourPowerRequest {
      void request(PipeFlow var1, Direction var2, long var3);
   }
}
