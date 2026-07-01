/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
