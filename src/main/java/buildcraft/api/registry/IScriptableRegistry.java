/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.registry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IScriptableRegistry<E> extends IReloadableRegistry<E> {
   String getEntryType();

   Map<String, Class<? extends E>> getScriptableTypes();

   Map<String, IScriptableRegistry.IEntryDeserializer<? extends E>> getCustomDeserializers();

   default void addSimpleType(String name, Class<? extends E> type) {
      this.getScriptableTypes().put(name, type);
   }

   default void addCustomType(String name, IScriptableRegistry.IEntryDeserializer<? extends E> deserializer) {
      this.getCustomDeserializers().put(name, deserializer);
   }

   Set<String> getSourceDomains();

   @FunctionalInterface
   interface IEntryDeserializer<E> {
      IScriptableRegistry.OptionallyDisabled<E> deserialize(Object var1, JsonObject var2, JsonDeserializationContext var3) throws JsonSyntaxException;
   }

   @FunctionalInterface
   interface ISimpleEntryDeserializer<E> extends IScriptableRegistry.IEntryDeserializer<E> {
      @Override
      default IScriptableRegistry.OptionallyDisabled<E> deserialize(Object name, JsonObject obj, JsonDeserializationContext ctx) throws JsonSyntaxException {
         return new IScriptableRegistry.OptionallyDisabled<>(this.deserializeConst(name, obj, ctx));
      }

      E deserializeConst(Object var1, JsonObject var2, JsonDeserializationContext var3) throws JsonSyntaxException;
   }

   final class OptionallyDisabled<E> {
      @Nullable
      private final E object;
      @Nullable
      private final String reason;

      public OptionallyDisabled(E object) {
         this.object = object;
         this.reason = null;
      }

      public OptionallyDisabled(String reason) {
         this.object = null;
         this.reason = reason;
      }

      public boolean isPresent() {
         return this.object != null;
      }

      @Nonnull
      public E get() {
         E o = this.object;
         if (o != null) {
            return o;
         } else {
            throw new IllegalStateException("This object has been disabled! You must call isPresent() first!");
         }
      }

      @Nonnull
      public String getDisabledReason() {
         String r = this.reason;
         if (r != null) {
            return r;
         } else {
            throw new IllegalStateException("This object has not been disabled! You must call isPresent() first!");
         }
      }
   }
}
