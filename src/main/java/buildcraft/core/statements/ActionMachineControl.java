/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.tiles.IControllable;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.BCCoreStatements;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import java.util.Locale;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ActionMachineControl extends BCStatement implements IActionExternal {
   public final IControllable.Mode mode;

   public ActionMachineControl(IControllable.Mode mode) {
      super("buildcraft:machine." + mode.name().toLowerCase(Locale.ROOT), "buildcraft.machine." + mode.name().toLowerCase(Locale.ROOT));
      this.mode = mode;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("gate.action.machine." + this.mode.name().toLowerCase(Locale.ROOT));
   }

   @Override
   public void actionActivate(BlockEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
      if (target instanceof IControllable controllable && controllable.acceptsControlMode(this.mode)) {
         controllable.setControlMode(this.mode);
      }
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCCoreSprites.ACTION_MACHINE_CONTROL.get(this.mode);
   }

   @Override
   public IStatement[] getPossible() {
      return BCCoreStatements.ACTION_MACHINE_CONTROL;
   }
}
