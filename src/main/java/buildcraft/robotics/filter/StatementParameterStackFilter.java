package buildcraft.robotics.filter;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Builds a stack filter from the item-stack parameters of a gate action slot. */
public class StatementParameterStackFilter extends ArrayStackFilter {
   public StatementParameterStackFilter(IStatementParameter... parameters) {
      super(collect(parameters));
   }

   private static ItemStack[] collect(IStatementParameter... parameters) {
      List<ItemStack> tmp = new ArrayList<>();
      if (parameters != null) {
         for (IStatementParameter param : parameters) {
            if (param instanceof StatementParameterItemStack stackParam) {
               tmp.add(stackParam.getItemStack());
            }
         }
      }

      return tmp.toArray(new ItemStack[0]);
   }
}
