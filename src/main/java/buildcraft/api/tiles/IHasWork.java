/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.tiles;

public interface IHasWork {
   /**
    * Whether this machine is actively working right now; drives gates and the {@code TriggerMachine} triggers.
    *
    * <p>Must reflect real activity: return {@code false} if out of power, paused, or idle, even with a queued
    * job. Distinct from {@code Pipe.hasSimulationWork()}, which only gates pipe ticking.
    */
   boolean hasWork();
}
