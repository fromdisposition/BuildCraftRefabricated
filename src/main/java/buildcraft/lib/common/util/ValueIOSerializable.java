package buildcraft.lib.common.util;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ValueIOSerializable {
   void serialize(ValueOutput var1);

   void deserialize(ValueInput var1);
}
