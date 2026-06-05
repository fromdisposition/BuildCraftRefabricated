package buildcraft.lib.statement;

import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;

import buildcraft.api.statements.IGuiSlot;

import buildcraft.lib.net.PacketBufferBC;

public abstract class StatementType<S extends IGuiSlot> {

    public final Class<S> clazz;
    public final S defaultStatement;

    public StatementType(Class<S> clazz, S defaultStatement) {
        this.clazz = clazz;
        this.defaultStatement = defaultStatement;
    }

    public abstract S readFromNbt(CompoundTag nbt);

    public abstract CompoundTag writeToNbt(S slot);

    public abstract S readFromBuffer(PacketBufferBC buffer) throws IOException;

    public abstract void writeToBuffer(PacketBufferBC buffer, S slot);

    @Nullable
    public abstract S convertToType(Object value);
}
