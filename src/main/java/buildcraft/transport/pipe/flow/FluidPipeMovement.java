package buildcraft.transport.pipe.flow;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.lib.fluids.FluidStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;

public final class FluidPipeMovement {
   private static final int COOLDOWN_OUTPUT = 60;
   private static final int COOLDOWN_INPUT = -60;
   public static final int COOLDOWN_OUTPUT_TICKS = 60;
   public static final int COOLDOWN_INPUT_TICKS = -60;

   private FluidPipeMovement() {
   }

   public static void moveFromPipe(FluidPipeMovement.Host host) {
      for (EnumPipePart part : EnumPipePart.FACES) {
         if (host.sectionCanOutput(part)) {
            int maxDrain = host.sectionDrain(part, host.transferPerTick(), false);
            if (maxDrain > 0) {
               PipeEventFluid.SideCheck sideCheck = new PipeEventFluid.SideCheck(host.flow().pipe.getHolder(), host.flow(), host.currentFluid());
               sideCheck.disallowAllExcept(part.face);
               host.fireEvent(sideCheck);
               if (sideCheck.getOrder().size() == 1) {
                  Storage<FluidVariant> storage = host.externalFluidStorage(part.face);
                  if (storage != null) {
                     FluidStack resource = host.currentFluid().copyWithAmount(1);
                     int filled = PipeNeighborTransfers.insertFluidMb(storage, resource, maxDrain, true);
                     if (filled > 0) {
                        host.sectionDrain(part, filled, true);
                        host.setSectionCooldownOutput(part);
                     }
                  }
               }
            }
         }
      }
   }

   public static void moveFromCenter(FluidPipeMovement.Host host) {
      int totalAvailable = host.centerMaxDrain();
      if (totalAvailable >= 1) {
         int flowRate = host.transferPerTick();
         Set<Direction> realDirections = EnumSet.noneOf(Direction.class);

         for (Direction direction : Direction.values()) {
            EnumPipePart part = EnumPipePart.fromFacing(direction);
            if (host.sectionCanOutput(part) && host.sectionMaxFill(part) > 0 && host.hasExternalFluidStorage(direction)) {
               realDirections.add(direction);
            }
         }

         if (!realDirections.isEmpty()) {
            PipeEventFluid.SideCheck sideCheck = new PipeEventFluid.SideCheck(host.flow().pipe.getHolder(), host.flow(), host.currentFluid());
            sideCheck.disallowAllExcept(realDirections);
            host.fireEvent(sideCheck);
            List<Direction> random = new ArrayList<>(sideCheck.getOrder());
            Collections.shuffle(random);
            float min = (float)Math.min(flowRate * realDirections.size(), totalAvailable) / flowRate / realDirections.size();

            for (Direction direction : random) {
               EnumPipePart part = EnumPipePart.fromFacing(direction);
               int available = host.sectionFill(part, flowRate, false);
               int amountToPush = (int)(available * min);
               if (amountToPush < 1) {
                  amountToPush++;
               }

               amountToPush = host.sectionDrain(EnumPipePart.CENTER, amountToPush, false);
               if (amountToPush > 0) {
                  int filled = host.sectionFill(part, amountToPush, true);
                  if (filled > 0) {
                     host.sectionDrain(EnumPipePart.CENTER, filled, true);
                     host.setSectionCooldownOutput(part);
                  }
               }
            }
         }
      }
   }

   public static void moveToCenter(FluidPipeMovement.Host host) {
      int transferInCount = 0;
      int spaceAvailable = host.capacity() - host.centerAmount();
      if (spaceAvailable > 0 && host.centerMaxFill() > 0) {
         int flowRate = host.transferPerTick();
         List<EnumPipePart> faces = new ArrayList<>();
         Collections.addAll(faces, EnumPipePart.FACES);
         Collections.shuffle(faces);
         int[] inputPerTick = new int[6];

         for (EnumPipePart part : faces) {
            inputPerTick[part.getIndex()] = 0;
            if (host.sectionCanInput(part)) {
               inputPerTick[part.getIndex()] = host.sectionDrain(part, flowRate, false);
               if (inputPerTick[part.getIndex()] > 0) {
                  transferInCount++;
               }
            }
         }

         int[] totalOffered = Arrays.copyOf(inputPerTick, 6);
         PipeEventFluid.PreMoveToCentre preMove = new PipeEventFluid.PreMoveToCentre(
            host.flow().pipe.getHolder(), host.flow(), host.currentFluid(), Math.min(flowRate, spaceAvailable), totalOffered, inputPerTick
         );
         host.fireEvent(preMove);
         int[] fluidLeavingSide = new int[6];
         int left = Math.min(flowRate, spaceAvailable);
         float min = (float)Math.min(flowRate * transferInCount, spaceAvailable) / flowRate / transferInCount;

         for (EnumPipePart part : EnumPipePart.FACES) {
            int i = part.getIndex();
            if (inputPerTick[i] > 0) {
               int amountToDrain = (int)(inputPerTick[i] * min);
               if (amountToDrain < 1) {
                  amountToDrain++;
               }

               if (amountToDrain > left) {
                  amountToDrain = left;
               }

               int amountToPush = host.sectionDrain(part, amountToDrain, false);
               if (amountToPush > 0) {
                  fluidLeavingSide[i] = amountToPush;
                  left -= amountToPush;
               }
            }
         }

         int[] fluidEnteringCentre = Arrays.copyOf(fluidLeavingSide, 6);
         PipeEventFluid.OnMoveToCentre move = new PipeEventFluid.OnMoveToCentre(
            host.flow().pipe.getHolder(), host.flow(), host.currentFluid(), fluidLeavingSide, fluidEnteringCentre
         );
         host.fireEvent(move);

         for (EnumPipePart part : EnumPipePart.FACES) {
            int i = part.getIndex();
            int leaving = fluidLeavingSide[i];
            if (leaving > 0) {
               int actuallyDrained = host.sectionDrain(part, leaving, true);
               if (actuallyDrained != leaving) {
                  throw new IllegalStateException("Couldn't drain " + leaving + " from " + part + ", only drained " + actuallyDrained);
               }

               if (actuallyDrained > 0) {
                  host.setSectionCooldownInput(part);
               }

               int entering = fluidEnteringCentre[i];
               if (entering > 0) {
                  int actuallyFilled = host.sectionFill(EnumPipePart.CENTER, entering, true);
                  if (actuallyFilled != entering) {
                     throw new IllegalStateException("Couldn't fill " + entering + " from " + part + ", only filled " + actuallyFilled);
                  }
               }
            }
         }
      }
   }

   public interface Host {
      PipeFlowFluids flow();

      FluidStack currentFluid();

      int transferPerTick();

      int capacity();

      boolean sectionCanOutput(EnumPipePart var1);

      boolean sectionCanInput(EnumPipePart var1);

      int sectionDrain(EnumPipePart var1, int var2, boolean var3);

      int sectionFill(EnumPipePart var1, int var2, boolean var3);

      int sectionMaxFill(EnumPipePart var1);

      int centerMaxDrain();

      int centerMaxFill();

      int centerAmount();

      void setSectionCooldownOutput(EnumPipePart var1);

      void setSectionCooldownInput(EnumPipePart var1);

      boolean hasExternalFluidStorage(Direction var1);

      Storage<FluidVariant> externalFluidStorage(Direction var1);

      void fireEvent(PipeEvent var1);
   }
}
