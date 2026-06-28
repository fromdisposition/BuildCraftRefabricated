/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.statement;

import buildcraft.lib.nbt.BcNbt;
import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.lib.misc.data.IReference;
import buildcraft.lib.net.PacketBufferBC;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;

public class FullStatement<S extends IStatement> implements IReference<S> {
   public final StatementType<S> type;
   public final int maxParams;
   public boolean canInteract = true;
   private final FullStatement.IStatementChangeListener listener;
   private final IStatementParameter[] params;
   private final FullStatement.ParamRef[] paramRefs;
   private S statement;

   public FullStatement(StatementType<S> type, int maxParams, FullStatement.IStatementChangeListener listener) {
      this.type = type;
      this.statement = type.defaultStatement;
      this.listener = listener;
      this.maxParams = maxParams;
      this.params = new IStatementParameter[maxParams];
      this.paramRefs = new FullStatement.ParamRef[maxParams];

      for (int i = 0; i < maxParams; i++) {
         this.paramRefs[i] = new FullStatement.ParamRef(this, i);
      }
   }

   public void readFromNbt(CompoundTag nbt) {
      this.statement = this.type.readFromNbt(BcNbt.getCompound(nbt, "s"));
      if (this.statement == null) {
         Arrays.fill(this.params, null);
      } else {
         for (int p = 0; p < this.params.length; p++) {
            CompoundTag pNbt = BcNbt.getCompound(nbt, Integer.toString(p));
            this.params[p] = StatementTypeParam.INSTANCE.readFromNbt(pNbt);
         }
      }
   }

   public CompoundTag writeToNbt() {
      CompoundTag nbt = new CompoundTag();
      if (this.statement != null) {
         nbt.put("s", this.type.writeToNbt(this.statement));

         for (int p = 0; p < this.params.length; p++) {
            IStatementParameter param = this.params[p];
            if (param != null) {
               nbt.put(Integer.toString(p), StatementTypeParam.INSTANCE.writeToNbt(param));
            }
         }
      }

      return nbt;
   }

   public void readFromBuffer(FriendlyByteBuf buffer) {
      if (buffer.readBoolean()) {
         this.statement = this.type.readFromBuffer(buffer);

         for (int p = 0; p < this.params.length; p++) {
            this.params[p] = StatementTypeParam.INSTANCE.readFromBuffer(buffer);
         }
      } else {
         this.statement = this.type.defaultStatement;
         Arrays.fill(this.params, null);
      }
   }

   public void writeToBuffer(FriendlyByteBuf buffer) {
      if (this.statement == null) {
         buffer.writeBoolean(false);
      } else {
         buffer.writeBoolean(true);
         this.type.writeToBuffer(buffer, this.statement);

         for (int p = 0; p < this.params.length; p++) {
            IStatementParameter param = this.params[p];
            StatementTypeParam.INSTANCE.writeToBuffer(buffer, param);
         }
      }
   }

   public S get() {
      return this.statement;
   }

   public void set(S to) {
      this.statement = to;
      if (this.statement == null) {
         Arrays.fill(this.params, null);
      } else {
         for (int i = 0; i < this.params.length; i++) {
            if (i > this.statement.maxParameters()) {
               this.params[i] = null;
            } else {
               this.params[i] = this.statement.createParameter(this.params[i], i);
            }
         }
      }
   }

   public boolean canSet(S value) {
      return value == null ? true : value.minParameters() <= this.params.length;
   }

   public S convertToType(Object value) {
      S val = (S)IReference.super.convertToType(value);
      return value != null && val == null ? this.type.convertToType(value) : val;
   }

   @Override
   public Class<S> getHeldType() {
      return this.type.clazz;
   }

   public IReference<IStatementParameter> getParamRef(int i) {
      return this.paramRefs[i];
   }

   public IStatementParameter get(int index) {
      return this.getParamRef(index).get();
   }

   public void set(int index, IStatementParameter param) {
      this.getParamRef(index).set(param);
   }

   public void set(S statement, IStatementParameter[] params) {
      this.set(statement);

      for (int i = Math.min(this.getParamCount(), params.length) - 1; i >= 0; i--) {
         this.set(i, params[i]);
      }
   }

   public boolean canSet(int index, IStatementParameter param) {
      return this.getParamRef(index).canSet(param);
   }

   public int getParamCount() {
      return this.params.length;
   }

   public IStatementParameter[] getParameters() {
      return this.params;
   }

   public void postSetFromGui(int paramIndex) {
      if (this.listener != null) {
         this.listener.onChange(this, paramIndex);
      }
   }

   @FunctionalInterface
   public interface IStatementChangeListener {
      void onChange(FullStatement<?> var1, int var2);
   }

   static class ParamRef implements IReference<IStatementParameter> {
      public final IReference<? extends IStatement> statementRef;
      public final IStatementParameter[] array;
      public final int index;

      public ParamRef(FullStatement<?> full, int index) {
         this.statementRef = (IReference<? extends IStatement>)full;
         this.array = full.params;
         this.index = index;
      }

      public IStatementParameter get() {
         return this.array[this.index];
      }

      public void set(IStatementParameter to) {
         this.array[this.index] = to;
      }

      public boolean canSet(IStatementParameter value) {
         IStatement statement = this.statementRef.get();
         return statement == null ? false : statement.createParameter(value, this.index) == value;
      }

      @Override
      public Class<IStatementParameter> getHeldType() {
         return IStatementParameter.class;
      }
   }
}
