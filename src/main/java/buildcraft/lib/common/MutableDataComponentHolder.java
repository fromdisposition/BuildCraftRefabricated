/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.common;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
//? if >= 1.21.10 {
import net.minecraft.core.component.DataComponentGetter;
//?}
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.jspecify.annotations.Nullable;

public interface MutableDataComponentHolder extends DataComponentHolder {
   <T> @Nullable T set(DataComponentType<T> var1, @Nullable T var2);

   default <T> @Nullable T set(Supplier<? extends DataComponentType<T>> componentType, @Nullable T value) {
      return this.set((DataComponentType<T>)componentType.get(), value);
   }

   //? if >= 1.21.10 {
   default <T> void copyFrom(DataComponentType<T> type, DataComponentGetter getter) {
      this.set(type, getter.get(type));
   }

   default <T> void copyFrom(Supplier<? extends DataComponentType<T>> type, DataComponentGetter getter) {
      this.copyFrom((DataComponentType<T>)type.get(), getter);
   }
   //?}

   default <T, U> @Nullable T update(DataComponentType<T> componentType, T value, U updateContext, BiFunction<T, U, T> updater) {
      return this.set(componentType, updater.apply((T)this.getOrDefault(componentType, value), updateContext));
   }

   default <T, U> @Nullable T update(Supplier<? extends DataComponentType<T>> componentType, T value, U updateContext, BiFunction<T, U, T> updater) {
      return this.update((DataComponentType<T>)componentType.get(), value, updateContext, updater);
   }

   default <T> @Nullable T update(DataComponentType<T> componentType, T value, UnaryOperator<T> updater) {
      return this.set(componentType, updater.apply((T)this.getOrDefault(componentType, value)));
   }

   default <T> @Nullable T update(Supplier<? extends DataComponentType<T>> componentType, T value, UnaryOperator<T> updater) {
      return this.update((DataComponentType<T>)componentType.get(), value, updater);
   }

   <T> @Nullable T remove(DataComponentType<? extends T> var1);

   default <T> @Nullable T remove(Supplier<? extends DataComponentType<? extends T>> componentType) {
      return this.remove((DataComponentType<? extends T>)componentType.get());
   }

   default void copyFrom(DataComponentHolder src, DataComponentType<?>... componentTypes) {
      for (DataComponentType<?> componentType : componentTypes) {
         this.copyFrom(componentType, src);
      }
   }

   @SuppressWarnings("unchecked")
   default void copyFrom(DataComponentHolder src, Supplier<? extends DataComponentType<?>>... componentTypes) {
      for (Supplier<? extends DataComponentType<?>> componentType : componentTypes) {
         this.copyFrom(componentType.get(), src);
      }
   }

   void applyComponents(DataComponentPatch var1);

   void applyComponents(DataComponentMap var1);

   private <T> void copyFrom(DataComponentType<T> componentType, DataComponentHolder src) {
      this.set(componentType, src.get(componentType));
   }
}
