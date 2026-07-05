/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.lib.fabric.transfer.fluid.FluidStorageOps;
import buildcraft.lib.fluid.stack.FluidStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;

public final class FluidPipeMovement {
   private static final int COOLDOWN_OUTPUT = 60;
   private static final int COOLDOWN_INPUT = -60;
   public static final int COOLDOWN_OUTPUT_TICKS = 60;
   public static final int COOLDOWN_INPUT_TICKS = -60;
   private static final ThreadLocal<MoveToCenterScratch> MOVE_TO_CENTER_SCRATCH = ThreadLocal.withInitial(MoveToCenterScratch::new);

   private FluidPipeMovement() {
   }

   private static void shuffleFaces(EnumPipePart[] faces) {
      ThreadLocalRandom rng = ThreadLocalRandom.current();

      for (int i = faces.length - 1; i > 0; i--) {
         int j = rng.nextInt(i + 1);
         EnumPipePart tmp = faces[i];
         faces[i] = faces[j];
         faces[j] = tmp;
      }
   }

   public static void moveFromPipe(FluidPipeMovement.Host host) {
      for (EnumPipePart part : EnumPipePart.FACES) {
         if (host.sectionCanOutput(part)) {
            int maxDrain = host.sectionDrain(part, host.transferPerTick(), false);
            if (maxDrain > 0) {
               PipeEventFluid.SideCheck sideCheck = host.sideCheck(host.currentFluid());
               sideCheck.disallowAllExcept(part.face);
               host.fireEvent(sideCheck);
               if (sideCheck.getOrderedDirections().size() == 1) {
                  Storage<FluidVariant> storage = host.externalFluidStorage(part.face);
                  if (storage != null) {
                     FluidStack resource = host.currentFluid().copyWithAmount(1);
                     int filled = FluidStorageOps.insertFluidMb(storage, resource, maxDrain, true);
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
            PipeEventFluid.SideCheck sideCheck = host.sideCheck(host.currentFluid());
            sideCheck.disallowAllExcept(realDirections);
            host.fireEvent(sideCheck);
            List<Direction> random = new ArrayList<>(sideCheck.getOrderedDirections());
            Collections.shuffle(random, ThreadLocalRandom.current());
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
         MoveToCenterScratch scratch = MOVE_TO_CENTER_SCRATCH.get();
         int flowRate = host.transferPerTick();
         scratch.resetFaces();
         shuffleFaces(scratch.faces);
         Arrays.fill(scratch.inputPerTick, 0);
         Arrays.fill(scratch.fluidLeavingSide, 0);

         for (EnumPipePart part : scratch.faces) {
            if (host.sectionCanInput(part)) {
               scratch.inputPerTick[part.getIndex()] = host.sectionDrain(part, flowRate, false);
               if (scratch.inputPerTick[part.getIndex()] > 0) {
                  transferInCount++;
               }
            }
         }

         System.arraycopy(scratch.inputPerTick, 0, scratch.totalOffered, 0, 6);
         PipeEventFluid.PreMoveToCentre preMove = host.preMoveToCentre(
            host.currentFluid(), Math.min(flowRate, spaceAvailable), scratch.totalOffered, scratch.inputPerTick
         );
         host.fireEvent(preMove);
         int left = Math.min(flowRate, spaceAvailable);
         float min = (float)Math.min(flowRate * transferInCount, spaceAvailable) / flowRate / transferInCount;

         for (EnumPipePart part : EnumPipePart.FACES) {
            int i = part.getIndex();
            if (scratch.inputPerTick[i] > 0) {
               int amountToDrain = (int)(scratch.inputPerTick[i] * min);
               if (amountToDrain < 1) {
                  amountToDrain++;
               }

               if (amountToDrain > left) {
                  amountToDrain = left;
               }

               int amountToPush = host.sectionDrain(part, amountToDrain, false);
               if (amountToPush > 0) {
                  scratch.fluidLeavingSide[i] = amountToPush;
                  left -= amountToPush;
               }
            }
         }

         System.arraycopy(scratch.fluidLeavingSide, 0, scratch.fluidEnteringCentre, 0, 6);
         PipeEventFluid.OnMoveToCentre move = host.onMoveToCentre(host.currentFluid(), scratch.fluidLeavingSide, scratch.fluidEnteringCentre);
         host.fireEvent(move);

         for (EnumPipePart part : EnumPipePart.FACES) {
            int i = part.getIndex();
            int leaving = scratch.fluidLeavingSide[i];
            if (leaving > 0) {
               int actuallyDrained = host.sectionDrain(part, leaving, true);
               if (actuallyDrained != leaving) {
                  throw new IllegalStateException("Couldn't drain " + leaving + " from " + part + ", only drained " + actuallyDrained);
               }

               if (actuallyDrained > 0) {
                  host.setSectionCooldownInput(part);
               }

               int entering = scratch.fluidEnteringCentre[i];
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

   private static final class MoveToCenterScratch {
      final EnumPipePart[] faces = new EnumPipePart[6];
      final int[] inputPerTick = new int[6];
      final int[] totalOffered = new int[6];
      final int[] fluidLeavingSide = new int[6];
      final int[] fluidEnteringCentre = new int[6];

      void resetFaces() {
         System.arraycopy(EnumPipePart.FACES, 0, this.faces, 0, EnumPipePart.FACES.length);
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

      PipeEventFluid.SideCheck sideCheck(FluidStack fluid);

      PipeEventFluid.PreMoveToCentre preMoveToCentre(FluidStack fluid, int totalAcceptable, int[] totalOffered, int[] actuallyOffered);

      PipeEventFluid.OnMoveToCentre onMoveToCentre(FluidStack fluid, int[] fluidLeavingSide, int[] fluidEnteringCentre);
   }
}
