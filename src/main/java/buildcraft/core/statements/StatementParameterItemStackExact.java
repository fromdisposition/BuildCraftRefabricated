package buildcraft.core.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.lib.misc.NBTUtilBC;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class StatementParameterItemStackExact implements IStatementParameter {
   protected ItemStack stack = ItemStack.EMPTY;

   @Nonnull
   @Override
   public ItemStack getItemStack() {
      return this.stack;
   }

   public StatementParameterItemStackExact onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
      if (!stack.isEmpty()) {
         if (areItemsEqual(this.stack, stack)) {
            if (mouse.getButton() == 0) {
               this.stack.setCount(Math.min(64, this.stack.getCount() + (mouse.isShift() ? 16 : 1)));
            } else {
               this.stack.setCount(this.stack.getCount() - (mouse.isShift() ? 16 : 1));
               if (this.stack.getCount() <= 0) {
                  this.stack = ItemStack.EMPTY;
               }
            }
         } else {
            this.stack = stack.copy();
         }
      } else if (!this.stack.isEmpty()) {
         if (mouse.getButton() == 0) {
            this.stack.setCount(Math.min(64, this.stack.getCount() + (mouse.isShift() ? 16 : 1)));
         } else {
            this.stack.setCount(this.stack.getCount() - (mouse.isShift() ? 16 : 1));
            if (this.stack.getCount() <= 0) {
               this.stack = ItemStack.EMPTY;
            }
         }
      }

      return this;
   }

   @Override
   public void writeToNbt(CompoundTag compound) {
      if (!this.stack.isEmpty()) {
         ItemStack.CODEC.encodeStart(NBTUtilBC.registryAwareOps(), this.stack).resultOrPartial().ifPresent(payload -> compound.put("stack", payload));
      }
   }

   public static StatementParameterItemStackExact readFromNbt(CompoundTag nbt) {
      StatementParameterItemStackExact param = new StatementParameterItemStackExact();
      Tag stackPayload = nbt.get("stack");
      if (stackPayload != null) {
         param.stack = ItemStack.CODEC.parse(NBTUtilBC.registryAwareOps(), stackPayload).resultOrPartial().orElse(ItemStack.EMPTY);
      }

      return param;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof StatementParameterItemStackExact param ? areItemsEqual(this.stack, param.stack) : false;
   }

   private static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
      return stack1.isEmpty() ? stack2.isEmpty() : !stack2.isEmpty() && ItemStack.isSameItemSameComponents(stack1, stack2);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.stack);
   }

   @Override
   public String getDescription() {
      return !this.stack.isEmpty() ? this.stack.getHoverName().getString() : "";
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:stackExact";
   }

   @Override
   public IStatementParameter rotateLeft() {
      return this;
   }

   @Override
   public ISprite getSprite() {
      return null;
   }

   @Override
   public IStatementParameter[] getPossible(IStatementContainer source) {
      return null;
   }
}
