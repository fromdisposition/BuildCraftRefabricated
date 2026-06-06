package buildcraft.lib.misc.collect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

public class TypedMapHierarchy<V> implements TypedMap<V> {
   private final Map<Class<?>, TypedMapHierarchy.Node<?>> nodes = new HashMap<>();

   @Override
   public <T extends V> T get(Class<T> clazz) {
      TypedMapHierarchy.Node<?> node = this.nodes.get(clazz);
      return node == null ? null : clazz.cast(node.getFirstValue());
   }

   @Override
   public void put(V value) {
      Class<?> clazz = value.getClass();
      TypedMapHierarchy.Node<?> node = this.nodes.get(clazz);
      if (node == null) {
         node = this.putNode(clazz);
      }

      node.setValue(value);
   }

   @Nullable
   private TypedMapHierarchy.Node<?> getNode(Class<?> clazz) {
      return this.nodes.get(clazz);
   }

   private <T> TypedMapHierarchy.Node<T> putNode(Class<T> clazz) {
      TypedMapHierarchy.Node<T> node = new TypedMapHierarchy.Node<>(clazz);
      this.nodes.put(clazz, node);

      for (Class<?> cls : getAllDirectParents(clazz)) {
         TypedMapHierarchy.Node<?> oNode = this.nodes.get(cls);
         if (oNode == null) {
            oNode = this.putNode(cls);
         }

         oNode.children.add(node);
         node.parents.add(oNode);
      }

      return node;
   }

   private static <T> List<Class<?>> getAllDirectParents(Class<T> clazz) {
      List<Class<?>> list = new ArrayList<>();
      Class<? super T> s = clazz.getSuperclass();
      if (s != null) {
         list.add(s);
      }

      Collections.addAll(list, clazz.getInterfaces());
      return list;
   }

   @Override
   public void clear() {
      this.nodes.clear();
   }

   @Override
   public void remove(V value) {
      Class<?> clazz = value.getClass();
      TypedMapHierarchy.Node<?> node = this.getNode(clazz);
      if (node != null && Objects.equals(value, node.value)) {
         node.value = null;
         this.removeNode(node);
      }
   }

   private <T> void removeNode(TypedMapHierarchy.Node<T> node) {
      if (node.children.isEmpty()) {
         this.nodes.remove(node.clazz);

         for (TypedMapHierarchy.Node<?> p : node.parents) {
            p.children.remove(node);
            this.removeNode(p);
         }
      }
   }

   static class Node<T> {
      final Class<T> clazz;
      final List<TypedMapHierarchy.Node<?>> parents = new ArrayList<>();
      final List<TypedMapHierarchy.Node<?>> children = new ArrayList<>();
      T value;

      Node(Class<T> clazz) {
         this.clazz = clazz;
      }

      void setValue(Object newValue) {
         this.value = this.clazz.cast(newValue);
      }

      T getFirstValue() {
         if (this.value != null) {
            return this.value;
         }

         for (TypedMapHierarchy.Node<?> child : this.children) {
            Object val = child.getFirstValue();
            if (val != null) {
               return this.clazz.cast(val);
            }
         }

         return null;
      }
   }
}
