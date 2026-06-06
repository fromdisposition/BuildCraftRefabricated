package buildcraft.silicon.gate;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementManager;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.ActionWrapper;
import buildcraft.lib.statement.StatementType;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;

public class ActionType extends StatementType<ActionWrapper> {
   public static final ActionType INSTANCE = new ActionType();

   private ActionType() {
      super(ActionWrapper.class, null);
   }

   public ActionWrapper convertToType(Object value) {
      return value instanceof IActionInternal ? new ActionWrapper.ActionWrapperInternal((IActionInternal)value) : null;
   }

   public ActionWrapper readFromNbt(CompoundTag nbt) {
      if (nbt == null) {
         return null;
      }

      String kind = nbt.getString("kind").orElse("");
      if (kind != null && !kind.isEmpty()) {
         EnumPipePart side = EnumPipePart.fromMeta(nbt.getByte("side").orElse((byte)5));
         IStatement statement = StatementManager.statements.get(kind);
         if (statement instanceof IAction) {
            return ActionWrapper.wrap(statement, side.face);
         }

         BCLog.logger.warn("[gate.trigger] Couldn't find an action called '{}'! (found {})", kind, statement);
         return null;
      } else {
         return null;
      }
   }

   public CompoundTag writeToNbt(ActionWrapper slot) {
      CompoundTag nbt = new CompoundTag();
      if (slot == null) {
         return nbt;
      }

      nbt.putString("kind", slot.getUniqueTag());
      nbt.putByte("side", (byte)slot.sourcePart.getIndex());
      return nbt;
   }

   public ActionWrapper readFromBuffer(PacketBufferBC buffer) throws IOException {
      if (buffer.readBoolean()) {
         String name = buffer.readUtf();
         EnumPipePart part = buffer.readEnumValue(EnumPipePart.class);
         IStatement statement = StatementManager.statements.get(name);
         if (statement instanceof IAction) {
            return ActionWrapper.wrap(statement, part.face);
         } else {
            throw new InvalidInputDataException("Unknown action '" + name + "'");
         }
      } else {
         return null;
      }
   }

   public void writeToBuffer(PacketBufferBC buffer, ActionWrapper slot) {
      if (slot == null) {
         buffer.writeBoolean(false);
      } else {
         buffer.writeBoolean(true);
         buffer.writeUtf(slot.getUniqueTag());
         buffer.writeEnumValue(slot.sourcePart);
      }
   }
}
