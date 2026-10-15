
package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

public final class SchematicBlockUnavailable implements ISchematicBlock {
   private final CompoundTag serialized;

   public SchematicBlockUnavailable(CompoundTag serialized) {
      this.serialized = serialized.copy();
   }

   public CompoundTag serializedEnvelope() {
      return this.serialized.copy();
   }

   @Override
   public void init(SchematicBlockContext context) {
   }

   @Override
   public ISchematicBlock getRotated(Rotation rotation) {
      return this;
   }

   @Override
   public boolean canBuild(Level level, BlockPos blockPos) {
      return false;
   }

   @Override
   public boolean build(Level level, BlockPos blockPos) {
      return false;
   }

   @Override
   public boolean buildWithoutChecks(Level level, BlockPos blockPos) {
      return false;
   }

   @Override
   public boolean isBuilt(Level level, BlockPos blockPos) {
      return true;
   }

   @Override
   public CompoundTag serializeNBT() {
      return this.serialized.copy();
   }

   @Override
   public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
      throw new InvalidInputDataException("Unavailable schematic placeholders are immutable");
   }

   @Override
   public boolean equals(Object o) {
      return this == o || o instanceof SchematicBlockUnavailable other && this.serialized.equals(other.serialized);
   }

   @Override
   public int hashCode() {
      return this.serialized.hashCode();
   }
}
