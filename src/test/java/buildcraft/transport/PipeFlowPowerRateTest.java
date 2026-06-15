package buildcraft.transport;

import static org.junit.jupiter.api.Assertions.*;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import org.junit.jupiter.api.Test;

/**
 * Verifies the MJ/t transfer caps produced by BCTransportConfig.registerPowerTransferData()
 * using the default basePowerRate=4. Tests are done by replicating the formula directly
 * so they remain independent of pipe registration (which requires a full MC bootstrap).
 */
class PipeFlowPowerRateTest {
   private static final long MJ = MjAPI.MJ;
   private static final int RATE = 4; // default basePowerRate

   private static PowerTransferInfo info(int multiplier, int resistanceDivisor, boolean recv) {
      long transfer = MJ * multiplier;
      long resistance = MJ / resistanceDivisor;
      return PowerTransferInfo.createFromResistance(transfer, resistance, recv);
   }

   @Test
   void cobblePipeIs4MJPerTick() {
      PowerTransferInfo cobble = info(RATE, 16, false);
      assertEquals(4 * MJ, cobble.transferPerTick, "cobblePower: 4 MJ/t");
      assertFalse(cobble.isReceiver);
   }

   @Test
   void stonePipeIs8MJPerTick() {
      PowerTransferInfo stone = info(RATE * 2, 32, false);
      assertEquals(8 * MJ, stone.transferPerTick, "stonePower: 8 MJ/t");
   }

   @Test
   void woodPipeIs16MJPerTickAndReceiver() {
      PowerTransferInfo wood = info(RATE * 4, 128, true);
      assertEquals(16 * MJ, wood.transferPerTick, "woodPower: 16 MJ/t");
      assertTrue(wood.isReceiver, "wood power pipe must be an extractor (isReceiver=true)");
   }

   @Test
   void sandstonePipeIs16MJPerTick() {
      PowerTransferInfo sandstone = info(RATE * 4, 32, false);
      assertEquals(16 * MJ, sandstone.transferPerTick, "sandstonePower: 16 MJ/t");
   }

   @Test
   void quartzPipeIs32MJPerTick() {
      PowerTransferInfo quartz = info(RATE * 8, 32, false);
      assertEquals(32 * MJ, quartz.transferPerTick, "quartzPower: 32 MJ/t");
   }

   @Test
   void ironPipeIs32MJPerTick() {
      PowerTransferInfo iron = info(RATE * 8, 32, false);
      assertEquals(32 * MJ, iron.transferPerTick, "ironPower: 32 MJ/t");
   }

   @Test
   void goldPipeIs128MJPerTick() {
      PowerTransferInfo gold = info(RATE * 32, 32, false);
      assertEquals(128 * MJ, gold.transferPerTick, "goldPower: 128 MJ/t");
   }

   @Test
   void diamondPipeIs256MJPerTick() {
      PowerTransferInfo diamond = info(RATE * 64, 32, false);
      assertEquals(256 * MJ, diamond.transferPerTick, "diamondPower: 256 MJ/t");
   }

   @Test
   void diamondWoodPipeIs256MJPerTickAndReceiver() {
      PowerTransferInfo diaWood = info(RATE * 64, 32, true);
      assertEquals(256 * MJ, diaWood.transferPerTick, "diaWoodPower: 256 MJ/t");
      assertTrue(diaWood.isReceiver, "diamond wood power pipe must be an extractor (isReceiver=true)");
   }

   @Test
   void woodPipeHasLowerResistanceThanCobble() {
      // wood has resistanceDivisor=128, cobble=16 — higher divisor means lower resistance per MJ
      PowerTransferInfo wood = info(RATE * 4, 128, true);
      PowerTransferInfo cobble = info(RATE, 16, false);
      assertTrue(wood.lossPerTick < cobble.lossPerTick,
         "wood pipe should lose less per MJ than cobble pipe");
   }

   @Test
   void createFromResistanceEnforcesMinTransfer() {
      // the constructor clamps transferPerTick to a minimum of 10 µMJ
      PowerTransferInfo tiny = PowerTransferInfo.createFromResistance(1L, 1L, false);
      assertEquals(10L, tiny.transferPerTick, "minimum transferPerTick is 10 µMJ");
   }
}
