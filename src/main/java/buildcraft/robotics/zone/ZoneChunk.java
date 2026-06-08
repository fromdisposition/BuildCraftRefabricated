package buildcraft.robotics.zone;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class ZoneChunk {
   public BitSet property;
   private boolean fullSet = false;

   public ZoneChunk() {
   }

   public ZoneChunk(ZoneChunk old) {
      if (old.property != null) {
         this.property = BitSet.valueOf(old.property.toLongArray());
      }

      this.fullSet = old.fullSet;
   }

   public boolean get(int xChunk, int zChunk) {
      return this.fullSet || this.property != null && this.property.get(xChunk + zChunk * 16);
   }

   public void set(int xChunk, int zChunk, boolean value) {
      if (value) {
         if (this.fullSet) {
            return;
         }

         if (this.property == null) {
            this.property = new BitSet(256);
         }

         this.property.set(xChunk + zChunk * 16, true);
         if (this.property.cardinality() >= 256) {
            this.property = null;
            this.fullSet = true;
         }
      } else {
         if (this.fullSet) {
            this.property = new BitSet(256);
            this.property.flip(0, 256);
            this.fullSet = false;
         } else if (this.property == null) {
            this.property = new BitSet(256);
         }

         this.property.set(xChunk + zChunk * 16, false);
      }
   }

   public List<int[]> getAll() {
      List<int[]> result = new ArrayList<>();

      for (int zChunk = 0; zChunk < 16; zChunk++) {
         for (int xChunk = 0; xChunk < 16; xChunk++) {
            if (this.get(xChunk, zChunk)) {
               result.add(new int[]{xChunk, zChunk});
            }
         }
      }

      return result;
   }

   public void writeToNBT(CompoundTag nbt) {
      nbt.putBoolean("fullSet", this.fullSet);
      if (this.property != null) {
         nbt.putByteArray("bits", this.property.toByteArray());
      }
   }

   public void readFromNBT(CompoundTag nbt) {
      this.fullSet = nbt.getBooleanOr("fullSet", false);
      nbt.getByteArray("bits").ifPresent(bytes -> this.property = BitSet.valueOf(bytes));
   }

   public BlockPos getRandomBlockPos(Random rand) {
      int x;
      int z;
      if (this.fullSet) {
         x = rand.nextInt(16);
         z = rand.nextInt(16);
      } else {
         int bitId = rand.nextInt(this.property.cardinality());

         int bitPosition;
         for (bitPosition = this.property.nextSetBit(0); bitId > 0; bitPosition = this.property.nextSetBit(bitPosition + 1)) {
            bitId--;
         }

         z = bitPosition / 16;
         x = bitPosition - 16 * z;
      }

      int y = rand.nextInt(255);
      return new BlockPos(x, y, z);
   }

   public boolean isEmpty() {
      return !this.fullSet && (this.property == null || this.property.isEmpty());
   }

   public ZoneChunk readFromByteBuf(FriendlyByteBuf buf) {
      int flags = buf.readUnsignedByte();
      if ((flags & 1) != 0) {
         this.property = BitSet.valueOf(buf.readByteArray());
      }

      this.fullSet = (flags & 2) != 0;
      return this;
   }

   public void writeToByteBuf(FriendlyByteBuf buf) {
      int flags = (this.fullSet ? 2 : 0) | (this.property != null ? 1 : 0);
      buf.writeByte(flags);
      if (this.property != null) {
         buf.writeByteArray(this.property.toByteArray());
      }
   }
}
