package buildcraft.lib.statement;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.lib.net.PacketBufferBC;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;

public class StatementTypeParam extends StatementType<IStatementParameter> {
   public static final StatementTypeParam INSTANCE = new StatementTypeParam();

   public StatementTypeParam() {
      super(IStatementParameter.class, null);
   }

   public IStatementParameter convertToType(Object value) {
      return value instanceof IStatementParameter ? (IStatementParameter)value : null;
   }

   public IStatementParameter readFromNbt(CompoundTag nbt) {
      String kind = nbt.getString("kind").orElse("");
      StatementManager.IParameterReader reader = StatementManager.getParameterReader(kind);
      return reader == null ? null : reader.readFromNbt(nbt);
   }

   public CompoundTag writeToNbt(IStatementParameter slot) {
      CompoundTag nbt = new CompoundTag();
      if (slot != null) {
         slot.writeToNbt(nbt);
         nbt.putString("kind", slot.getUniqueTag());
      }

      return nbt;
   }

   public IStatementParameter readFromBuffer(FriendlyByteBuf buffer) throws IOException {
      if (buffer.readBoolean()) {
         String tag = buffer.readUtf();
         StatementManager.IParamReaderBuf reader = StatementManager.paramsBuf.get(tag);
         if (reader == null) {
            throw new InvalidInputDataException("Unknown paramater type " + tag);
         } else {
            return reader.readFromBuf(buffer);
         }
      } else {
         return null;
      }
   }

   public void writeToBuffer(FriendlyByteBuf buffer, IStatementParameter slot) {
      if (slot == null) {
         buffer.writeBoolean(false);
      } else {
         buffer.writeBoolean(true);
         buffer.writeUtf(slot.getUniqueTag());
         slot.writeToBuf(buffer);
      }
   }
}
