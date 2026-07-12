/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.statements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class StatementManager {
   public static Map<String, IStatement> statements = new HashMap<>();
   public static Map<String, StatementManager.IParameterReader> parameters = new HashMap<>();
   public static Map<String, StatementManager.IParamReaderBuf> paramsBuf = new HashMap<>();
   private static List<ITriggerProvider> triggerProviders = new LinkedList<>();
   private static List<IActionProvider> actionProviders = new LinkedList<>();

   private StatementManager() {
   }

   public static void registerTriggerProvider(ITriggerProvider provider) {
      if (provider != null && !triggerProviders.contains(provider)) {
         triggerProviders.add(provider);
      }
   }

   public static void registerActionProvider(IActionProvider provider) {
      if (provider != null && !actionProviders.contains(provider)) {
         actionProviders.add(provider);
      }
   }

   public static void registerStatement(IStatement statement) {
      statements.put(statement.getUniqueTag(), statement);
   }

   public static void registerParameter(StatementManager.IParameterReader reader) {
      registerParameter(reader, buf -> reader.readFromNbt(buf.readNbt()));
   }

   public static void registerParameter(StatementManager.IParameterReader reader, StatementManager.IParamReaderBuf bufReader) {
      String name = reader.readFromNbt(new CompoundTag()).getUniqueTag();
      registerParameter(name, reader);
      registerParameterBuf(name, bufReader);
   }

   public static void registerParameter(String name, StatementManager.IParameterReader reader) {
      parameters.put(name, reader);
   }

   public static void registerParameterBuf(String name, StatementManager.IParamReaderBuf reader) {
      paramsBuf.put(name, reader);
   }

   public static List<ITriggerExternal> getExternalTriggers(Direction side, BlockEntity entity) {
      if (entity instanceof IOverrideDefaultStatements) {
         List<ITriggerExternal> result = ((IOverrideDefaultStatements)entity).overrideTriggers();
         if (result != null) {
            return result;
         }
      }

      LinkedHashSet<ITriggerExternal> triggers = new LinkedHashSet<>();

      for (ITriggerProvider provider : triggerProviders) {
         provider.addExternalTriggers(triggers, side, entity);
      }

      return new ArrayList<>(triggers);
   }

   public static List<IActionExternal> getExternalActions(Direction side, BlockEntity entity) {
      if (entity instanceof IOverrideDefaultStatements) {
         List<IActionExternal> result = ((IOverrideDefaultStatements)entity).overrideActions();
         if (result != null) {
            return result;
         }
      }

      LinkedHashSet<IActionExternal> actions = new LinkedHashSet<>();

      for (IActionProvider provider : actionProviders) {
         provider.addExternalActions(actions, side, entity);
      }

      return new ArrayList<>(actions);
   }

   public static List<ITriggerInternal> getInternalTriggers(IStatementContainer container) {
      LinkedHashSet<ITriggerInternal> triggers = new LinkedHashSet<>();

      for (ITriggerProvider provider : triggerProviders) {
         provider.addInternalTriggers(triggers, container);
      }

      return new ArrayList<>(triggers);
   }

   public static List<IActionInternal> getInternalActions(IStatementContainer container) {
      LinkedHashSet<IActionInternal> actions = new LinkedHashSet<>();

      for (IActionProvider provider : actionProviders) {
         provider.addInternalActions(actions, container);
      }

      return new ArrayList<>(actions);
   }

   public static List<ITriggerInternalSided> getInternalSidedTriggers(IStatementContainer container, Direction side) {
      LinkedHashSet<ITriggerInternalSided> triggers = new LinkedHashSet<>();

      for (ITriggerProvider provider : triggerProviders) {
         provider.addInternalSidedTriggers(triggers, container, side);
      }

      return new ArrayList<>(triggers);
   }

   public static List<IActionInternalSided> getInternalSidedActions(IStatementContainer container, Direction side) {
      LinkedHashSet<IActionInternalSided> actions = new LinkedHashSet<>();

      for (IActionProvider provider : actionProviders) {
         provider.addInternalSidedActions(actions, container, side);
      }

      return new ArrayList<>(actions);
   }

   public static StatementManager.IParameterReader getParameterReader(String kind) {
      return parameters.get(kind);
   }

   static {
      registerParameter(StatementParameterItemStack::new);
   }

   @FunctionalInterface
   public interface IParamReaderBuf {
      IStatementParameter readFromBuf(FriendlyByteBuf var1);
   }

   @FunctionalInterface
   public interface IParameterReader {
      IStatementParameter readFromNbt(CompoundTag var1);
   }
}
