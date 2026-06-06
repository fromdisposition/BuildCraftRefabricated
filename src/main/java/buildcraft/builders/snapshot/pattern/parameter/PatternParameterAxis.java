package buildcraft.builders.snapshot.pattern.parameter;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementMouseClick;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StackUtil;
import java.util.Locale;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public enum PatternParameterAxis implements IStatementParameter {
   X(Axis.X),
   Y(Axis.Y),
   Z(Axis.Z);

   public final Axis axis;

   PatternParameterAxis(Axis axis) {
      this.axis = axis;
   }

   public static PatternParameterAxis readFromNbt(CompoundTag nbt) {
      byte ord = nbt.getByte("a").orElse((byte)0);
      if (ord <= 0) {
         return X;
      } else {
         return ord >= 2 ? Z : Y;
      }
   }

   @Override
   public String getUniqueTag() {
      return "buildcraft:filler_parameter_axis";
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize("buildcraft.param.axis." + this.name().toLowerCase(Locale.ROOT));
   }

   @Override
   public ISprite getSprite() {
      return BCBuildersSprites.PARAM_AXIS.get(this.axis);
   }

   @Nonnull
   @Override
   public ItemStack getItemStack() {
      return StackUtil.EMPTY;
   }

   @Override
   public IStatementParameter onClick(IStatementContainer source, IStatement stmt, ItemStack stack, StatementMouseClick mouse) {
      return null;
   }

   @Override
   public void writeToNbt(CompoundTag nbt) {
      nbt.putByte("a", (byte)this.ordinal());
   }

   @Override
   public IStatementParameter rotateLeft() {
      switch (this) {
         case X:
            return Z;
         case Y:
            return Y;
         case Z:
            return X;
         default:
            throw new IllegalStateException("Unknown axis " + this);
      }
   }

   @Override
   public IStatementParameter[] getPossible(IStatementContainer source) {
      return values();
   }
}
