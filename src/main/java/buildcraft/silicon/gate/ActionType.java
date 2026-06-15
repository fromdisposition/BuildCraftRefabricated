/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementManager;
import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.ActionWrapper;
import buildcraft.lib.statement.StatementType;
import net.minecraft.nbt.CompoundTag;

public class ActionType extends StatementType<ActionWrapper> {
   public static final ActionType INSTANCE = new ActionType();

   private ActionType() {
      super(ActionWrapper.class, null);
   }

   public ActionWrapper convertToType(Object value) {
      return value instanceof IActionInternal ? new ActionWrapper.ActionWrapperInternal((IActionInternal)value) : null;
   }

   public ActionWrapper readFromNbt(CompoundTag nbt) {
      if (nbt == null) {
         return null;
      }

      String kind = nbt.getString("kind").orElse("");
      if (kind != null && !kind.isEmpty()) {
         EnumPipePart side = EnumPipePart.fromMeta(nbt.getByte("side").orElse((byte)5));
         IStatement statement = StatementManager.statements.get(kind);
         if (statement instanceof IAction) {
            return ActionWrapper.wrap(statement, side.face);
         }

         BCLog.logger.warn("[gate.trigger] Couldn't find an action called '{}'! (found {})", kind, statement);
         return null;
      } else {
         return null;
      }
   }

   public CompoundTag writeToNbt(ActionWrapper slot) {
      CompoundTag nbt = new CompoundTag();
      if (slot == null) {
         return nbt;
      }

      nbt.putString("kind", slot.getUniqueTag());
      nbt.putByte("side", (byte)slot.sourcePart.getIndex());
      return nbt;
   }

   public ActionWrapper readFromBuffer(FriendlyByteBuf buffer) {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buffer);
      if (bc.readBoolean()) {
         String name = bc.readUtf();
         EnumPipePart part = bc.readEnumValue(EnumPipePart.class);
         IStatement statement = StatementManager.statements.get(name);
         if (statement instanceof IAction) {
            return ActionWrapper.wrap(statement, part.face);
         } else {
            BCLog.logger.warn("[gate.action] Unknown action '{}'", name);
            return null;
         }
      } else {
         return null;
      }
   }

   public void writeToBuffer(FriendlyByteBuf buffer, ActionWrapper slot) {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buffer);
      if (slot == null) {
         bc.writeBoolean(false);
      } else {
         bc.writeBoolean(true);
         bc.writeUtf(slot.getUniqueTag());
         bc.writeEnumValue(slot.sourcePart);
      }
   }
}
