/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.tiles;

public interface IHasWork {
   /**
    * Reports whether this machine is actively working right now, for gates and the
    * {@code TriggerMachine} "machine working"/"machine inactive" triggers.
    *
    * <p>This must reflect real activity: a machine that is out of power, paused, or has nothing left
    * to process should return {@code false} even if it still has a queued job. Returning {@code true}
    * while idle makes gates report a machine as working when it is not. This is distinct from
    * {@code Pipe.hasSimulationWork()}, which only gates pipe ticking and is unrelated to this trigger.
    */
   boolean hasWork();
}
