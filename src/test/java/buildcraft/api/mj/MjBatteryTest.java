package buildcraft.api.mj;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class MjBatteryTest {
   private static final long MJ = MjAPI.MJ;
   private static final long CAPACITY = 100 * MJ;

   private MjBattery battery() {
      return new MjBattery(CAPACITY);
   }

   @Test
   void addPowerWithRoomAcceptsAll() {
      MjBattery b = battery();
      long leftover = b.addPower(10 * MJ, false);
      assertEquals(0, leftover);
      assertEquals(10 * MJ, b.getStored());
   }

   @Test
   void addPowerExceedingCapacityReturnsExcess() {
      MjBattery b = battery();
      long leftover = b.addPower(CAPACITY + 5 * MJ, false);
      assertEquals(5 * MJ, leftover);
      assertEquals(CAPACITY, b.getStored());
   }

   @Test
   void addPowerSimulateDoesNotChangeStored() {
      MjBattery b = battery();
      b.addPower(10 * MJ, true);
      assertEquals(0, b.getStored());
   }

   @Test
   void addPowerCheckingWhenFullRejectsAll() {
      MjBattery b = battery();
      b.addPower(CAPACITY, false);
      assertTrue(b.isFull());
      long leftover = b.addPowerChecking(5 * MJ, false);
      assertEquals(5 * MJ, leftover);
      assertEquals(CAPACITY, b.getStored());
   }

   @Test
   void extractPowerWithinStoredSucceeds() {
      MjBattery b = battery();
      b.addPower(50 * MJ, false);
      long extracted = b.extractPower(10 * MJ, 10 * MJ);
      assertEquals(10 * MJ, extracted);
      assertEquals(40 * MJ, b.getStored());
   }

   @Test
   void extractPowerBelowMinReturnsZero() {
      MjBattery b = battery();
      b.addPower(5 * MJ, false);
      long extracted = b.extractPower(10 * MJ, 20 * MJ);
      assertEquals(0, extracted);
      assertEquals(5 * MJ, b.getStored());
   }

   @Test
   void extractAllDrainsBattery() {
      MjBattery b = battery();
      b.addPower(CAPACITY, false);
      long drained = b.extractAll();
      assertEquals(CAPACITY, drained);
      assertEquals(0, b.getStored());
   }

   @Test
   void setStoredClampsNegativeToZero() {
      MjBattery b = battery();
      b.setStored(-1);
      assertEquals(0, b.getStored());
   }

   @Test
   void setStoredClampsAboveCapacityToCapacity() {
      MjBattery b = battery();
      b.setStored(CAPACITY + MJ);
      assertEquals(CAPACITY, b.getStored());
   }

   @Test
   void nbtRoundTrip() {
      MjBattery b = battery();
      b.addPower(42 * MJ, false);
      CompoundTag nbt = b.serializeNBT();

      MjBattery restored = new MjBattery(CAPACITY);
      restored.deserializeNBT(nbt);
      assertEquals(42 * MJ, restored.getStored());
   }

   @Test
   void isFullReturnsTrueAtCapacity() {
      MjBattery b = battery();
      b.addPower(CAPACITY, false);
      assertTrue(b.isFull());
   }

   @Test
   void isFullReturnsFalseWhenPartial() {
      MjBattery b = battery();
      b.addPower(CAPACITY - 1, false);
      assertFalse(b.isFull());
   }

   @Test
   void capacityReported() {
      MjBattery b = battery();
      assertEquals(CAPACITY, b.getCapacity());
   }
}
