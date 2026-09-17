/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport;

import buildcraft.api.statements.StatementManager;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;
import buildcraft.transport.statements.ActionExtractionPreset;
import buildcraft.transport.statements.ActionParameterSignal;
import buildcraft.transport.statements.ActionPipeColor;
import buildcraft.transport.statements.ActionPipeDirection;
import buildcraft.transport.statements.ActionPipeSignal;
import buildcraft.transport.statements.ActionPowerLimit;
import buildcraft.transport.statements.ActionProviderPipes;
import buildcraft.transport.statements.TriggerFluidsTraversing;
import buildcraft.transport.statements.TriggerItemsTraversing;
import buildcraft.transport.statements.TriggerParameterSignal;
import buildcraft.transport.statements.TriggerPipeEmpty;
import buildcraft.transport.statements.TriggerPipeSignal;
import buildcraft.transport.statements.TriggerPowerRequested;
import buildcraft.transport.statements.TriggerProviderPipes;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

public class BCTransportStatements {
   public static final TriggerPipeSignal[] TRIGGER_PIPE_SIGNAL = new TriggerPipeSignal[2 * ColourUtil.COLOURS.length];
   public static final TriggerItemsTraversing TRIGGER_ITEMS_TRAVERSING;
   public static final TriggerFluidsTraversing TRIGGER_FLUIDS_TRAVERSING;
   public static final TriggerPipeEmpty TRIGGER_PIPE_EMPTY;
   public static final TriggerPowerRequested TRIGGER_POWER_REQUESTED;
   public static final ActionPipeSignal[] ACTION_PIPE_SIGNAL;
   public static final ActionPipeColor[] ACTION_PIPE_COLOUR;
   public static final ActionExtractionPreset[] ACTION_EXTRACTION_PRESET;
   public static final ActionPipeDirection[] ACTION_PIPE_DIRECTION;
   public static final ActionPowerLimit[] ACTION_IRON_POWER_LIMIT;
   public static final ActionPowerLimit[] ACTION_DIAMOND_POWER_LIMIT;
   public static final ActionPowerLimit[] ACTION_IRON_RF_LIMIT;
   public static final ActionPowerLimit[] ACTION_DIAMOND_RF_LIMIT;

   public static void preInit() {
      StatementManager.registerActionProvider(ActionProviderPipes.INSTANCE);
      StatementManager.registerTriggerProvider(TriggerProviderPipes.INSTANCE);
   }

   static {
      for (DyeColor colour : ColourUtil.COLOURS) {
         TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 0] = new TriggerPipeSignal(true, colour);
         TRIGGER_PIPE_SIGNAL[colour.ordinal() * 2 + 1] = new TriggerPipeSignal(false, colour);
      }

      ACTION_PIPE_SIGNAL = new ActionPipeSignal[ColourUtil.COLOURS.length];

      for (DyeColor colour : ColourUtil.COLOURS) {
         ACTION_PIPE_SIGNAL[colour.ordinal()] = new ActionPipeSignal(colour);
      }

      ACTION_PIPE_COLOUR = new ActionPipeColor[ColourUtil.COLOURS.length];

      for (DyeColor colour : ColourUtil.COLOURS) {
         ACTION_PIPE_COLOUR[colour.ordinal()] = new ActionPipeColor(colour);
      }

      ACTION_EXTRACTION_PRESET = new ActionExtractionPreset[PipeBehaviourEmzuli.SlotIndex.VALUES.length];

      for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
         ACTION_EXTRACTION_PRESET[index.ordinal()] = new ActionExtractionPreset(index);
      }

      ACTION_PIPE_DIRECTION = new ActionPipeDirection[Direction.values().length];

      for (Direction face : Direction.values()) {
         ACTION_PIPE_DIRECTION[face.ordinal()] = new ActionPipeDirection(face);
      }

      TRIGGER_ITEMS_TRAVERSING = new TriggerItemsTraversing();
      TRIGGER_FLUIDS_TRAVERSING = new TriggerFluidsTraversing();
      TRIGGER_PIPE_EMPTY = new TriggerPipeEmpty();
      TRIGGER_POWER_REQUESTED = new TriggerPowerRequested();
      int numLevels = 7;
      ACTION_IRON_POWER_LIMIT = new ActionPowerLimit[numLevels];
      ACTION_DIAMOND_POWER_LIMIT = new ActionPowerLimit[numLevels];
      ACTION_IRON_RF_LIMIT = new ActionPowerLimit[numLevels];
      ACTION_DIAMOND_RF_LIMIT = new ActionPowerLimit[numLevels];

      for (int i = 0; i < numLevels; i++) {
         int shift = numLevels - 1 - i;
         ACTION_IRON_POWER_LIMIT[i] = new ActionPowerLimit.ActionIronPowerLimit(shift);
         ACTION_DIAMOND_POWER_LIMIT[i] = new ActionPowerLimit.ActionDiamondPowerLimit(shift);
         ACTION_IRON_RF_LIMIT[i] = new ActionPowerLimit.ActionIronRfLimit(shift);
         ACTION_DIAMOND_RF_LIMIT[i] = new ActionPowerLimit.ActionDiamondRfLimit(shift);
      }

      StatementManager.registerParameter(TriggerParameterSignal::readFromNbt, TriggerParameterSignal::readFromBuf);
      StatementManager.registerParameter(ActionParameterSignal::readFromNbt);
   }
}
