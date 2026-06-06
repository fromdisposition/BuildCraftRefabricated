package buildcraft.api.robots;

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
      this.slot = nbt.getInt("localId").orElse(0);
   }
}
