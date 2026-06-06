package buildcraft.api.statements;

import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public interface IStatementParameter extends IGuiSlot {
   @Nonnull
   ItemStack getItemStack();

   default IStatementParameter.DrawType getDrawType() {
      return IStatementParameter.DrawType.SPRITE_STACK;
   }

   IStatementParameter onClick(IStatementContainer var1, IStatement var2, ItemStack var3, StatementMouseClick var4);

   void writeToNbt(CompoundTag var1);

   default void writeToBuf(FriendlyByteBuf buffer) {
      CompoundTag nbt = new CompoundTag();
      this.writeToNbt(nbt);
      buffer.writeNbt(nbt);
   }

   IStatementParameter rotateLeft();

   IStatementParameter[] getPossible(IStatementContainer var1);

   default boolean isPossibleOrdered() {
      return false;
   }

   enum DrawType {
      SPRITE_ONLY,
      STACK_ONLY,
      STACK_ONLY_OR_QUESTION_MARK,
      SPRITE_STACK,
      SPRITE_STACK_OR_QUESTION_MARK,
      STACK_SPRITE,
      STACK_OR_QUESTION_MARK_THEN_SPRITE;
   }
}
