/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;
import buildcraft.lib.misc.data.Box;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
//? if >= 1.21.10 {
import net.minecraft.world.item.component.TooltipDisplay;
//?}
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ItemMapLocation extends Item implements IMapLocation {
   private static final String[] STORAGE_TAGS = "x,y,z,side,xMin,xMax,yMin,yMax,zMin,zMax,path,chunkMapping,name".split(",");
   private static final String TAG_MAP_TYPE = "mapType";

   public ItemMapLocation(Properties properties) {
      super(properties);
   }

   private static IMapLocation.MapLocationType getTypeFromStack(@Nonnull ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      if (customData == null) {
         return IMapLocation.MapLocationType.CLEAN;
      }

      CompoundTag tag = customData.copyTag();
      if (!tag.contains("mapType")) {
         return IMapLocation.MapLocationType.CLEAN;
      }

      String typeName = BcNbt.getString(tag, "mapType", "");

      try {
         return IMapLocation.MapLocationType.valueOf(typeName);
      } catch (IllegalArgumentException e) {
         return IMapLocation.MapLocationType.CLEAN;
      }
   }

   private static void setTypeToStack(@Nonnull ItemStack stack, IMapLocation.MapLocationType type) {
      stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
         CompoundTag tag = data.copyTag();
         tag.putString("mapType", type.name());
         return CustomData.of(tag);
      });
   }

   private static void updateModelData(@Nonnull ItemStack stack, IMapLocation.MapLocationType type) {
      if (type == IMapLocation.MapLocationType.CLEAN) {
         stack.remove(DataComponents.CUSTOM_MODEL_DATA);
      } else {
         stack.set(DataComponents.CUSTOM_MODEL_DATA, buildcraft.lib.compat.BcModelData.index((float)type.ordinal()));
      }
   }

   private static CompoundTag getCustomTag(@Nonnull ItemStack stack) {
      CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return customData == null ? new CompoundTag() : customData.copyTag();
   }

   private static void setCustomTag(@Nonnull ItemStack stack, CompoundTag tag) {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
   }

   private static CompoundTag writeBlockPosNbt(BlockPos pos) {
      CompoundTag tag = new CompoundTag();
      tag.putInt("X", pos.getX());
      tag.putInt("Y", pos.getY());
      tag.putInt("Z", pos.getZ());
      return tag;
   }

   private static BlockPos readBlockPosNbt(CompoundTag tag) {
      return new BlockPos(BcNbt.getInt(tag, "X", 0), BcNbt.getInt(tag, "Y", 0), BcNbt.getInt(tag, "Z", 0));
   }

   private static Direction readSide(CompoundTag tag) {
      int side = BcNbt.getByte(tag, "side", (byte)0) & 255;
      return side >= 0 && side < Direction.values().length ? Direction.values()[side] : Direction.UP;
   }

   public static void appendTooltipLines(ItemMapLocation item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      CompoundTag cpt = getCustomTag(stack);
      if (cpt.contains("name")) {
         String name = BcNbt.getString(cpt, "name", "");
         if (!name.isEmpty()) {
            tooltip.add(Component.literal(name));
         }
      }

      IMapLocation.MapLocationType type = getTypeFromStack(stack);
      switch (type) {
         case SPOT:
            if (cpt.contains("x") && cpt.contains("y") && cpt.contains("z") && cpt.contains("side")) {
               int x = BcNbt.getInt(cpt, "x", 0);
               int y = BcNbt.getInt(cpt, "y", 0);
               int z = BcNbt.getInt(cpt, "z", 0);
               Direction side = readSide(cpt);
               tooltip.add(Component.literal("{" + x + ", " + y + ", " + z + ", " + side + "}"));
            }
            break;
         case AREA:
            if (cpt.contains("xMin") && cpt.contains("yMin") && cpt.contains("zMin") && cpt.contains("xMax") && cpt.contains("yMax") && cpt.contains("zMax")) {
               int x = BcNbt.getInt(cpt, "xMin", 0);
               int y = BcNbt.getInt(cpt, "yMin", 0);
               int z = BcNbt.getInt(cpt, "zMin", 0);
               int xLength = BcNbt.getInt(cpt, "xMax", 0) - x + 1;
               int yLength = BcNbt.getInt(cpt, "yMax", 0) - y + 1;
               int zLength = BcNbt.getInt(cpt, "zMax", 0) - z + 1;
               tooltip.add(Component.literal("{" + x + ", " + y + ", " + z + "} + {" + xLength + " x " + yLength + " x " + zLength + "}"));
            }
            break;
         case PATH:
         case PATH_REPEATING:
            ListTag pathNBT = (ListTag)BcNbt.getList(cpt, "path");
            if (pathNBT != null && pathNBT.size() > 0) {
               CompoundTag firstTag = (CompoundTag)BcNbt.getCompound(pathNBT, 0);
               if (firstTag != null) {
                  BlockPos first = readBlockPosNbt(firstTag);
                  tooltip.add(
                     Component.literal("{" + first.getX() + ", " + first.getY() + ", " + first.getZ() + "}, (+" + (pathNBT.size() - 1) + " elements)")
                  );
               }
            }
      }

      if (type != IMapLocation.MapLocationType.CLEAN) {
         tooltip.add(Component.translatable("buildcraft.item.nonclean.usage", new Object[]{Component.keybind("key.sneak"), Component.keybind("key.use")}));
      }
   }

   //? if >= 1.21.10 {
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      return this.bcUse(level, player, hand);
   }
   //?} else {
   /*public net.minecraft.world.InteractionResultHolder<net.minecraft.world.item.ItemStack> use(Level level, Player player, InteractionHand hand) {
      return buildcraft.lib.compat.BcInteract.toUse(this.bcUse(level, player, hand), player, hand);
   }
   *///?}

   private InteractionResult bcUse(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (level.isClientSide()) {
         return InteractionResult.PASS;
      } else {
         return (InteractionResult)(player.isShiftKeyDown() ? clearMarkerData(stack) : InteractionResult.PASS);
      }
   }

   private static InteractionResult clearMarkerData(@Nonnull ItemStack stack) {
      if (getTypeFromStack(stack) == IMapLocation.MapLocationType.CLEAN) {
         return InteractionResult.PASS;
      }

      CompoundTag nbt = getCustomTag(stack);

      for (String key : STORAGE_TAGS) {
         nbt.remove(key);
      }

      nbt.putString("mapType", IMapLocation.MapLocationType.CLEAN.name());
      if (nbt.size() <= 1) {
         stack.remove(DataComponents.CUSTOM_DATA);
      } else {
         setCustomTag(stack, nbt);
      }

      updateModelData(stack, IMapLocation.MapLocationType.CLEAN);
      return InteractionResult.SUCCESS;
   }

   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      if (level.isClientSide()) {
         return InteractionResult.PASS;
      }

      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.PASS;
      }

      ItemStack stack = context.getItemInHand();
      if (getTypeFromStack(stack) != IMapLocation.MapLocationType.CLEAN) {
         return InteractionResult.FAIL;
      }

      BlockPos pos = context.getClickedPos();
      Direction side = context.getClickedFace();
      ItemStack modified = stack;
      if (stack.getCount() > 1) {
         modified = stack.copy();
         stack.shrink(1);
         modified.setCount(1);
      }

      BlockEntity tile = level.getBlockEntity(pos);
      CompoundTag cpt = getCustomTag(modified);
      IMapLocation.MapLocationType newType;
      if (tile instanceof IPathProvider) {
         List<BlockPos> path = ((IPathProvider)tile).getPath();
         if (path.size() > 1 && path.get(0).equals(path.get(path.size() - 1))) {
            newType = IMapLocation.MapLocationType.PATH_REPEATING;
         } else {
            newType = IMapLocation.MapLocationType.PATH;
         }

         ListTag pathNBT = new ListTag();

         for (BlockPos posInPath : path) {
            pathNBT.add(writeBlockPosNbt(posInPath));
         }

         cpt.put("path", pathNBT);
      } else if (tile instanceof IAreaProvider) {
         newType = IMapLocation.MapLocationType.AREA;
         IAreaProvider areaTile = (IAreaProvider)tile;
         cpt.putInt("xMin", areaTile.min().getX());
         cpt.putInt("yMin", areaTile.min().getY());
         cpt.putInt("zMin", areaTile.min().getZ());
         cpt.putInt("xMax", areaTile.max().getX());
         cpt.putInt("yMax", areaTile.max().getY());
         cpt.putInt("zMax", areaTile.max().getZ());
      } else {
         newType = IMapLocation.MapLocationType.SPOT;
         cpt.putByte("side", (byte)side.ordinal());
         cpt.putInt("x", pos.getX());
         cpt.putInt("y", pos.getY());
         cpt.putInt("z", pos.getZ());
      }

      cpt.putString("mapType", newType.name());
      setCustomTag(modified, cpt);
      updateModelData(modified, newType);
      if (modified != stack && !player.getInventory().add(modified)) {
         player.drop(modified, false);
      }

      return InteractionResult.SUCCESS;
   }

   public static IBox getAreaBox(@Nonnull ItemStack item) {
      CompoundTag cpt = getCustomTag(item);
      int xMin = BcNbt.getInt(cpt, "xMin", 0);
      int yMin = BcNbt.getInt(cpt, "yMin", 0);
      int zMin = BcNbt.getInt(cpt, "zMin", 0);
      BlockPos min = new BlockPos(xMin, yMin, zMin);
      int xMax = BcNbt.getInt(cpt, "xMax", 0);
      int yMax = BcNbt.getInt(cpt, "yMax", 0);
      int zMax = BcNbt.getInt(cpt, "zMax", 0);
      BlockPos max = new BlockPos(xMax, yMax, zMax);
      return new Box(min, max);
   }

   public static IBox getPathBoundingBox(@Nonnull ItemStack item) {
      if (!(item.getItem() instanceof ItemMapLocation map)) {
         return null;
      }

      List<BlockPos> path = map.getPath(item);
      if (path == null || path.isEmpty()) {
         return null;
      }

      int minX = path.get(0).getX();
      int minY = path.get(0).getY();
      int minZ = path.get(0).getZ();
      int maxX = minX;
      int maxY = minY;
      int maxZ = minZ;

      for (BlockPos pos : path) {
         minX = Math.min(minX, pos.getX());
         minY = Math.min(minY, pos.getY());
         minZ = Math.min(minZ, pos.getZ());
         maxX = Math.max(maxX, pos.getX());
         maxY = Math.max(maxY, pos.getY());
         maxZ = Math.max(maxZ, pos.getZ());
      }

      return new Box(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
   }

   public static IBox getPointBox(@Nonnull ItemStack item) {
      CompoundTag cpt = getCustomTag(item);
      IMapLocation.MapLocationType type = getTypeFromStack(item);
      switch (type) {
         case SPOT:
            int x = BcNbt.getInt(cpt, "x", 0);
            int y = BcNbt.getInt(cpt, "y", 0);
            int z = BcNbt.getInt(cpt, "z", 0);
            BlockPos pos = new BlockPos(x, y, z);
            return new Box(pos, pos);
         default:
            return null;
      }
   }

   public static Direction getPointFace(@Nonnull ItemStack stack) {
      CompoundTag cpt = getCustomTag(stack);
      return readSide(cpt);
   }

   @Override
   public IBox getBox(@Nonnull ItemStack item) {
      IMapLocation.MapLocationType type = getTypeFromStack(item);
      switch (type) {
         case SPOT:
            return getPointBox(item);
         case AREA:
            return getAreaBox(item);
         default:
            return null;
      }
   }

   @Override
   public Direction getPointSide(@Nonnull ItemStack item) {
      IMapLocation.MapLocationType type = getTypeFromStack(item);
      if (type == IMapLocation.MapLocationType.SPOT) {
         CompoundTag cpt = getCustomTag(item);
         return readSide(cpt);
      } else {
         return null;
      }
   }

   @Override
   public BlockPos getPoint(@Nonnull ItemStack item) {
      CompoundTag cpt = getCustomTag(item);
      IMapLocation.MapLocationType type = getTypeFromStack(item);
      return type == IMapLocation.MapLocationType.SPOT ? new BlockPos(BcNbt.getInt(cpt, "x", 0), BcNbt.getInt(cpt, "y", 0), BcNbt.getInt(cpt, "z", 0)) : null;
   }

   @Override
   public IZone getZone(@Nonnull ItemStack item) {
      IMapLocation.MapLocationType type = getTypeFromStack(item);
      switch (type) {
         case AREA:
            return this.getBox(item);
         case PATH:
         case PATH_REPEATING:
            return getPathBoundingBox(item);
         case ZONE:
            buildcraft.robotics.zone.ZonePlan plan = new buildcraft.robotics.zone.ZonePlan();
            plan.readFromNBT(getCustomTag(item));
            return plan;
         default:
            return null;
      }
   }

   @Override
   public List<BlockPos> getPath(@Nonnull ItemStack item) {
      CompoundTag cpt = getCustomTag(item);
      IMapLocation.MapLocationType type = getTypeFromStack(item);
      switch (type) {
         case SPOT: {
            List<BlockPos> indexList = new ArrayList<>();
            indexList.add(new BlockPos(BcNbt.getInt(cpt, "x", 0), BcNbt.getInt(cpt, "y", 0), BcNbt.getInt(cpt, "z", 0)));
            return indexList;
         }
         case AREA:
         default:
            return null;
         case PATH:
         case PATH_REPEATING: {
            List<BlockPos> indexList = new ArrayList<>();
            ListTag pathNBT = (ListTag)BcNbt.getList(cpt, "path");
            if (pathNBT != null) {
               for (int i = 0; i < pathNBT.size(); i++) {
                  CompoundTag posTag = (CompoundTag)BcNbt.getCompound(pathNBT, i);
                  if (posTag != null) {
                     indexList.add(readBlockPosNbt(posTag));
                  }
               }
            }

            return indexList;
         }
      }
   }

   @Override
   public String getLocationName(@Nonnull ItemStack item) {
      CompoundTag cpt = getCustomTag(item);
      return BcNbt.getString(cpt, "name", "");
   }

   @Override
   public boolean setLocationName(@Nonnull ItemStack item, String name) {
      CompoundTag cpt = getCustomTag(item);
      cpt.putString("name", name);
      setCustomTag(item, cpt);
      return true;
   }
}
