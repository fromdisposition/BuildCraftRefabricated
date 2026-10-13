
package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.api.schematics.SchematicEntityContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;

public final class SchematicEntityUnavailable implements ISchematicEntity {
   private final CompoundTag serialized;

   public SchematicEntityUnavailable(CompoundTag serialized) {
      this.serialized = serialized.copy();
   }

   public CompoundTag serializedEnvelope() {
      return this.serialized.copy();
   }

   @Override
   public void init(SchematicEntityContext context) {
   }

   @Override
   public Vec3 getPos() {
      return Vec3.ZERO;
   }

   @Override
   public ISchematicEntity getRotated(Rotation rotation) {
      return this;
   }

   @Override
   public Entity build(Level level, BlockPos basePos) {
      return null;
   }

   @Override
   public Entity buildWithoutChecks(Level level, BlockPos basePos) {
      return null;
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
      return this == o || o instanceof SchematicEntityUnavailable other && this.serialized.equals(other.serialized);
   }

   @Override
   public int hashCode() {
      return this.serialized.hashCode();
   }
}
