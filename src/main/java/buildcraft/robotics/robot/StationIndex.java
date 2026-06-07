package buildcraft.robotics.robot;

import buildcraft.api.robots.DockingStation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class StationIndex {
   private final BlockPos pos;
   private final Direction side;

   public StationIndex(DockingStation station) {
      this(station.getPos(), station.side());
   }

   public StationIndex(BlockPos pos, Direction side) {
      this.pos = pos.immutable();
      this.side = side;
   }

   public BlockPos pos() {
      return this.pos;
   }

   public Direction side() {
      return this.side;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (!(obj instanceof StationIndex other)) {
         return false;
      }

      return this.pos.equals(other.pos) && this.side == other.side;
   }

   @Override
   public int hashCode() {
      return 31 * this.pos.hashCode() + this.side.ordinal();
   }

   @Override
   public String toString() {
      return "{" + this.pos + ", " + this.side + "}";
   }
}
