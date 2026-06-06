package buildcraft.lib.misc.data;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.ImmutableTable.Builder;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;

public class AxisOrder {
   private static final Table<EnumAxisOrder, AxisOrder.Inversion, AxisOrder> allOrders;
   public final EnumAxisOrder order;
   public final AxisOrder.Inversion inversion;
   public final Direction first;
   public final Direction second;
   public final Direction third;

   private AxisOrder(EnumAxisOrder order, AxisOrder.Inversion inv) {
      this.order = order;
      this.inversion = inv;
      this.first = Direction.fromAxisAndDirection(order.first, inv.first);
      this.second = Direction.fromAxisAndDirection(order.second, inv.second);
      this.third = Direction.fromAxisAndDirection(order.third, inv.third);
   }

   public static AxisOrder readNbt(CompoundTag nbt) {
      return getFor(EnumAxisOrder.getOrder(nbt.getStringOr("order", "")), AxisOrder.Inversion.getFor(nbt.getStringOr("inversion", "")));
   }

   public CompoundTag writeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.putString("order", this.order.name());
      nbt.putString("inversion", this.inversion.name());
      return nbt;
   }

   public static AxisOrder getFor(EnumAxisOrder order, AxisOrder.Inversion inv) {
      if (order == null) {
         throw new NullPointerException("order");
      } else if (inv == null) {
         throw new NullPointerException("inv");
      } else {
         AxisOrder axisOrder = (AxisOrder)allOrders.get(order, inv);
         if (axisOrder == null) {
            throw new IllegalStateException("Tried to lookup " + order + ", " + inv + " but failed!");
         } else {
            return axisOrder;
         }
      }
   }

   @Override
   public String toString() {
      return this.first + ", " + this.second + ", " + this.third;
   }

   public AxisOrder invertFirst() {
      return (AxisOrder)allOrders.get(this.order, AxisOrder.Inversion.getFor(this.first.getOpposite(), this.second, this.third));
   }

   public AxisOrder invertSecond() {
      return (AxisOrder)allOrders.get(this.order, AxisOrder.Inversion.getFor(this.first, this.second.getOpposite(), this.third));
   }

   public AxisOrder invertThird() {
      return (AxisOrder)allOrders.get(this.order, AxisOrder.Inversion.getFor(this.first, this.second, this.third.getOpposite()));
   }

   public AxisOrder invert(Axis axis) {
      if (axis == this.first.getAxis()) {
         return this.invertFirst();
      } else {
         return axis == this.second.getAxis() ? this.invertSecond() : this.invertThird();
      }
   }

   static {
      Builder<EnumAxisOrder, AxisOrder.Inversion, AxisOrder> builder = ImmutableTable.builder();

      for (EnumAxisOrder order : EnumAxisOrder.VALUES) {
         for (AxisOrder.Inversion inv : AxisOrder.Inversion.VALUES) {
            builder.put(order, inv, new AxisOrder(order, inv));
         }
      }

      allOrders = builder.build();
   }

   public enum Inversion {
      PPP,
      PPN,
      PNP,
      PNN,
      NPP,
      NPN,
      NNP,
      NNN;

      public static final AxisOrder.Inversion[] VALUES = values();
      public final AxisDirection first = getFor(this.name().charAt(0));
      public final AxisDirection second = getFor(this.name().charAt(1));
      public final AxisDirection third = getFor(this.name().charAt(2));

      private static AxisDirection getFor(char charAt) {
         if (charAt == 'P') {
            return AxisDirection.POSITIVE;
         } else if (charAt == 'N') {
            return AxisDirection.NEGATIVE;
         } else {
            throw new Error("Unknown char " + charAt);
         }
      }

      public static AxisOrder.Inversion getFor(Direction first, Direction second, Direction third) {
         return getFor(first.getAxisDirection(), second.getAxisDirection(), third.getAxisDirection());
      }

      public static AxisOrder.Inversion getFor(AxisDirection first, AxisDirection second, AxisDirection third) {
         return getFor(first == AxisDirection.POSITIVE, second == AxisDirection.POSITIVE, third == AxisDirection.POSITIVE);
      }

      public static AxisOrder.Inversion getFor(boolean first, boolean second, boolean third) {
         if (first) {
            if (second) {
               return third ? PPP : PPN;
            } else {
               return third ? PNP : PNN;
            }
         } else if (second) {
            return third ? NPP : NPN;
         } else {
            return third ? NNP : NNN;
         }
      }

      public static AxisOrder.Inversion getFor(String name) {
         if (name != null && name.length() == 3) {
            boolean first = name.charAt(0) == 'P';
            boolean second = name.charAt(1) == 'P';
            boolean third = name.charAt(2) == 'P';
            return getFor(first, second, third);
         } else {
            return PPP;
         }
      }
   }
}
