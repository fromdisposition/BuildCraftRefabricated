/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerInternalSided;
import buildcraft.api.statements.ITriggerProvider;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.lib.fabric.transfer.TriggerFluidChecks;
import buildcraft.lib.fabric.transfer.TriggerItemChecks;
import java.util.Collection;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public enum CoreTriggerProvider implements ITriggerProvider {
   INSTANCE;

   @Override
   public void addInternalTriggers(Collection<ITriggerInternal> res, IStatementContainer container) {
      res.add(BCCoreStatements.TRIGGER_TRUE);
      if (container instanceof IRedstoneStatementContainer) {
         res.add(BCCoreStatements.TRIGGER_REDSTONE_ACTIVE);
         res.add(BCCoreStatements.TRIGGER_REDSTONE_INACTIVE);
      }

      if (TriggerPower.isTriggeringTile(container.getTile())) {
         res.add(BCCoreStatements.TRIGGER_POWER_HIGH);
         res.add(BCCoreStatements.TRIGGER_POWER_LOW);
      }
   }

   @Override
   public void addInternalSidedTriggers(Collection<ITriggerInternalSided> res, IStatementContainer container, @Nonnull Direction side) {
   }

   @Override
   public void addExternalTriggers(Collection<ITriggerExternal> res, @Nonnull Direction side, BlockEntity tile) {
      if (!(tile instanceof IPipeHolder)) {
         if (TriggerPower.isTriggeringTile(tile, side.getOpposite())) {
            res.add(BCCoreStatements.TRIGGER_POWER_HIGH);
            res.add(BCCoreStatements.TRIGGER_POWER_LOW);
         }

         boolean blockInventoryTriggers = false;
         boolean blockFluidHandlerTriggers = false;
         if (tile instanceof IBlockDefaultTriggers defaults) {
            blockInventoryTriggers = defaults.blockInventoryTriggers(side);
            blockFluidHandlerTriggers = defaults.blockFluidHandlerTriggers(side);
         }

         if (!blockInventoryTriggers
            && tile != null
            && tile.getLevel() != null
            && TriggerItemChecks.hasViews(BcTransfers.item(tile.getLevel(), tile.getBlockPos(), side.getOpposite()))) {
            res.add(BCCoreStatements.TRIGGER_INVENTORY_EMPTY);
            res.add(BCCoreStatements.TRIGGER_INVENTORY_SPACE);
            res.add(BCCoreStatements.TRIGGER_INVENTORY_CONTAINS);
            res.add(BCCoreStatements.TRIGGER_INVENTORY_FULL);
            res.add(BCCoreStatements.TRIGGER_INVENTORY_BELOW_25);
            res.add(BCCoreStatements.TRIGGER_INVENTORY_BELOW_50);
            res.add(BCCoreStatements.TRIGGER_INVENTORY_BELOW_75);
         }

         if (!blockFluidHandlerTriggers
            && tile != null
            && tile.getLevel() != null
            && TriggerFluidChecks.hasViews(BcTransfers.fluid(tile.getLevel(), tile.getBlockPos(), side.getOpposite()))) {
            res.add(BCCoreStatements.TRIGGER_FLUID_EMPTY);
            res.add(BCCoreStatements.TRIGGER_FLUID_SPACE);
            res.add(BCCoreStatements.TRIGGER_FLUID_CONTAINS);
            res.add(BCCoreStatements.TRIGGER_FLUID_FULL);
            res.add(BCCoreStatements.TRIGGER_FLUID_BELOW_25);
            res.add(BCCoreStatements.TRIGGER_FLUID_BELOW_50);
            res.add(BCCoreStatements.TRIGGER_FLUID_BELOW_75);
         }

         if (tile instanceof IHasWork) {
            res.add(BCCoreStatements.TRIGGER_MACHINE_ACTIVE);
            res.add(BCCoreStatements.TRIGGER_MACHINE_INACTIVE);
         }

         if (TriggerEnginePowerStage.isTriggeringTile(tile)) {
            res.add(BCCoreStatements.TRIGGER_POWER_BLUE);
            res.add(BCCoreStatements.TRIGGER_POWER_GREEN);
            res.add(BCCoreStatements.TRIGGER_POWER_YELLOW);
            res.add(BCCoreStatements.TRIGGER_POWER_RED);
            res.add(BCCoreStatements.TRIGGER_POWER_OVERHEAT);
         }
      }
   }
}
