/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.filler.IFilledTemplate;
import buildcraft.lib.misc.VecUtil;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;

public class Template extends Snapshot {
   public BitSet data;

   public Template copy() {
      Template template = new Template();
      template.size = this.size;
      template.facing = this.facing;
      template.offset = this.offset;
      template.data = (BitSet)this.data.clone();
      template.computeKey();
      return template;
   }

   public Template.FilledTemplate getFilledTemplate() {
      return new Template.FilledTemplate();
   }

   public void invert() {
      this.data.flip(0, this.getDataSize());
   }

   @Override
   public CompoundTag serializeNBT() {
      CompoundTag nbt = super.serializeNBT();
      nbt.putByteArray("data", this.data.toByteArray());
      return nbt;
   }

   @Override
   public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
      super.deserializeNBT(nbt);
      //? if >= 1.21.10 {
      this.data = BitSet.valueOf(nbt.getByteArray("data").orElse(new byte[0]));
      //?} else {
      /*this.data = BitSet.valueOf(nbt.getByteArray("data"));
      *///?}
      if (this.data.length() > this.getDataSize()) {
         throw new InvalidInputDataException(
            "Serialized data has length of " + this.data.length() + ", but we expected at most " + this.getDataSize() + " (" + this.size.toString() + ")"
         );
      }
   }

   @Override
   public EnumSnapshotType getType() {
      return EnumSnapshotType.TEMPLATE;
   }

   @Override
   public int countNonAirCells() {
      return this.data == null ? 0 : this.data.cardinality();
   }

   public class BuildingInfo extends Snapshot.BuildingInfo {
      public BuildingInfo(BlockPos basePos, Rotation rotation) {
         super(basePos, rotation);
      }

      public Template getSnapshot() {
         return Template.this;
      }
   }

   public class FilledTemplate implements IFilledTemplate {
      private final BlockPos max = Template.this.size.subtract(VecUtil.POS_ONE);

      public Template getTemplate() {
         return Template.this;
      }

      private void checkPos(int x, int y, int z) {
         if (x < 0 || y < 0 || z < 0 || x >= Template.this.size.getX() || y >= Template.this.size.getY() || z >= Template.this.size.getZ()) {
            throw new IllegalArgumentException("Size: " + Template.this.size + ", pos: " + new BlockPos(x, y, z));
         }
      }

      @Override
      public BlockPos getSize() {
         return Template.this.size;
      }

      @Override
      public BlockPos getMax() {
         return this.max;
      }

      @Override
      public void set(int x, int y, int z, boolean value) {
         this.checkPos(x, y, z);
         Template.this.data.set(Template.this.posToIndex(x, y, z), value);
      }

      @Override
      public boolean get(int x, int y, int z) {
         this.checkPos(x, y, z);
         return Template.this.data.get(Template.this.posToIndex(x, y, z));
      }

      @Override
      public void setLineX(int fromX, int toX, int y, int z, boolean value) {
         this.checkPos(fromX, y, z);
         this.checkPos(toX, y, z);
         Template.this.data.set(Template.this.posToIndex(fromX, y, z), Template.this.posToIndex(toX, y, z) + 1, value);
      }

      @Override
      public void setLineY(int x, int fromY, int toY, int z, boolean value) {
         this.checkPos(x, fromY, z);
         this.checkPos(x, toY, z);

         for (int y = fromY; y <= toY; y++) {
            this.set(x, y, z, value);
         }
      }

      @Override
      public void setLineZ(int x, int y, int fromZ, int toZ, boolean value) {
         this.checkPos(x, y, fromZ);
         this.checkPos(x, y, toZ);

         for (int z = fromZ; z <= toZ; z++) {
            this.set(x, y, z, value);
         }
      }

      @Override
      public void setAreaYZ(int x, int fromY, int toY, int fromZ, int toZ, boolean value) {
         this.checkPos(x, fromY, fromZ);
         this.checkPos(x, toY, toZ);

         for (int z = fromZ; z <= toZ; z++) {
            this.setLineY(x, fromY, toY, z, value);
         }
      }

      @Override
      public void setAreaXZ(int fromX, int toX, int y, int fromZ, int toZ, boolean value) {
         this.checkPos(fromX, y, fromZ);
         this.checkPos(toX, y, toZ);

         for (int z = fromZ; z <= toZ; z++) {
            this.setLineX(fromX, toX, y, z, value);
         }
      }

      @Override
      public void setAreaXY(int fromX, int toX, int fromY, int toY, int z, boolean value) {
         this.checkPos(fromX, fromY, z);
         this.checkPos(toX, toY, z);

         for (int y = fromY; y <= toY; y++) {
            this.setLineX(fromX, toX, y, z, value);
         }
      }

      @Override
      public void setPlaneYZ(int x, boolean value) {
         this.checkPos(x, 0, 0);
         this.setAreaYZ(x, 0, this.max.getY(), 0, this.max.getZ(), value);
      }

      @Override
      public void setPlaneXZ(int y, boolean value) {
         this.checkPos(0, y, 0);
         this.setAreaXZ(0, this.max.getX(), y, 0, this.max.getZ(), value);
      }

      @Override
      public void setPlaneXY(int z, boolean value) {
         this.checkPos(0, 0, z);
         Template.this.data.set(Template.this.posToIndex(0, 0, z), Template.this.posToIndex(this.max.getX(), this.max.getY(), z) + 1, value);
      }

      @Override
      public void setAll(boolean value) {
         Template.this.data.set(0, Template.this.getDataSize(), value);
      }

      @Override
      public String toString() {
         List<String> zParts = new ArrayList<>();

         for (int z = 0; z < this.getSize().getZ(); z++) {
            List<String> yParts = new ArrayList<>();

            for (int y = 0; y < this.getSize().getY(); y++) {
               List<String> xParts = new ArrayList<>();

               for (int x = 0; x < this.getSize().getX(); x++) {
                  xParts.add(this.get(x, y, z) ? "#" : " ");
               }

               yParts.add(String.join("", xParts));
            }

            zParts.add(String.join("\n", yParts));
         }

         return String.join("\n" + String.join("", Collections.nCopies(this.getSize().getX(), "-")) + "\n", zParts);
      }
   }
}
