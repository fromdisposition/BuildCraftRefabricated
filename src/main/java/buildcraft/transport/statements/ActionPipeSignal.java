/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.transport.IWireEmitter;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import java.util.Locale;
import net.minecraft.world.item.DyeColor;

public class ActionPipeSignal extends BCStatement implements IActionInternal {
   public final DyeColor colour;

   public ActionPipeSignal(DyeColor colour) {
      super("buildcraft:pipe.wire.output." + colour.name().toLowerCase(Locale.ROOT), "buildcraft.pipe.wire.output." + colour.name().toLowerCase(Locale.ROOT));
      this.colour = colour;
   }

   @Override
   public String getDescription() {
      return String.format(LocaleUtil.localize("gate.action.pipe.wire"), ColourUtil.getTextFullTooltip(this.colour));
   }

   @Override
   public int maxParameters() {
      return 3;
   }

   @Override
   public IStatementParameter createParameter(int index) {
      return ActionParameterSignal.EMPTY;
   }

   @Override
   public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
      if (container instanceof IWireEmitter emitter) {
         emitter.emitWire(this.colour);

         for (IStatementParameter param : parameters) {
            if (param != null && param instanceof ActionParameterSignal signal && signal.getColor() != null) {
               emitter.emitWire(signal.getColor());
            }
         }
      }
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCTransportSprites.getPipeSignal(true, this.colour);
   }

   public ActionPipeSignal[] getPossible() {
      return BCTransportStatements.ACTION_PIPE_SIGNAL;
   }
}
