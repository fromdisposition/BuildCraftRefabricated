package buildcraft.lib.statement;

import buildcraft.api.statements.IGuiSlot;
import buildcraft.lib.net.PacketBufferBC;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;

public abstract class StatementType<S extends IGuiSlot> {
   public final Class<S> clazz;
   public final S defaultStatement;

   public StatementType(Class<S> clazz, S defaultStatement) {
      this.clazz = clazz;
      this.defaultStatement = defaultStatement;
   }

   public abstract S readFromNbt(CompoundTag var1);

   public abstract CompoundTag writeToNbt(S var1);

   public abstract S readFromBuffer(PacketBufferBC var1) throws IOException;

   public abstract void writeToBuffer(PacketBufferBC var1, S var2);

   @Nullable
   public abstract S convertToType(Object var1);
}
