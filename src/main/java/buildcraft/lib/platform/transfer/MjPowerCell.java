package buildcraft.lib.platform.transfer;

public interface MjPowerCell {
   long getStored();

   void setStored(long microJoules);

   long getCapacity();

   long addPower(long microJoules, boolean simulate);

   long extractPower(long min, long max);
}
