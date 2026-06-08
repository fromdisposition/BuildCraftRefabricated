/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.api;

import buildcraft.lib.expression.NodeTypeBase;

public final class NodeType<T> extends NodeTypeBase<T> {
   public final Class<T> type;
   public final T defaultValue;

   @SuppressWarnings("unchecked")
   public NodeType(String name, T defaultValue) {
      this(name, (Class<T>)defaultValue.getClass(), defaultValue);
   }

   public NodeType(String name, Class<T> type, T defaultValue) {
      super(name);
      this.type = type;
      this.defaultValue = defaultValue;
   }

   @Override
   protected Class<T> getType() {
      return this.type;
   }

   @Override
   public int hashCode() {
      return this.type.hashCode();
   }

   @Override
      @SuppressWarnings("unchecked")
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return obj.getClass() != this.getClass() ? false : this.type == ((NodeType)obj).type;
      }
   }

   public void putConstant(String name, T value) {
      this.putConstant(name, this.type, value);
   }
}
