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

public class TriggerInventoryLevel extends BCStatement implements ITriggerExternal {
   public TriggerInventoryLevel.TriggerType type;

   public TriggerInventoryLevel(TriggerInventoryLevel.TriggerType type) {
      super(
         "buildcraft:inventorylevel." + type.name().toLowerCase(Locale.ROOT),
         "buildcraft.inventorylevel." + type.name().toLowerCase(Locale.ROOT),
         "buildcraft.filteredBuffer." + type.name().toLowerCase(Locale.ROOT)
      );
      this.type = type;
   }

   @Override
   public int maxParameters() {
      return 1;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCCoreSprites.TRIGGER_INVENTORY_LEVEL.get(this.type);
   }

   @Override
   public String getDescription() {
      return String.format(LocaleUtil.localize("gate.trigger.inventorylevel.below"), (int)(this.type.level * 100.0F));
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

      StatementParameterItemStack param = getParam(0, parameters, new StatementParameterItemStack());
      ItemStack searchStack = param.getItemStack();
      return TriggerItemChecks.fillRatio(storage, searchStack) < this.type.level;
   }

   @Override
   public IStatementParameter createParameter(int index) {
      return new StatementParameterItemStack();
   }

   @Override
   public IStatement[] getPossible() {
      return BCCoreStatements.TRIGGER_INVENTORY_ALL;
   }

   public enum TriggerType {
      BELOW25(0.25F),
      BELOW50(0.5F),
      BELOW75(0.75F);

      public static final TriggerInventoryLevel.TriggerType[] VALUES = values();
      public final float level;

      TriggerType(float level) {
         this.level = level;
      }
   }
}
