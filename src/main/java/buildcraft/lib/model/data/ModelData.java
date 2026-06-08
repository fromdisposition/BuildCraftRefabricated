/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.model.data;

public final class ModelData {
   public static final ModelData EMPTY = new ModelData();

   public static ModelData.Builder builder() {
      return new ModelData.Builder();
   }

   public static final class Builder {
      public <T> ModelData.Builder with(ModelProperty<T> property, T value) {
         return this;
      }

      public ModelData build() {
         return ModelData.EMPTY;
      }
   }
}
