/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.robot;

import buildcraft.api.mj.MjBattery;

public class GatedMjBattery extends MjBattery {
   private final MjBattery delegate;
   public boolean inRange;

   public GatedMjBattery(MjBattery delegate) {
      super(delegate.getCapacity());
      this.delegate = delegate;
   }

   @Override
   public long getStored() {
      return this.delegate.getStored();
   }

   @Override
   public long getCapacity() {
      return this.delegate.getCapacity();
   }

   @Override
   public boolean isFull() {
      return this.delegate.isFull();
   }

   @Override
   public long addPower(long microJoulesToAdd, boolean simulate) {
      return this.delegate.addPower(microJoulesToAdd, simulate);
   }

   @Override
   public long extractPower(long min, long max) {
      return this.inRange ? this.delegate.extractPower(min, max) : 0L;
   }

   @Override
   public long extractAll() {
      return this.delegate.extractAll();
   }

   @Override
   public void setStored(long microJoules) {
      this.delegate.setStored(microJoules);
   }
}
