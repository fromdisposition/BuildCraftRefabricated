package buildcraft.lib.misc.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import net.minecraft.core.Direction.Axis;

public enum EnumAxisOrder {
   XYZ(Axis.X, Axis.Y, Axis.Z),
   XZY(Axis.X, Axis.Z, Axis.Y),
   YXZ(Axis.Y, Axis.X, Axis.Z),
   YZX(Axis.Y, Axis.Z, Axis.X),
   ZXY(Axis.Z, Axis.X, Axis.Y),
   ZYX(Axis.Z, Axis.Y, Axis.X);

   public static final EnumAxisOrder[] VALUES = values();
   private static final Map<String, EnumAxisOrder> orderMap;
   public final Axis first;
   public final Axis second;
   public final Axis third;

   EnumAxisOrder(Axis a, Axis b, Axis c) {
      this.first = a;
      this.second = b;
      this.third = c;
   }

   public static EnumAxisOrder getOrder(String name) {
      EnumAxisOrder order = orderMap.get(name);
      if (order == null) {
         order = XZY;
      }

      return order;
   }

   public static EnumAxisOrder getOrder(Axis first, Axis b) {
      if (first == Axis.X) {
         return b == Axis.Y ? XYZ : XZY;
      } else if (first == Axis.Y) {
         return b == Axis.X ? YXZ : YZX;
      } else {
         return b == Axis.X ? ZXY : ZYX;
      }
   }

   public AxisOrder getMinToMaxOrder() {
      return AxisOrder.getFor(this, AxisOrder.Inversion.PPP);
   }

   public AxisOrder getMaxToMinOrder() {
      return AxisOrder.getFor(this, AxisOrder.Inversion.NNN);
   }

   static {
      Builder<String, EnumAxisOrder> builder = ImmutableMap.builder();

      for (EnumAxisOrder order : values()) {
         builder.put(order.name(), order);
      }

      orderMap = builder.build();
   }
}
