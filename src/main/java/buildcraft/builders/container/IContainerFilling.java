/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import java.util.stream.IntStream;

import net.minecraft.world.entity.player.Player;

import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.filler.FillerType;

public interface IContainerFilling {
    Player getPlayer();

    FullStatement<IFillerPattern> getPatternStatementClient();

    FullStatement<IFillerPattern> getPatternStatement();

    boolean isInverted();

    void setInverted(boolean value);

    default boolean isLocked() {
        return false;
    }

    void valuesChanged();

    default void onStatementChange() {}
}
