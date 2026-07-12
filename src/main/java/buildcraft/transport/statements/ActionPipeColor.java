/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import net.minecraft.world.item.DyeColor;

public class ActionPipeColor extends BCStatement implements IActionInternal {
   public final DyeColor color;

   public ActionPipeColor(DyeColor color) {
      super("buildcraft:pipe.color." + color.getName(), "buildcraft.pipe." + color.getName());
      this.color = color;
   }

   @Override
   public String getDescription() {
      return String.format(LocaleUtil.localize("gate.action.pipe.item.color"), ColourUtil.getTextFullTooltip(this.color));
   }

   @Override
   public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
   }

   @Override
   public IStatement[] getPossible() {
      return BCTransportStatements.ACTION_PIPE_COLOUR;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCTransportSprites.ACTION_PIPE_COLOUR[this.color.ordinal()];
   }
}
