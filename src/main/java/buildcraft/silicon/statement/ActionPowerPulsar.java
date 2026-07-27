/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.statement;

import buildcraft.api.gates.IGate;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.IActionSingle;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.silicon.BCSiliconSprites;
import buildcraft.silicon.BCSiliconStatements;
import buildcraft.silicon.plug.PluggablePulsar;
import net.minecraft.core.Direction;

public class ActionPowerPulsar extends BCStatement implements IActionInternalSided, IActionSingle {
   public final boolean constant;

   public ActionPowerPulsar(boolean constant) {
      // Second tag is the legacy (pre-colon) alias; it must be "buildcraft.pulsar.<kind>" -- a stray "constant"
      // baked into the base string registered garbage aliases and legacy saves failed to resolve the action.
      super("buildcraft:pulsar." + (constant ? "constant" : "single"), "buildcraft.pulsar." + (constant ? "constant" : "single"));
      this.constant = constant;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize(this.constant ? "gate.action.pulsar.constant" : "gate.action.pulsar.single");
   }

   @Override
   public void actionActivate(Direction side, IStatementContainer source, IStatementParameter[] parameters) {
      if (source instanceof IGate gate) {
         IPipeHolder pipe = gate.getPipeHolder();
         if (pipe.getPluggable(side) instanceof PluggablePulsar pulsar) {
            if (this.constant) {
               pulsar.enablePulsar();
            } else {
               pulsar.addSinglePulse();
            }
         }
      }
   }

   @Override
   public boolean singleActionTick() {
      return !this.constant;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return this.constant ? BCSiliconSprites.ACTION_PULSAR_CONSTANT : BCSiliconSprites.ACTION_PULSAR_SINGLE;
   }

   @Override
   public IStatement[] getPossible() {
      return BCSiliconStatements.ACTION_PULSAR;
   }
}
