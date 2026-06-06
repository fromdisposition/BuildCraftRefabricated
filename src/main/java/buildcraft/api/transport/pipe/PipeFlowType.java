package buildcraft.api.transport.pipe;

import net.minecraft.nbt.CompoundTag;

public final class PipeFlowType {
   public final PipeFlowType.IFlowCreator creator;
   public final PipeFlowType.IFlowLoader loader;
   public EnumPipeColourType fallbackColourType;

   public PipeFlowType(PipeFlowType.IFlowCreator creator, PipeFlowType.IFlowLoader loader) {
      this(creator, loader, null);
   }

   public PipeFlowType(PipeFlowType.IFlowCreator creator, PipeFlowType.IFlowLoader loader, EnumPipeColourType colourType) {
      this.creator = creator;
      this.loader = loader;
      this.fallbackColourType = colourType;
   }

   @FunctionalInterface
   public interface IFlowCreator {
      PipeFlow createFlow(IPipe var1);
   }

   @FunctionalInterface
   public interface IFlowLoader {
      PipeFlow loadFlow(IPipe var1, CompoundTag var2);
   }
}
