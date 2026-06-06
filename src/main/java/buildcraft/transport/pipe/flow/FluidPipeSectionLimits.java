package buildcraft.transport.pipe.flow;

public final class FluidPipeSectionLimits {
   private FluidPipeSectionLimits() {
   }

   public static int maxFilled(int capacity, int amount, int transferPerTick, int incomingAtCurrentTime) {
      int availableTotal = capacity - amount;
      int availableThisTick = transferPerTick - incomingAtCurrentTime;
      return Math.min(availableTotal, availableThisTick);
   }

   public static int maxDrained(int amount, int incomingTotalCache, int transferPerTick) {
      return Math.min(amount - incomingTotalCache, transferPerTick);
   }
}
