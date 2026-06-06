package buildcraft.api.transport;

import net.minecraft.world.item.DyeColor;

public interface IWireEmitter {
   boolean isEmitting(DyeColor var1);

   void emitWire(DyeColor var1);
}
