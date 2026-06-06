package buildcraft.api.mj;

import team.reborn.energy.api.EnergyStorage;

final class OfBoth extends MjToRfAutoConvertor implements IMjReceiver, IMjPassiveProvider {
   OfBoth(EnergyStorage storage) {
      super(storage);
   }

   @Override
   public boolean canReceive() {
      return true;
   }

   @Override
   public long getPowerRequested() {
      return this.implGetPowerRequested();
   }

   @Override
   public long receivePower(long microJoules, boolean simulate) {
      return this.implReceivePower(microJoules, simulate);
   }

   @Override
   public long extractPower(long min, long max, boolean simulate) {
      return this.implExtractPower(min, max, simulate);
   }
}
