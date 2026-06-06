package buildcraft.silicon.gate;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITrigger;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.StatementManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.StatementType;
import buildcraft.lib.statement.TriggerWrapper;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;

public class TriggerType extends StatementType<TriggerWrapper> {
   public static final TriggerType INSTANCE = new TriggerType();

   private TriggerType() {
      super(TriggerWrapper.class, null);
   }

   public TriggerWrapper convertToType(Object value) {
      return value instanceof ITriggerInternal ? new TriggerWrapper.TriggerWrapperInternal((ITriggerInternal)value) : null;
   }

   public TriggerWrapper readFromNbt(CompoundTag nbt) {
      if (nbt == null) {
         return null;
      }

      String kind = (String)nbt.getString("kind").orElse(null);
      if (kind != null && !kind.isEmpty()) {
         EnumPipePart side = EnumPipePart.fromMeta(nbt.getByte("side").orElse((byte)5));
         IStatement statement = StatementManager.statements.get(kind);
         if (statement instanceof ITrigger) {
            return TriggerWrapper.wrap(statement, side.face);
         }

         BCLog.logger.warn("[gate.trigger] Couldn't find a trigger called '{}'! (found {})", kind, statement);
         return null;
      } else {
         return null;
      }
   }

   public CompoundTag writeToNbt(TriggerWrapper slot) {
      CompoundTag nbt = new CompoundTag();
      if (slot == null) {
         return nbt;
      }

      nbt.putString("kind", slot.getUniqueTag());
      nbt.putByte("side", (byte)slot.sourcePart.getIndex());
      return nbt;
   }

   public TriggerWrapper readFromBuffer(PacketBufferBC buffer) throws IOException {
      if (buffer.readBoolean()) {
         String name = buffer.readUtf();
         EnumPipePart part = buffer.readEnumValue(EnumPipePart.class);
         IStatement statement = StatementManager.statements.get(name);
         if (statement instanceof ITrigger) {
            return TriggerWrapper.wrap(statement, part.face);
         } else {
            throw new InvalidInputDataException("Unknown trigger '" + name + "'");
         }
      } else {
         return null;
      }
   }

   public void writeToBuffer(PacketBufferBC buffer, TriggerWrapper slot) {
      if (slot == null) {
         buffer.writeBoolean(false);
      } else {
         buffer.writeBoolean(true);
         buffer.writeUtf(slot.getUniqueTag());
         buffer.writeEnumValue(slot.sourcePart);
      }
   }
}
