package buildcraft.lib.client.render.laser;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;

public class CompiledLaserType {
   public final LaserData_BC8.LaserType type;
   private final CompiledLaserRow startCap;
   private final CompiledLaserRow endCap;
   private final CompiledLaserRow start;
   private final CompiledLaserRow end;
   private final double startWidth;
   private final double middleWidth;
   private final double endWidth;
   private final Map<LaserData_BC8.LaserSide, CompiledLaserRow> rows = new EnumMap<>(LaserData_BC8.LaserSide.class);

   public CompiledLaserType(LaserData_BC8.LaserType type) {
      this.type = type;
      this.startCap = new CompiledLaserRow(type.capStart);
      this.endCap = new CompiledLaserRow(type.capEnd);
      this.start = type.start == null ? null : new CompiledLaserRow(type.start);
      this.end = type.end == null ? null : new CompiledLaserRow(type.end);
      this.startWidth = this.start == null ? 0.0 : this.start.width;
      this.endWidth = this.end == null ? 0.0 : this.end.width;

      for (LaserData_BC8.LaserSide side : LaserData_BC8.LaserSide.VALUES) {
         List<LaserData_BC8.LaserRow> validRows = new ArrayList<>();

         for (LaserData_BC8.LaserRow row : type.variations) {
            for (LaserData_BC8.LaserSide inner : row.validSides) {
               if (inner == side) {
                  validRows.add(row);
                  break;
               }
            }
         }

         this.rows.put(side, new CompiledLaserRow(validRows.toArray(new LaserData_BC8.LaserRow[validRows.size()])));
      }

      this.middleWidth = this.rows.get(LaserData_BC8.LaserSide.BOTTOM).width;
   }

   public void bakeFor(LaserContext context) {
      this.startCap.bakeStartCap(context);
      this.endCap.bakeEndCap(context);
      double lengthForMiddle = Math.max(0.0, context.length - this.startWidth - this.endWidth);
      int numMiddle = Mth.floor(lengthForMiddle / this.middleWidth);
      double leftOverFromMiddle = lengthForMiddle - this.middleWidth * numMiddle;
      if (leftOverFromMiddle > 0.0) {
         numMiddle++;
      }

      double lengthEnds = context.length - this.middleWidth * numMiddle;
      double startLength;
      double endLength;
      if (this.startWidth > 0.0 && this.endWidth > 0.0) {
         double ratioStartEnd = this.startWidth / this.endWidth;
         startLength = lengthEnds / 2.0 * ratioStartEnd;
         endLength = lengthEnds / 2.0 / ratioStartEnd;
      } else if (this.startWidth <= 0.0) {
         startLength = 0.0;
         endLength = lengthEnds;
      } else {
         startLength = lengthEnds;
         endLength = 0.0;
      }

      if (startLength > 0.0) {
         this.start.bakeStart(context, startLength);
      }

      if (endLength > 0.0) {
         this.end.bakeEnd(context, endLength);
      }

      if (numMiddle > 0) {
         for (LaserData_BC8.LaserSide side : LaserData_BC8.LaserSide.VALUES) {
            CompiledLaserRow interp = this.rows.get(side);
            interp.bakeFor(context, side, startLength, numMiddle);
         }
      }
   }
}
