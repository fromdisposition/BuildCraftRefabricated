/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.gate;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITrigger;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.lib.nbt.BcNbt;
import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.ActionWrapper;
import buildcraft.lib.statement.StatementType;
import buildcraft.lib.statement.StatementWrapper;
import buildcraft.lib.statement.TriggerWrapper;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public final class StatementWrapperType<W extends StatementWrapper> extends StatementType<W> {
   public static final StatementWrapperType<TriggerWrapper> TRIGGER = new StatementWrapperType<>(
      TriggerWrapper.class,
      "trigger",
      ITrigger.class,
      value -> value instanceof ITriggerInternal internal ? new TriggerWrapper.TriggerWrapperInternal(internal) : null,
      TriggerWrapper::wrap
   );
   public static final StatementWrapperType<ActionWrapper> ACTION = new StatementWrapperType<>(
      ActionWrapper.class,
      "action",
      IAction.class,
      value -> value instanceof IActionInternal internal ? new ActionWrapper.ActionWrapperInternal(internal) : null,
      ActionWrapper::wrap
   );
   private final String kind;
   private final Class<? extends IStatement> statementClass;
   private final Function<Object, W> converter;
   private final BiFunction<IStatement, Direction, W> wrapper;

   private StatementWrapperType(
      Class<W> clazz,
      String kind,
      Class<? extends IStatement> statementClass,
      Function<Object, W> converter,
      BiFunction<IStatement, Direction, W> wrapper
   ) {
      super(clazz, null);
      this.kind = kind;
      this.statementClass = statementClass;
      this.converter = converter;
      this.wrapper = wrapper;
   }

   @Override
   public W convertToType(Object value) {
      return this.converter.apply(value);
   }

   private W wrap(String name, Direction side, String warning) {
      IStatement statement = StatementManager.statements.get(name);
      if (this.statementClass.isInstance(statement)) {
         return this.wrapper.apply(statement, side);
      }

      BCLog.logger.warn("[gate." + this.kind + "] " + warning, name, statement);
      return null;
   }

   @Override
   public W readFromNbt(CompoundTag nbt) {
      if (nbt == null) {
         return null;
      }

      String name = BcNbt.getString(nbt, "kind", "");
      if (name.isEmpty()) {
         return null;
      }

      EnumPipePart side = EnumPipePart.fromMeta(BcNbt.getByte(nbt, "side", (byte)5));
      return this.wrap(name, side.face, "Couldn't find a " + this.kind + " called '{}'! (found {})");
   }

   @Override
   public CompoundTag writeToNbt(W slot) {
      CompoundTag nbt = new CompoundTag();
      if (slot == null) {
         return nbt;
      }

      nbt.putString("kind", slot.getUniqueTag());
      nbt.putByte("side", (byte)slot.sourcePart.getIndex());
      return nbt;
   }

   @Override
   public W readFromBuffer(FriendlyByteBuf buffer) {
      PacketBufferBC bc = BcPayloadBuffers.ensure(buffer);
      if (!bc.readBoolean()) {
         return null;
      }

      String name = bc.readUtf();
      EnumPipePart part = bc.readEnumValue(EnumPipePart.class);
      return this.wrap(name, part.face, "Unknown " + this.kind + " '{}'");
   }

   @Override
   public void writeToBuffer(FriendlyByteBuf buffer, W slot) {
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
