/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.statements.containers.ISidedStatementContainer;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import net.minecraft.core.Direction;

public class ActionRedstoneOutput extends BCStatement implements IActionInternal {
   protected ActionRedstoneOutput(String s) {
      super(s);
   }

   public ActionRedstoneOutput() {
      super("buildcraft:redstone.output", "buildcraft.redstone.output");
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.action.redstone.signal");
   }

   @Override
   public IStatementParameter createParameter(int index) {
      switch (index) {
         case 0:
            return StatementParamGateSideOnly.ANY;
         default:
            return null;
      }
   }

   @Override
   public int maxParameters() {
      return 1;
   }

   protected boolean isSideOnly(IStatementParameter[] parameters) {
      return parameters != null && parameters.length >= this.getRGSOSlot() + 1 && parameters[this.getRGSOSlot()] instanceof StatementParamGateSideOnly
         ? ((StatementParamGateSideOnly)parameters[this.getRGSOSlot()]).isSpecific
         : false;
   }

   @Override
   public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
      if (source instanceof IRedstoneStatementContainer) {
         Direction side = null;
         if (source instanceof ISidedStatementContainer && this.isSideOnly(parameters)) {
            side = ((ISidedStatementContainer)source).getSide();
         }

         ((IRedstoneStatementContainer)source).setRedstoneOutput(side, this.getSignalLevel(parameters));
      }
   }

   protected int getRGSOSlot() {
      return 0;
   }

   protected int getSignalLevel(IStatementParameter[] parameters) {
      return 15;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCCoreSprites.ACTION_REDSTONE;
   }

   @Override
   public <T> T convertTo(Class<T> clazz) {
      T obj = super.convertTo(clazz);
      if (obj != null) {
         return obj;
      } else {
         return clazz.isInstance(BCCoreStatements.TRIGGER_REDSTONE_ACTIVE) ? clazz.cast(BCCoreStatements.TRIGGER_REDSTONE_ACTIVE) : null;
      }
   }
}
