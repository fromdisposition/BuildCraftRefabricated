/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.filler;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.builders.BCBuildersStatements;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.StatementType;
import net.minecraft.nbt.CompoundTag;

public class FillerType extends StatementType<IFillerPattern> {
   public static final FillerType INSTANCE = new FillerType();

   private FillerType() {
      super(IFillerPattern.class, BCBuildersStatements.PATTERN_NONE);
   }

   public IFillerPattern convertToType(Object value) {
      return value instanceof IFillerPattern ? (IFillerPattern)value : null;
   }

   public IFillerPattern readFromNbt(CompoundTag nbt) {
      String kind = nbt.getString("kind").orElse("");
      IFillerPattern pattern = FillerManager.registry.getPattern(kind);
      return pattern == null ? this.defaultStatement : pattern;
   }

   public CompoundTag writeToNbt(IFillerPattern slot) {
      CompoundTag nbt = new CompoundTag();
      nbt.putString("kind", slot.getUniqueTag());
      return nbt;
   }

   public IFillerPattern readFromBuffer(FriendlyByteBuf buffer) {
      String kind = buffer.readUtf();
      IFillerPattern pattern = FillerManager.registry.getPattern(kind);
      return pattern == null ? this.defaultStatement : pattern;
   }

   public void writeToBuffer(FriendlyByteBuf buffer, IFillerPattern slot) {
      buffer.writeUtf(slot.getUniqueTag());
   }
}
