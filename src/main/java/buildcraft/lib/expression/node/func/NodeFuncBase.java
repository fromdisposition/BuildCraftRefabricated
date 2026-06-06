package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.INodeFunc;
import javax.annotation.Nullable;

public abstract class NodeFuncBase implements INodeFunc {
   protected boolean canInline = true;
   @Nullable
   private String deprecationMessage;
   private INodeFunc deprecationRecomendation;

   public NodeFuncBase setNeverInline() {
      this.canInline = false;
      return this;
   }

   public NodeFuncBase deprecate(String msg) {
      return this.deprecate(msg, null);
   }

   public NodeFuncBase deprecate(INodeFunc useInstead) {
      return this.deprecate(null, useInstead);
   }

   public NodeFuncBase deprecate(String msg, INodeFunc useInstead) {
      this.deprecationMessage = msg;
      this.deprecationRecomendation = useInstead;
      return this;
   }

   public boolean isDeprecated() {
      return this.deprecationMessage != null || this.deprecationRecomendation != null;
   }

   @Nullable
   public String getDeprecationMessage() {
      return this.deprecationMessage;
   }

   @Nullable
   public INodeFunc getDeprecationRecomendation() {
      return this.deprecationRecomendation;
   }

   public interface IFunctionNode {
      NodeFuncBase getFunction();
   }
}
