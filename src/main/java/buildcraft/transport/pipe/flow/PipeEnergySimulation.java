package buildcraft.transport.pipe.flow;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.core.BCCoreConfig;
import buildcraft.lib.misc.data.AverageInt;
import java.math.BigInteger;
import java.util.EnumMap;
import net.minecraft.core.Direction;

public final class PipeEnergySimulation {
   private PipeEnergySimulation() {
   }

   public static SafeTimeTracker createNetworkTracker() {
      return new SafeTimeTracker(BCCoreConfig.networkUpdateRate.get(), 4L);
   }

   public static void stepOnce(long now, long currentWorldTime, Runnable onNewTick) {
      if (currentWorldTime != now) {
         onNewTick.run();
      }
   }

   public static void distributeInternalPower(EnumMap<Direction, ? extends PipeEnergySimulation.SimSection> sections, PipeEnergySimulation.PowerSink sink) {
      for (Direction face : Direction.values()) {
         PipeEnergySimulation.SimSection source = sections.get(face);
         if (source.getInternalPower() > 0L) {
            long totalPowerQuery = 0L;

            for (Direction face2 : Direction.values()) {
               if (face != face2) {
                  totalPowerQuery += sections.get(face2).getPowerQuery();
               }
            }

            boolean returnPower = false;
            if (totalPowerQuery <= 0L && source.getPowerQuery() > 0L) {
               totalPowerQuery = source.getPowerQuery();
               returnPower = true;
            }

            if (totalPowerQuery > 0L) {
               long unusedPowerQuery = totalPowerQuery;

               for (Direction face2 : Direction.values()) {
                  if (face != face2 || returnPower) {
                     PipeEnergySimulation.SimSection target = sections.get(face2);
                     long targetQuery = target.getPowerQuery();
                     if (targetQuery > 0L) {
                        long available = source.getInternalPower();
                        long watts = Math.min(
                           BigInteger.valueOf(available).multiply(BigInteger.valueOf(targetQuery)).divide(BigInteger.valueOf(unusedPowerQuery)).longValue(),
                           available
                        );
                        unusedPowerQuery -= targetQuery;
                        long leftover = sink.receive(face2, watts);
                        long used = watts - leftover;
                        source.subtractInternalPower(used);
                        target.addDebugOutput(used);
                        source.pushPowerAverage((int)used);
                        target.pushPowerAverage((int)used);
                        source.setDisplayFlow(PipeEnergyEnumFlow.OUT);
                        target.setDisplayFlow(PipeEnergyEnumFlow.IN);
                     }
                  }
               }
            }
         }
      }
   }

   public static void updateDisplayPower(EnumMap<Direction, ? extends PipeEnergySimulation.SimSectionWithAverage> sections, long maxPower) {
      for (PipeEnergySimulation.SimSectionWithAverage section : sections.values()) {
         AverageInt average = section.getPowerAverage();
         average.tick();
         double value = average.getAverage() / maxPower;
         value = Math.sqrt(value);
         section.setDisplayPower((int)(value * MjAPI.MJ));
      }
   }

   public static void requestFromConnectedTiles(IPipe pipe, PipeEnergySimulation.TilePowerProbe probe, PipeEnergySimulation.PowerRequestConsumer consumer) {
      for (Direction face : Direction.values()) {
         if (pipe.getConnectedType(face) == IPipe.ConnectedType.TILE) {
            long requested = probe.probeRequestedPower(face);
            if (requested > 0L) {
               consumer.request(face, requested);
            }
         }
      }
   }

   public static long[] buildTransferQuery(IPipe pipe, EnumMap<Direction, ? extends PipeEnergySimulation.SimSection> sections) {
      long[] transferQuery = new long[6];

      for (Direction face : Direction.values()) {
         if (pipe.isConnected(face)) {
            long query = 0L;

            for (Direction face2 : Direction.values()) {
               if (face != face2) {
                  query += sections.get(face2).getPowerQuery();
               }
            }

            transferQuery[face.ordinal()] = query;
         }
      }

      return transferQuery;
   }

   @FunctionalInterface
   public interface PowerRequestConsumer {
      void request(Direction var1, long var2);
   }

   @FunctionalInterface
   public interface PowerSink {
      long receive(Direction var1, long var2);
   }

   public interface SimSection extends PipeEnergyDisplaySupport.DisplaySection {
      long getInternalPower();

      void subtractInternalPower(long var1);

      long getPowerQuery();

      void pushPowerAverage(int var1);

      void addDebugOutput(long var1);
   }

   public interface SimSectionWithAverage extends PipeEnergySimulation.SimSection {
      AverageInt getPowerAverage();
   }

   @FunctionalInterface
   public interface TilePowerProbe {
      long probeRequestedPower(Direction var1);
   }
}
