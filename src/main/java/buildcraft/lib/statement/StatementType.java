/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.statement;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.statements.IGuiSlot;
import buildcraft.lib.net.PacketBufferBC;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;

public abstract class StatementType<S extends IGuiSlot> {
   public final Class<S> clazz;
   public final S defaultStatement;

   public StatementType(Class<S> clazz, S defaultStatement) {
      this.clazz = clazz;
      this.defaultStatement = defaultStatement;
   }

   public abstract S readFromNbt(CompoundTag var1);

   public abstract CompoundTag writeToNbt(S var1);

   public abstract S readFromBuffer(FriendlyByteBuf var1);

   public abstract void writeToBuffer(FriendlyByteBuf var1, S var2);

   @Nullable
   public abstract S convertToType(Object var1);
}
