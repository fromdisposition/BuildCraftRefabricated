package buildcraft.lib.fluid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import buildcraft.lib.fabric.transfer.FluidVariants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import org.junit.jupiter.api.Test;

class FluidVariantsTest {
   @Test
   void fabricBucketMatchesBcMillibucketRatio() {
      assertEquals(81L, FluidVariants.DROPLETS_PER_MB);
      assertEquals(FluidConstants.BUCKET / 1000L, FluidVariants.DROPLETS_PER_MB);
      assertEquals(FluidConstants.BUCKET, FluidVariants.mbToDroplets(1000L));
   }

   @Test
   void dropletConversionIsReversibleForWholeMillibuckets() {
      long droplets = FluidVariants.mbToDroplets(500);
      assertEquals(500L, FluidVariants.dropletsToMb(droplets));
   }
}
