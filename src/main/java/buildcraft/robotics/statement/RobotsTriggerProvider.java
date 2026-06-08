/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.statement;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerInternalSided;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.robotics.BCRoboticsStatements;
import buildcraft.robotics.RobotUtils;
import java.util.Collection;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RobotsTriggerProvider implements ITriggerProvider {
   @Override
   public void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer container) {
      IPipeHolder holder = RobotUtils.getPipeHolder(container);
      if (holder != null && !RobotUtils.getStations(holder).isEmpty()) {
         triggers.add(BCRoboticsStatements.TRIGGER_ROBOT_SLEEP);
         triggers.add(BCRoboticsStatements.TRIGGER_ROBOT_IN_STATION);
         triggers.add(BCRoboticsStatements.TRIGGER_ROBOT_LINKED);
         triggers.add(BCRoboticsStatements.TRIGGER_ROBOT_RESERVED);
      }
   }

   @Override
   public void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container, @Nonnull Direction side) {
   }

   @Override
   public void addExternalTriggers(Collection<ITriggerExternal> triggers, @Nonnull Direction side, BlockEntity tile) {
   }
}
