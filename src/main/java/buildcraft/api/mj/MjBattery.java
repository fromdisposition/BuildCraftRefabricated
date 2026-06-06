package buildcraft.api.mj;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MjBattery {
   private final long capacity;
   private long microJoules = 0L;

   public MjBattery(long capacity) {
      this.capacity = capacity;
   }

   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.putLong("stored", this.microJoules);
      return nbt;
   }

   public void deserializeNBT(CompoundTag nbt) {
      this.setStored(nbt.getLong("stored").orElse(0L));
   }

   public void writeToBuffer(ByteBuf buffer) {
      buffer.writeLong(this.microJoules);
   }

   public void readFromBuffer(ByteBuf buffer) {
      this.setStored(buffer.readLong());
   }

   public void setStored(long microJoules) {
      if (microJoules < 0L) {
         this.microJoules = 0L;
      } else if (microJoules > this.capacity) {
         this.microJoules = this.capacity;
      } else {
         this.microJoules = microJoules;
      }
   }

   public long addPower(long microJoulesToAdd, boolean simulate) {
      long accepted = microJoulesToAdd;
      if (microJoulesToAdd > 0L) {
         long room = Math.max(0L, this.capacity - this.microJoules);
         accepted = Math.min(microJoulesToAdd, room);
      }

      if (!simulate) {
         this.microJoules += accepted;
      }

      return microJoulesToAdd - accepted;
   }

   public long addPowerChecking(long microJoulesToAdd, boolean simulate) {
      return this.isFull() ? microJoulesToAdd : this.addPower(microJoulesToAdd, simulate);
   }

   public long extractAll() {
      return this.extractPower(0L, this.microJoules);
   }

   public boolean extractPower(long power) {
      return this.extractPower(power, power) > 0L;
   }

   public long extractPower(long min, long max) {
      if (this.microJoules < min) {
         return 0L;
      }

      long extracting = Math.min(this.microJoules, max);
      this.microJoules -= extracting;
      return extracting;
   }

   public boolean isFull() {
      return this.microJoules >= this.capacity;
   }

   public long getStored() {
      return this.microJoules;
   }

   public long getCapacity() {
      return this.capacity;
   }

   public void tick(Level world, BlockPos position) {
      this.tick(world, new Vec3(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5));
   }

   public void tick(Level world, Vec3 position) {
      if (this.microJoules > this.capacity * 2L) {
         this.losePower(world, position);
      }
   }

   protected void losePower(Level world, Vec3 position) {
      long diff = this.microJoules - this.capacity * 2L;
      long lost = ceilDivide(diff, 32L);
      this.microJoules -= lost;
      MjAPI.EFFECT_MANAGER.createPowerLossEffect(world, position, lost);
   }

   private static long ceilDivide(long val, long by) {
      return val / by + (val % by == 0L ? 0 : 1);
   }

   public String getDebugString() {
      return MjAPI.formatMj(this.microJoules) + " / " + MjAPI.formatMj(this.capacity) + " MJ";
   }
}
