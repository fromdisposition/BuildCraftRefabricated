/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;

public class WireNode {
   public final BlockPos pos;
   public final EnumWirePart part;
   private final int hash;

   public WireNode(BlockPos pos, EnumWirePart part) {
      this.pos = pos;
      this.part = part;
      this.hash = pos.hashCode() * 31 + part.hashCode();
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (this.getClass() != obj.getClass()) {
         return false;
      }

      WireNode other = (WireNode)obj;
      return this.part == other.part && this.pos.equals(other.pos);
   }

   @Override
   public String toString() {
      return "(" + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ() + ", " + this.part + ")";
   }

   public WireNode offset(Direction face) {
      int nx = (this.part.x == AxisDirection.POSITIVE ? 1 : 0) + face.getStepX();
      int ny = (this.part.y == AxisDirection.POSITIVE ? 1 : 0) + face.getStepY();
      int nz = (this.part.z == AxisDirection.POSITIVE ? 1 : 0) + face.getStepZ();
      EnumWirePart nPart = EnumWirePart.get(nx, ny, nz);
      return nx >= 0 && ny >= 0 && nz >= 0 && nx <= 1 && ny <= 1 && nz <= 1 ? new WireNode(this.pos, nPart) : new WireNode(this.pos.relative(face), nPart);
   }

   public Map<Direction, WireNode> getAllPossibleConnections() {
      Map<Direction, WireNode> map = new EnumMap<>(Direction.class);

      for (Direction face : Direction.values()) {
         map.put(face, this.offset(face));
      }

      return map;
   }
}
