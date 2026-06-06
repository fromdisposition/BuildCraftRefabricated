package buildcraft.lib.expression.api;

import buildcraft.lib.expression.NodeTypeBase;

public final class NodeType<T> extends NodeTypeBase<T> {
   public final Class<T> type;
   public final T defaultValue;

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
