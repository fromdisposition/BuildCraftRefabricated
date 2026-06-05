/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
