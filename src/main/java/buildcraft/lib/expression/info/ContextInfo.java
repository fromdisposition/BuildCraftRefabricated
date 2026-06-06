package buildcraft.lib.expression.info;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import java.util.HashMap;
import java.util.Map;

public class ContextInfo {
   public final FunctionContext fnCtx;
   public final Map<String, VariableInfo<?>> variables = new HashMap<>();

   public ContextInfo(FunctionContext fnCtx) {
      this.fnCtx = fnCtx;
   }

   public <T> VariableInfo.VariableInfoObject<T> createInfoObject(NodeVariableObject<T> node) {
      VariableInfo.VariableInfoObject<T> info = new VariableInfo.VariableInfoObject<>(node);
      this.variables.put(node.name, info);
      return info;
   }

   public VariableInfo.VariableInfoObject<?> getInfoObject(String name) {
      VariableInfo<?> info = this.variables.get(name);
      return info instanceof VariableInfo.VariableInfoObject ? (VariableInfo.VariableInfoObject)info : null;
   }

   public VariableInfo.VariableInfoDouble createInfoDouble(NodeVariableDouble node) {
      VariableInfo.VariableInfoDouble info = new VariableInfo.VariableInfoDouble(node);
      this.variables.put(node.name, info);
      return info;
   }

   public VariableInfo.VariableInfoDouble getInfoDouble(String name) {
      VariableInfo<?> info = this.variables.get(name);
      return info instanceof VariableInfo.VariableInfoDouble ? (VariableInfo.VariableInfoDouble)info : null;
   }

   public VariableInfo.VariableInfoLong createInfoLong(NodeVariableLong node) {
      VariableInfo.VariableInfoLong info = new VariableInfo.VariableInfoLong(node);
      this.variables.put(node.name, info);
      return info;
   }

   public VariableInfo.VariableInfoLong getInfoLong(String name) {
      VariableInfo<?> info = this.variables.get(name);
      return info instanceof VariableInfo.VariableInfoLong ? (VariableInfo.VariableInfoLong)info : null;
   }

   public VariableInfo.VariableInfoBoolean createInfoBoolean(NodeVariableBoolean node) {
      VariableInfo.VariableInfoBoolean info = new VariableInfo.VariableInfoBoolean(node);
      this.variables.put(node.name, info);
      return info;
   }

   public VariableInfo.VariableInfoBoolean getInfoBoolean(String name) {
      VariableInfo<?> info = this.variables.get(name);
      return info instanceof VariableInfo.VariableInfoBoolean ? (VariableInfo.VariableInfoBoolean)info : null;
   }
}
