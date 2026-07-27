/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.robots;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.EnumPipePart;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ResourceIdRequest extends ResourceIdBlock {
   private int slot;

   public ResourceIdRequest() {
   }

   public ResourceIdRequest(DockingStation station, int slot) {
      this.pos = station.index();
      this.side = EnumPipePart.fromFacing(station.side());
      this.slot = slot;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (!super.equals(obj)) {
         return false;
      }

      ResourceIdRequest compareId = (ResourceIdRequest)obj;
      return this.slot == compareId.slot;
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder().append(super.hashCode()).append(this.slot).build();
   }

   @Override
   public void writeToNBT(CompoundTag nbt) {
      super.writeToNBT(nbt);
      nbt.putInt("localId", this.slot);
   }

   @Override
   protected void readFromNBT(CompoundTag nbt) {
      super.readFromNBT(nbt);
      this.slot = BcNbt.getInt(nbt, "localId", 0);
   }
}
