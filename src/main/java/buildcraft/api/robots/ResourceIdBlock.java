package buildcraft.api.robots;

import buildcraft.api.core.EnumPipePart;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ResourceIdBlock extends ResourceId {
   public BlockPos pos = new BlockPos(0, 0, 0);
   public EnumPipePart side = EnumPipePart.CENTER;

   public ResourceIdBlock() {
   }

   public ResourceIdBlock(int x, int y, int z) {
      this.pos = new BlockPos(x, y, z);
   }

   public ResourceIdBlock(BlockPos iIndex) {
      this.pos = iIndex;
   }

   public ResourceIdBlock(BlockEntity tile) {
      this.pos = tile.getBlockPos();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         ResourceIdBlock compareId = (ResourceIdBlock)obj;
         return this.pos.equals(compareId.pos) && this.side == compareId.side;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return new HashCodeBuilder().append(this.pos.hashCode()).append(this.side != null ? this.side.ordinal() : 0).build();
   }

   @Override
   public void writeToNBT(CompoundTag nbt) {
      super.writeToNBT(nbt);
      int[] arr = new int[]{this.pos.getX(), this.pos.getY(), this.pos.getZ()};
      nbt.putIntArray("pos", arr);
      nbt.put("side", this.side.writeToNBT());
   }

   @Override
   protected void readFromNBT(CompoundTag nbt) {
      super.readFromNBT(nbt);
      int[] arr = nbt.getIntArray("pos").orElse(new int[0]);
      this.pos = new BlockPos(arr[0], arr[1], arr[2]);
      this.side = EnumPipePart.readFromNBT(nbt.get("side"));
   }
}
