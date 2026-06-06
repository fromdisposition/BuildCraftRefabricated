package buildcraft.core.item;

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
import net.minecraft.world.item.component.TooltipDisplay;
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

      String typeName = tag.getString("mapType").orElse("");

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
         stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float)type.ordinal()), List.of(), List.of(), List.of()));
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
      return new BlockPos(tag.getInt("X").orElse(0), tag.getInt("Y").orElse(0), tag.getInt("Z").orElse(0));
   }

   @Override
   @SuppressWarnings("deprecation")
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
      CompoundTag cpt = getCustomTag(stack);
      if (cpt.contains("name")) {
         String name = cpt.getString("name").orElse("");
         if (!name.isEmpty()) {
            tooltip.accept(Component.literal(name));
         }
      }

      IMapLocation.MapLocationType type = getTypeFromStack(stack);
      switch (type) {
         case SPOT:
            if (cpt.contains("x") && cpt.contains("y") && cpt.contains("z") && cpt.contains("side")) {
               int x = cpt.getInt("x").orElse(0);
               int y = cpt.getInt("y").orElse(0);
               int z = cpt.getInt("z").orElse(0);
               Direction side = Direction.values()[cpt.getByte("side").orElse((byte)0)];
               tooltip.accept(Component.literal("{" + x + ", " + y + ", " + z + ", " + side + "}"));
            }
            break;
         case AREA:
            if (cpt.contains("xMin") && cpt.contains("yMin") && cpt.contains("zMin") && cpt.contains("xMax") && cpt.contains("yMax") && cpt.contains("zMax")) {
               int x = cpt.getInt("xMin").orElse(0);
               int y = cpt.getInt("yMin").orElse(0);
               int z = cpt.getInt("zMin").orElse(0);
               int xLength = cpt.getInt("xMax").orElse(0) - x + 1;
               int yLength = cpt.getInt("yMax").orElse(0) - y + 1;
               int zLength = cpt.getInt("zMax").orElse(0) - z + 1;
               tooltip.accept(Component.literal("{" + x + ", " + y + ", " + z + "} + {" + xLength + " x " + yLength + " x " + zLength + "}"));
            }
            break;
         case PATH:
         case PATH_REPEATING:
            ListTag pathNBT = (ListTag)cpt.getList("path").orElse(null);
            if (pathNBT != null && pathNBT.size() > 0) {
               CompoundTag firstTag = (CompoundTag)pathNBT.getCompound(0).orElse(null);
               if (firstTag != null) {
                  BlockPos first = readBlockPosNbt(firstTag);
                  tooltip.accept(
                     Component.literal("{" + first.getX() + ", " + first.getY() + ", " + first.getZ() + "}, (+" + (pathNBT.size() - 1) + " elements)")
                  );
               }
            }
      }

      if (type != IMapLocation.MapLocationType.CLEAN) {
         tooltip.accept(Component.translatable("buildcraft.item.nonclean.usage", new Object[]{Component.keybind("key.sneak"), Component.keybind("key.use")}));
      }
   }

   public InteractionResult use(Level level, Player player, InteractionHand hand) {
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
      int xMin = cpt.getInt("xMin").orElse(0);
      int yMin = cpt.getInt("yMin").orElse(0);
      int zMin = cpt.getInt("zMin").orElse(0);
      BlockPos min = new BlockPos(xMin, yMin, zMin);
      int xMax = cpt.getInt("xMax").orElse(0);
      int yMax = cpt.getInt("yMax").orElse(0);
      int zMax = cpt.getInt("zMax").orElse(0);
      BlockPos max = new BlockPos(xMax, yMax, zMax);
      return new Box(min, max);
   }

   public static IBox getPointBox(@Nonnull ItemStack item) {
      CompoundTag cpt = getCustomTag(item);
      IMapLocation.MapLocationType type = getTypeFromStack(item);
      switch (type) {
         case SPOT:
            int x = cpt.getInt("x").orElse(0);
            int y = cpt.getInt("y").orElse(0);
            int z = cpt.getInt("z").orElse(0);
            BlockPos pos = new BlockPos(x, y, z);
            return new Box(pos, pos);
         default:
            return null;
      }
   }

   public static Direction getPointFace(@Nonnull ItemStack stack) {
      CompoundTag cpt = getCustomTag(stack);
      return Direction.values()[cpt.getByte("side").orElse((byte)0)];
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
         return Direction.values()[cpt.getByte("side").orElse((byte)0)];
      } else {
         return null;
      }
   }

   @Override
   public BlockPos getPoint(@Nonnull ItemStack item) {
      CompoundTag cpt = getCustomTag(item);
      IMapLocation.MapLocationType type = getTypeFromStack(item);
      return type == IMapLocation.MapLocationType.SPOT ? new BlockPos(cpt.getInt("x").orElse(0), cpt.getInt("y").orElse(0), cpt.getInt("z").orElse(0)) : null;
   }

   @Override
   public IZone getZone(@Nonnull ItemStack item) {
      IMapLocation.MapLocationType type = getTypeFromStack(item);
      switch (type) {
         case AREA:
            return this.getBox(item);
         case PATH:
         case PATH_REPEATING:
            return getPointBox(item);
         case ZONE:
            return null;
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
            indexList.add(new BlockPos(cpt.getInt("x").orElse(0), cpt.getInt("y").orElse(0), cpt.getInt("z").orElse(0)));
            return indexList;
         }
         case AREA:
         default:
            return null;
         case PATH:
         case PATH_REPEATING: {
            List<BlockPos> indexList = new ArrayList<>();
            ListTag pathNBT = (ListTag)cpt.getList("path").orElse(null);
            if (pathNBT != null) {
               for (int i = 0; i < pathNBT.size(); i++) {
                  CompoundTag posTag = (CompoundTag)pathNBT.getCompound(i).orElse(null);
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
      return cpt.getString("name").orElse("");
   }

   @Override
   public boolean setLocationName(@Nonnull ItemStack item, String name) {
      CompoundTag cpt = getCustomTag(item);
      cpt.putString("name", name);
      setCustomTag(item, cpt);
      return true;
   }
}
