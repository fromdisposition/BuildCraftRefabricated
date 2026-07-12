/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.robot;

import buildcraft.api.mj.MjRfConversion;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

/**
 * RF/E view of a {@link DockingStationPipe}'s power buffer, letting an RF pipe charge the station.
 *
 * <p>Kept in its own class on purpose: the Team Reborn Energy API is an <em>optional</em> interop dependency (only
 * RF pipes need it), so {@link DockingStationPipe} must never mention {@link EnergyStorage} in its own signature --
 * otherwise the whole class fails to link when the API is absent, and since a saved robot deserialises its docking
 * stations on world load, that would crash the world just for having a robot placed (with no energy mod installed).
 * This class is only ever loaded from {@link DockingStationPipe#getEnergyStorage()}, which is in turn only reached
 * while the energy mod is present (the station's RF capability is registered behind an {@code isModLoaded} guard).
 */
final class DockingStationRfStorage implements EnergyStorage {
   private final DockingStationPipe station;

   DockingStationRfStorage(DockingStationPipe station) {
      this.station = station;
   }

   @Override
   public long insert(long maxAmount, TransactionContext transaction) {
      long mjPerRf = MjRfConversion.DEFAULT_MJ_PER_RF;
      long roomRf = this.station.powerRoom() / mjPerRf;
      long accepted = Math.max(0L, Math.min(roomRf, maxAmount));
      if (accepted > 0L) {
         this.station.rfInsert(transaction, accepted * mjPerRf);
      }

      return accepted;
   }

   @Override
   public long extract(long maxAmount, TransactionContext transaction) {
      return 0L;
   }

   @Override
   public long getAmount() {
      return this.station.getPowerBuffer() / MjRfConversion.DEFAULT_MJ_PER_RF;
   }

   @Override
   public long getCapacity() {
      return DockingStationPipe.POWER_BUFFER_CAP / MjRfConversion.DEFAULT_MJ_PER_RF;
   }

   @Override
   public boolean supportsInsertion() {
      return true;
   }

   @Override
   public boolean supportsExtraction() {
      return false;
   }
}
