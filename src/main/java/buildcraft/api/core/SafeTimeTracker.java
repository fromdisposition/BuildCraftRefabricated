package buildcraft.api.core;

import net.minecraft.world.level.Level;

public class SafeTimeTracker {
   private long lastMark = Long.MIN_VALUE;
   private long duration = -1L;
   private long randomRange = 0L;
   private long lastRandomDelay = 0L;
   private long internalDelay = 1L;

   public SafeTimeTracker() {
   }

   public SafeTimeTracker(long delay) {
      this.internalDelay = delay;
   }

   public SafeTimeTracker(long delay, long random) {
      this.internalDelay = delay;
      this.randomRange = random;
   }

   public boolean markTimeIfDelay(Level world) {
      return this.markTimeIfDelay(world, this.internalDelay);
   }

   public boolean markTimeIfDelay(Level world, long delay) {
      if (world == null) {
         return false;
      } else {
         long currentTime = world.getGameTime();
         if (currentTime < this.lastMark) {
            this.lastMark = currentTime;
            return false;
         } else if (this.lastMark + delay + this.lastRandomDelay <= currentTime) {
            this.duration = currentTime - this.lastMark;
            this.lastMark = currentTime;
            this.lastRandomDelay = (int)(Math.random() * this.randomRange);
            return true;
         } else {
            return false;
         }
      }
   }

   public long durationOfLastDelay() {
      return this.duration > 0L ? this.duration : 0L;
   }

   public void markTime(Level world) {
      this.lastMark = world.getGameTime();
   }
}
