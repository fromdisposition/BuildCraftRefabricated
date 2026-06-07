package buildcraft.robotics.robot;

import buildcraft.api.mj.MjBattery;

/**
 * An {@link MjBattery} facade backed entirely by a robot's real battery. Power extraction is gated by {@link #inRange}:
 * while the builder robot has not physically reached the block it is working on, no energy is allowed to flow, so the
 * underlying {@link buildcraft.builders.snapshot.SnapshotBuilder} cannot finish any break/place task. This reproduces
 * the strict "fly to every block before acting" behaviour without forking the snapshot pipeline.
 */
public class GatedMjBattery extends MjBattery {
   private final MjBattery delegate;
   public boolean inRange;

   public GatedMjBattery(MjBattery delegate) {
      super(delegate.getCapacity());
      this.delegate = delegate;
   }

   @Override
   public long getStored() {
      return this.delegate.getStored();
   }

   @Override
   public long getCapacity() {
      return this.delegate.getCapacity();
   }

   @Override
   public boolean isFull() {
      return this.delegate.isFull();
   }

   @Override
   public long addPower(long microJoulesToAdd, boolean simulate) {
      return this.delegate.addPower(microJoulesToAdd, simulate);
   }

   @Override
   public long extractPower(long min, long max) {
      return this.inRange ? this.delegate.extractPower(min, max) : 0L;
   }

   @Override
   public long extractAll() {
      return this.delegate.extractAll();
   }

   @Override
   public void setStored(long microJoules) {
      this.delegate.setStored(microJoules);
   }
}
