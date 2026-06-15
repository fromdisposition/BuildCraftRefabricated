package buildcraft.builders.snapshot;

import static org.junit.jupiter.api.Assertions.*;

import buildcraft.api.core.InvalidInputDataException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class BlueprintSerializationTest {

   private static Blueprint emptyBlueprint(BlockPos size, Direction facing, BlockPos offset) {
      Blueprint bp = new Blueprint();
      bp.size = size;
      bp.facing = facing;
      bp.offset = offset;
      bp.data = new int[Snapshot.getDataSize(size)];
      return bp;
   }

   @Test
   void metadataRoundTrip() throws InvalidInputDataException {
      Blueprint original = emptyBlueprint(new BlockPos(2, 3, 4), Direction.EAST, new BlockPos(1, 0, 0));

      CompoundTag nbt = Snapshot.writeToNBT(original);
      Blueprint restored = (Blueprint) Snapshot.readFromNBT(nbt);

      assertEquals(original.size, restored.size);
      assertEquals(original.facing, restored.facing);
      assertEquals(original.offset, restored.offset);
   }

   @Test
   void sizeRoundTrips() throws InvalidInputDataException {
      BlockPos size = new BlockPos(5, 10, 15);
      Blueprint bp = emptyBlueprint(size, Direction.NORTH, BlockPos.ZERO);

      Blueprint restored = (Blueprint) Snapshot.readFromNBT(Snapshot.writeToNBT(bp));

      assertEquals(size, restored.size);
   }

   @Test
   void facingRoundTripsAllDirections() throws InvalidInputDataException {
      for (Direction dir : Direction.values()) {
         Blueprint bp = emptyBlueprint(new BlockPos(1, 1, 1), dir, BlockPos.ZERO);
         Blueprint restored = (Blueprint) Snapshot.readFromNBT(Snapshot.writeToNBT(bp));
         assertEquals(dir, restored.facing, "facing=" + dir);
      }
   }

   @Test
   void emptyPaletteRoundTrips() throws InvalidInputDataException {
      Blueprint bp = emptyBlueprint(new BlockPos(1, 1, 1), Direction.SOUTH, BlockPos.ZERO);
      assertTrue(bp.palette.isEmpty());

      Blueprint restored = (Blueprint) Snapshot.readFromNBT(Snapshot.writeToNBT(bp));

      assertTrue(restored.palette.isEmpty());
      assertEquals(0, restored.entities.size());
   }

   @Test
   void dataArrayRoundTrips() throws InvalidInputDataException {
      BlockPos size = new BlockPos(2, 2, 2);
      Blueprint bp = emptyBlueprint(size, Direction.NORTH, BlockPos.ZERO);
      int dataSize = Snapshot.getDataSize(size);
      for (int i = 0; i < dataSize; i++) {
         bp.data[i] = i;
      }

      Blueprint restored = (Blueprint) Snapshot.readFromNBT(Snapshot.writeToNBT(bp));

      assertEquals(dataSize, restored.data.length);
      for (int i = 0; i < dataSize; i++) {
         assertEquals(i, restored.data[i], "data[" + i + "]");
      }
   }

   @Test
   void computeKeyProducesStableHash() throws InvalidInputDataException {
      Blueprint bp = emptyBlueprint(new BlockPos(3, 3, 3), Direction.WEST, new BlockPos(0, 1, 0));
      bp.computeKey();

      // hash must be non-empty after compute
      assertTrue(bp.key.hash.length > 0, "key hash should be non-empty");

      // recomputing on an identical blueprint gives the same hash
      Blueprint bp2 = emptyBlueprint(new BlockPos(3, 3, 3), Direction.WEST, new BlockPos(0, 1, 0));
      bp2.computeKey();
      assertArrayEquals(bp.key.hash, bp2.key.hash, "identical blueprints must hash identically");
   }

   @Test
   void computeKeyDiffersForDifferentSizes() {
      Blueprint bp1 = emptyBlueprint(new BlockPos(1, 1, 1), Direction.NORTH, BlockPos.ZERO);
      Blueprint bp2 = emptyBlueprint(new BlockPos(2, 2, 2), Direction.NORTH, BlockPos.ZERO);
      bp1.computeKey();
      bp2.computeKey();

      assertFalse(
         java.util.Arrays.equals(bp1.key.hash, bp2.key.hash),
         "blueprints with different sizes must have different hashes"
      );
   }

   @Test
   void getDataSizeMatchesDimensions() {
      assertEquals(1, Snapshot.getDataSize(new BlockPos(1, 1, 1)));
      assertEquals(8, Snapshot.getDataSize(new BlockPos(2, 2, 2)));
      assertEquals(60, Snapshot.getDataSize(new BlockPos(3, 4, 5)));
   }

   @Test
   void posToIndexAndBackIsIdentity() {
      BlockPos size = new BlockPos(4, 4, 4);
      for (int x = 0; x < 4; x++) {
         for (int y = 0; y < 4; y++) {
            for (int z = 0; z < 4; z++) {
               int idx = Snapshot.posToIndex(size, x, y, z);
               BlockPos recovered = Snapshot.indexToPos(size, idx);
               assertEquals(new BlockPos(x, y, z), recovered, "roundtrip at " + x + "," + y + "," + z);
            }
         }
      }
   }
}
