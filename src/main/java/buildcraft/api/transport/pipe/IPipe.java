/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IPipe {
   IPipeHolder getHolder();

   PipeDefinition getDefinition();

   PipeBehaviour getBehaviour();

   PipeFlow getFlow();

   DyeColor getColour();

   void setColour(DyeColor var1);

   void markForUpdate();

   BlockEntity getConnectedTile(Direction var1);

   IPipe getConnectedPipe(Direction var1);

   boolean isConnected(Direction var1);

   IPipe.ConnectedType getConnectedType(Direction var1);

   enum ConnectedType {
      TILE,
      PIPE;
   }
}
