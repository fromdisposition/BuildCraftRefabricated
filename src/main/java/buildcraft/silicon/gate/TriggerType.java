/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITrigger;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.StatementType;
import buildcraft.lib.statement.TriggerWrapper;
import net.minecraft.nbt.CompoundTag;

public class TriggerType extends StatementType<TriggerWrapper> {
   public static final TriggerType INSTANCE = new TriggerType();

   private TriggerType() {
      super(TriggerWrapper.class, null);
   }

   public TriggerWrapper convertToType(Object value) {
      return value instanceof ITriggerInternal ? new TriggerWrapper.TriggerWrapperInternal((ITriggerInternal)value) : null;
   }

   public TriggerWrapper readFromNbt(CompoundTag nbt) {
      if (nbt == null) {
         return null;
      }

      String kind = (String)nbt.getString("kind").orElse(null);
      if (kind != null && !kind.isEmpty()) {
         EnumPipePart side = EnumPipePart.fromMeta(nbt.getByte("side").orElse((byte)5));
         IStatement statement = StatementManager.statements.get(kind);
         if (statement instanceof ITrigger) {
            return TriggerWrapper.wrap(statement, side.face);
         }

         BCLog.logger.warn("[gate.trigger] Couldn't find a trigger called '{}'! (found {})", kind, statement);
         return null;
      } else {
         return null;
      }
   }

   public CompoundTag writeToNbt(TriggerWrapper slot) {
      CompoundTag nbt = new CompoundTag();
      if (slot == null) {
         return nbt;
      }

      nbt.putString("kind", slot.getUniqueTag());
      nbt.putByte("side", (byte)slot.sourcePart.getIndex());
      return nbt;
   }

   public TriggerWrapper readFromBuffer(FriendlyByteBuf buffer) {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buffer);
      if (bc.readBoolean()) {
         String name = bc.readUtf();
         EnumPipePart part = bc.readEnumValue(EnumPipePart.class);
         IStatement statement = StatementManager.statements.get(name);
         if (statement instanceof ITrigger) {
            return TriggerWrapper.wrap(statement, part.face);
         } else {
            BCLog.logger.warn("[gate.trigger] Unknown trigger '{}'", name);
            return null;
         }
      } else {
         return null;
      }
   }

   public void writeToBuffer(FriendlyByteBuf buffer, TriggerWrapper slot) {
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
