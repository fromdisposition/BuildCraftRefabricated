package buildcraft.api.transport.pluggable;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.transport.pipe.IPipeHolder;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public final class PluggableDefinition {
   public final Object identifier;
   public final PluggableDefinition.IPluggableNetLoader loader;
   public final PluggableDefinition.IPluggableNbtReader reader;
   @Nullable
   public final PluggableDefinition.IPluggableCreator creator;

   public PluggableDefinition(Object identifier, PluggableDefinition.IPluggableNbtReader reader, PluggableDefinition.IPluggableNetLoader loader) {
      this.identifier = identifier;
      this.reader = reader;
      this.loader = loader;
      this.creator = null;
   }

   public PluggableDefinition(Object identifier, @Nullable PluggableDefinition.IPluggableCreator creator) {
      this.identifier = identifier;
      this.reader = creator;
      this.loader = creator;
      this.creator = creator;
   }

   public PipePluggable readFromNbt(IPipeHolder holder, Direction side, CompoundTag nbt) {
      return this.reader.readFromNbt(this, holder, side, nbt);
   }

   public PipePluggable loadFromBuffer(IPipeHolder holder, Direction side, FriendlyByteBuf buffer) throws InvalidInputDataException {
      return this.loader.loadFromBuffer(this, holder, side, buffer);
   }

   @FunctionalInterface
   public interface IPluggableCreator extends PluggableDefinition.IPluggableNbtReader, PluggableDefinition.IPluggableNetLoader {
      @Override
      default PipePluggable loadFromBuffer(PluggableDefinition definition, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
         return this.createSimplePluggable(definition, holder, side);
      }

      @Override
      default PipePluggable readFromNbt(PluggableDefinition definition, IPipeHolder holder, Direction side, CompoundTag nbt) {
         return this.createSimplePluggable(definition, holder, side);
      }

      PipePluggable createSimplePluggable(PluggableDefinition var1, IPipeHolder var2, Direction var3);
   }

   @FunctionalInterface
   public interface IPluggableNbtReader {
      PipePluggable readFromNbt(PluggableDefinition var1, IPipeHolder var2, Direction var3, CompoundTag var4);
   }

   @FunctionalInterface
   public interface IPluggableNetLoader {
      PipePluggable loadFromBuffer(PluggableDefinition var1, IPipeHolder var2, Direction var3, FriendlyByteBuf var4) throws InvalidInputDataException;
   }
}
