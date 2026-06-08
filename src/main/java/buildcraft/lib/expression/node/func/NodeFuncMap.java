/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.NodeInliningHelper;
import buildcraft.lib.expression.api.IConstantNode;
import buildcraft.lib.expression.api.IDependancyVisitor;
import buildcraft.lib.expression.api.IDependantNode;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

public class NodeFuncMap<K extends Enum<K>, V> implements INodeFunc.INodeFuncObject<V> {
   private final Class<K> keyClass;
   private final Class<V> valueClass;
   private final Map<K, NodeVariableObject<V>> variableMap;
   private final NodeVariableObject<V> nullEntry;

   public NodeFuncMap(Class<K> keyClass, Class<V> valueClass) {
      this.keyClass = keyClass;
      this.valueClass = valueClass;
      this.variableMap = new EnumMap<>(keyClass);

      for (K enumKey : keyClass.getEnumConstants()) {
         this.variableMap.put(enumKey, new NodeVariableObject<>("entry_" + enumKey.name(), valueClass));
      }

      this.nullEntry = new NodeVariableObject<>("null_entry", valueClass);
   }

   public void putAll(Map<K, V> map) {
      for (Entry<K, V> entry : map.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }
   }

   public void putAll(V value) {
      this.nullEntry.set(value);

      for (K key : this.keyClass.getEnumConstants()) {
         this.put(key, value);
      }
   }

   public void put(K key, V value) {
      if (key == null) {
         this.nullEntry.set(value);
      } else {
         IVariableNode.IVariableNodeObject<V> node = this.variableMap.get(key);
         if (node == null) {
            throw new IllegalArgumentException("Unknown enum key " + key + " for " + key.getClass());
         }

         node.set(value);
      }
   }

   public NodeVariableObject<V> get(K key) {
      if (key == null) {
         return this.nullEntry;
      } else {
         NodeVariableObject<V> node = this.variableMap.get(key);
         if (node == null) {
            throw new IllegalArgumentException("Unknown enum key " + key + " for " + key.getClass());
         } else {
            return node;
         }
      }
   }

   @Override
   public IExpressionNode.INodeObject<V> getNode(INodeStack stack) throws InvalidExpressionException {
      return this.new Node(stack.popObject(this.keyClass));
   }

   @Override
   public Class<V> getType() {
      return this.valueClass;
   }

   private class Node implements IExpressionNode.INodeObject<V>, IDependantNode {
      private final IExpressionNode.INodeObject<K> input;

      public Node(IExpressionNode.INodeObject<K> input) {
         this.input = input;
      }

      @Override
      public void visitDependants(IDependancyVisitor visitor) {
         visitor.dependOn((IExpressionNode)NodeFuncMap.this.nullEntry);
         visitor.dependOnNodes(NodeFuncMap.this.variableMap.values());
      }

      @Override
      public V evaluate() {
         return NodeFuncMap.this.get(this.input.evaluate()).evaluate();
      }

      @Override
      public Class<V> getType() {
         return NodeFuncMap.this.valueClass;
      }

      @Override
      public IExpressionNode.INodeObject<V> inline() {
         return NodeInliningHelper.tryInline(this, this.input, x$0 -> NodeFuncMap.this.new Node(x$0), i -> {
            assert i instanceof IConstantNode;
            K key = i.evaluate();
            return NodeFuncMap.this.get(key).inline();
         });
      }
   }
}
