package buildcraft.lib.platform.transfer;

import buildcraft.api.mj.MjBattery;

public final class MjBatteryPowerCell implements MjPowerCell {
   private final MjBattery battery;

   public MjBatteryPowerCell(MjBattery battery) {
      this.battery = battery;
   }

   @Override
   public long getStored() {
      return this.battery.getStored();
   }

   @Override
   public void setStored(long microJoules) {
      this.battery.setStored(microJoules);
   }

   @Override
   public long getCapacity() {
      return this.battery.getCapacity();
   }

   @Override
   public long addPower(long microJoules, boolean simulate) {
      return this.battery.addPower(microJoules, simulate);
   }

   @Override
   public long extractPower(long min, long max) {
      return this.battery.extractPower(min, max);
   }
}
