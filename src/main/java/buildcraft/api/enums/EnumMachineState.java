package buildcraft.api.enums;

import buildcraft.api.properties.BuildCraftProperties;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;

public enum EnumMachineState implements StringRepresentable {
   OFF,
   ON,
   DONE;

   public static EnumMachineState getType(BlockState state) {
      return (EnumMachineState)state.getValue(BuildCraftProperties.MACHINE_STATE);
   }

   public String getSerializedName() {
      return this.name();
   }
}
