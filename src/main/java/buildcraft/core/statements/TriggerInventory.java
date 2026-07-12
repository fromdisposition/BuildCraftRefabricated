/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.fabric.transfer.TriggerItemChecks;
import buildcraft.lib.fabric.transfer.BcTransfers;
import buildcraft.lib.misc.LocaleUtil;
import java.util.Locale;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TriggerInventory extends BCStatement implements ITriggerExternal {
   public TriggerInventory.State state;

   public TriggerInventory(TriggerInventory.State state) {
      super("buildcraft:inventory." + state.name().toLowerCase(Locale.ROOT), "buildcraft.inventory." + state.name().toLowerCase(Locale.ROOT));
      this.state = state;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCCoreSprites.TRIGGER_INVENTORY.get(this.state);
   }

   @Override
   public int maxParameters() {
      return this.state != TriggerInventory.State.CONTAINS && this.state != TriggerInventory.State.SPACE ? 0 : 1;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.trigger.inventory." + this.state.name().toLowerCase(Locale.ROOT));
   }

   @Override
   public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer container, IStatementParameter[] parameters) {
      if (tile.getLevel() == null) {
         return false;
      }

      Storage<ItemVariant> storage = BcTransfers.item(tile.getLevel(), tile.getBlockPos(), side != null ? side.getOpposite() : null);
      if (storage == null) {
         return false;
      }

      ItemStack searchedStack = ItemStack.EMPTY;
      if (parameters != null && parameters.length >= 1 && parameters[0] != null) {
         searchedStack = parameters[0].getItemStack();
      }

      TriggerItemChecks.InventoryScan scan = TriggerItemChecks.scan(storage, searchedStack);
      if (!scan.hasSlots) {
         return false;
      }

      return switch (this.state) {
         case EMPTY -> !scan.foundItems;
         case CONTAINS -> scan.foundItems;
         case SPACE -> scan.foundSpace;
         default -> !scan.foundSpace;
      };
   }

   @Override
   public IStatementParameter createParameter(int index) {
      return new StatementParameterItemStack();
   }

   @Override
   public IStatement[] getPossible() {
      return BCCoreStatements.TRIGGER_INVENTORY_ALL;
   }

   public enum State {
      EMPTY,
      CONTAINS,
      SPACE,
      FULL;

      public static final TriggerInventory.State[] VALUES = values();
   }
}
