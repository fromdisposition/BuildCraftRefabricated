package buildcraft.api.mj;

public interface IMjReceiver extends IMjConnector {
   long getPowerRequested();

   long receivePower(long var1, boolean var3);

   default boolean canReceive() {
      return true;
   }
}
