package buildcraft.builders.tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;

public final class QuarryFrameChecker {
   private QuarryFrameChecker() {
   }

   public static void runChecks(
      boolean firstChecked, Deque<BlockPos> toCheck, Collection<BlockPos> breakPoses, Collection<BlockPos> placePoses, Consumer<BlockPos> check, int limit
   ) {
      int checked = 0;
      if (firstChecked) {
         if (!breakPoses.isEmpty()) {
            for (BlockPos blockPos : new ArrayList<>(breakPoses)) {
               if (checked >= limit) {
                  break;
               }

               check.accept(blockPos);
               checked++;
            }
         }

         if (!placePoses.isEmpty()) {
            for (BlockPos blockPos : new ArrayList<>(placePoses)) {
               if (checked >= limit) {
                  break;
               }

               check.accept(blockPos);
               checked++;
            }
         }
      }

      while (checked < limit && !toCheck.isEmpty()) {
         BlockPos blockPos = toCheck.pollFirst();
         check.accept(blockPos);
         toCheck.addLast(blockPos);
         checked++;
      }
   }
}
