package buildcraft.transport.pipe.flow;

public enum PipeEnergyEnumFlow {
   IN(-1),
   OUT(1),
   STATIONARY(0);

   public final int value;

   PipeEnergyEnumFlow(int value) {
      this.value = value;
   }
}
