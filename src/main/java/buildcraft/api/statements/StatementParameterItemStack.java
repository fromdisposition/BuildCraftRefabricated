package buildcraft.api.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.lib.misc.NBTUtilBC;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class StatementParameterItemStack implements IStatementParameter {
   @Nonnull
   private static final ItemStack EMPTY_STACK;
   public static final StatementParameterItemStack EMPTY;
   @Nonnull
   protected final ItemStack stack;

   public StatementParameterItemStack() {
      this.stack = EMPTY_STACK;
   }

   public StatementParameterItemStack(@Nonnull ItemStack stack) {
      this.stack = stack;
   }

   public StatementParameterItemStack(CompoundTag nbt) {
      ItemStack read = ItemStack.EMPTY;
      Tag stackPayload = nbt.get("stack");
      if (stackPayload != null) {
         read = ItemStack.CODEC.parse(NBTUtilBC.registryAwareOps(), stackPayload).resultOrPartial().orElse(ItemStack.EMPTY);
      }

      this.stack = read.isEmpty() ? EMPTY_STACK : read;
   }

   @Override
   public void writeToNbt(CompoundTag compound) {
      if (!this.stack.isEmpty()) {
         ItemStack.CODEC.encodeStart(NBTUtilBC.registryAwareOps(), this.stack).resultOrPartial().ifPresent(payload -> compound.put("stack", payload));
      }
   }

   @Override
   public ISprite getSprite() {
      return null;
   }

   @Nonnull
   @Override
   public ItemStack getItemStack() {
      return this.stack;
   }

   public StatementParameterItemStack onClick(IStatementContainer source, IStatement stmt, ItemStack clickedStack, StatementMouseClick mouseClick) {
      if (this.stack.isEmpty()) {
         return EMPTY;
      }

      ItemStack newStack = this.stack.copy();
      newStack.setCount(1);
      return new StatementParameterItemStack(newStack);
   }

   @Override
   public boolean equals(Object object) {
      return !(object instanceof StatementParameterItemStack param)
         ? false
         : ItemStack.isSameItem(this.stack, param.stack) && ItemStack.isSameItemSameComponents(this.stack, param.stack);
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.stack);
   }

   @Override
   public String getDescription() {
      throw new UnsupportedOperationException("Don't call getDescription directly!");
   }

   @Override
   public List<String> getTooltip() {
      return this.stack.isEmpty() ? ImmutableList.of() : ImmutableList.of(this.stack.getHoverName().getString());
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:stack";
   }

   @Override
   public IStatementParameter rotateLeft() {
      return this;
   }

   @Override
   public IStatementParameter[] getPossible(IStatementContainer source) {
      return null;
   }

   static {
      ItemStack stack = ItemStack.EMPTY;
      if (stack == null) {
         throw new Error("Somehow ItemStack.EMPTY was null!");
      }

      EMPTY_STACK = stack;
      EMPTY = new StatementParameterItemStack();
   }
}
