package buildcraft.energy.generation.adapter;

import net.minecraft.core.BlockPos;

/**
 * Exploratory spike for a future structure-based oil well implementation.
 *
 * <p>This class intentionally stays out of runtime registration. It only captures the shape envelope we would need
 * for a {@code StructurePiece} migration and keeps the parity-risk discussion concrete in code.
 */
public final class OilWellStructureSpike {
   private OilWellStructureSpike() {
   }

   public static Envelope envelopeForSphereAndSpout(BlockPos center, int sphereRadius, int spoutHeight, int spoutRadius) {
      int minX = center.getX() - sphereRadius;
      int minY = center.getY() - sphereRadius;
      int minZ = center.getZ() - sphereRadius;
      int maxX = center.getX() + sphereRadius;
      int maxY = Math.max(center.getY() + sphereRadius, center.getY() + spoutHeight);
      int maxZ = center.getZ() + sphereRadius;

      if (spoutRadius > 0) {
         minX = Math.min(minX, center.getX() - spoutRadius);
         minZ = Math.min(minZ, center.getZ() - spoutRadius);
         maxX = Math.max(maxX, center.getX() + spoutRadius);
         maxZ = Math.max(maxZ, center.getZ() + spoutRadius);
      }

      return new Envelope(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
   }

   public record Envelope(BlockPos min, BlockPos max) {}
}
