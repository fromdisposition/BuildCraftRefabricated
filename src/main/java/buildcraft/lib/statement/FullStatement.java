package buildcraft.lib.statement;

import java.io.IOException;
import java.util.Arrays;

import net.minecraft.nbt.CompoundTag;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.net.PacketBufferBC;

public class FullStatement<S extends IStatement> implements IReference<S> {
    public final StatementType<S> type;
    public final int maxParams;
    public boolean canInteract = true;
    private final IStatementChangeListener listener;
    private final IStatementParameter[] params;
    private final ParamRef[] paramRefs;
    private S statement;

    public FullStatement(StatementType<S> type, int maxParams, IStatementChangeListener listener) {
        this.type = type;
        this.statement = type.defaultStatement;
        this.listener = listener;
        this.maxParams = maxParams;
        this.params = new IStatementParameter[maxParams];
        this.paramRefs = new FullStatement.ParamRef[maxParams];
        for (int i = 0; i < maxParams; i++) {
            paramRefs[i] = new ParamRef(this, i);
        }
    }

    public void readFromNbt(CompoundTag nbt) {
        statement = type.readFromNbt(nbt.getCompound("s").orElse(new CompoundTag()));
        if (statement == null) {
            Arrays.fill(params, null);
        } else {
            for (int p = 0; p < params.length; p++) {
                CompoundTag pNbt = nbt.getCompound(Integer.toString(p)).orElse(new CompoundTag());
                params[p] = StatementTypeParam.INSTANCE.readFromNbt(pNbt);
            }
        }
    }

    public CompoundTag writeToNbt() {
        CompoundTag nbt = new CompoundTag();
        if (statement != null) {
            nbt.put("s", type.writeToNbt(statement));
            for (int p = 0; p < params.length; p++) {
                IStatementParameter param = params[p];
                if (param != null) {
                    nbt.put(Integer.toString(p), StatementTypeParam.INSTANCE.writeToNbt(param));
                }
            }
        }
        return nbt;
    }

    public void readFromBuffer(PacketBufferBC buffer) throws IOException {
        if (buffer.readBoolean()) {
            statement = type.readFromBuffer(buffer);
            for (int p = 0; p < params.length; p++) {
                params[p] = StatementTypeParam.INSTANCE.readFromBuffer(buffer);
            }
        } else {
            statement = type.defaultStatement;
            Arrays.fill(params, null);
        }
    }

    public void writeToBuffer(PacketBufferBC buffer) {
        if (statement == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            type.writeToBuffer(buffer, statement);
            for (int p = 0; p < params.length; p++) {
                IStatementParameter param = params[p];
                StatementTypeParam.INSTANCE.writeToBuffer(buffer, param);
            }
        }
    }

    @Override
    public S get() {
        return statement;
    }

    @Override
    public void set(S to) {
        statement = to;
        if (statement == null) {
            Arrays.fill(params, null);
            return;
        }
        for (int i = 0; i < params.length; i++) {
            if (i > statement.maxParameters()) {
                params[i] = null;
            } else {
                params[i] = statement.createParameter(params[i], i);
            }
        }
    }

    @Override
    public boolean canSet(S value) {
        if (value == null) {
            return true;
        }
        return value.minParameters() <= params.length;
    }

    @Override
    public S convertToType(Object value) {
        S val = IReference.super.convertToType(value);
        if (value != null && val == null) {
            return type.convertToType(value);
        }
        return val;
    }

    @Override
    public Class<S> getHeldType() {
        return type.clazz;
    }

    static class ParamRef implements IReference<IStatementParameter> {
        public final IReference<? extends IStatement> statementRef;
        public final IStatementParameter[] array;
        public final int index;

        public ParamRef(FullStatement<?> full, int index) {
            statementRef = full;
            this.array = full.params;
            this.index = index;
        }

        @Override
        public IStatementParameter get() {
            return array[index];
        }

        @Override
        public void set(IStatementParameter to) {
            array[index] = to;
        }

        @Override
        public boolean canSet(IStatementParameter value) {
            IStatement statement = statementRef.get();
            if (statement == null) {
                return false;
            }
            return statement.createParameter(value, index) == value;
        }

        @Override
        public Class<IStatementParameter> getHeldType() {
            return IStatementParameter.class;
        }
    }

    public IReference<IStatementParameter> getParamRef(int i) {
        return paramRefs[i];
    }

    public IStatementParameter get(int index) {
        return getParamRef(index).get();
    }

    public void set(int index, IStatementParameter param) {
        getParamRef(index).set(param);
    }

    public void set(S statement, IStatementParameter[] params) {
        set(statement);
        for (int i = Math.min(getParamCount(), params.length) - 1; i > 0; i--) {
            set(i, params[i]);
        }
    }

    public boolean canSet(int index, IStatementParameter param) {
        return getParamRef(index).canSet(param);
    }

    public int getParamCount() {
        return params.length;
    }

    public IStatementParameter[] getParameters() {
        return params;
    }

    public void postSetFromGui(int paramIndex) {
        if (listener != null) {
            listener.onChange(this, paramIndex);
        }
    }

    @FunctionalInterface
    public interface IStatementChangeListener {

        void onChange(FullStatement<?> statement, int paramIndex);
    }
}
