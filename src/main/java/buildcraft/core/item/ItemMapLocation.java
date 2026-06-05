/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;

import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.IBox;
import buildcraft.api.core.IPathProvider;
import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;

import buildcraft.lib.misc.data.Box;

@SuppressWarnings("deprecation")
public class ItemMapLocation extends Item implements IMapLocation {
    private static final String[] STORAGE_TAGS = "x,y,z,side,xMin,xMax,yMin,yMax,zMin,zMax,path,chunkMapping,name".split(",");
    private static final String TAG_MAP_TYPE = "mapType";

    public ItemMapLocation(Item.Properties properties) {
        super(properties);
    }

    private static MapLocationType getTypeFromStack(@Nonnull ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return MapLocationType.CLEAN;
        }
        CompoundTag tag = customData.copyTag();
        if (!tag.contains(TAG_MAP_TYPE)) {
            return MapLocationType.CLEAN;
        }
        String typeName = tag.getString(TAG_MAP_TYPE).orElse("");
        try {
            return MapLocationType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return MapLocationType.CLEAN;
        }
    }

    private static void setTypeToStack(@Nonnull ItemStack stack, MapLocationType type) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> {
            CompoundTag tag = data.copyTag();
            tag.putString(TAG_MAP_TYPE, type.name());
            return CustomData.of(tag);
        });
    }

    private static void updateModelData(@Nonnull ItemStack stack, MapLocationType type) {
        if (type == MapLocationType.CLEAN) {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        } else {

            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                    java.util.List.of((float) type.ordinal()),
                    java.util.List.of(),
                    java.util.List.of(),
                    java.util.List.of()
            ));
        }
    }

    private static CompoundTag getCustomTag(@Nonnull ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return new CompoundTag();
        }
        return customData.copyTag();
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
        return new BlockPos(
            tag.getInt("X").orElse(0),
            tag.getInt("Y").orElse(0),
            tag.getInt("Z").orElse(0)
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        CompoundTag cpt = getCustomTag(stack);

        if (cpt.contains("name")) {
            String name = cpt.getString("name").orElse("");
            if (!name.isEmpty()) {
                tooltip.accept(Component.literal(name));
            }
        }

        MapLocationType type = getTypeFromStack(stack);
        switch (type) {
            case SPOT: {
                if (cpt.contains("x") && cpt.contains("y") && cpt.contains("z") && cpt.contains("side")) {
                    int x = cpt.getInt("x").orElse(0);
                    int y = cpt.getInt("y").orElse(0);
                    int z = cpt.getInt("z").orElse(0);
                    Direction side = Direction.values()[cpt.getByte("side").orElse((byte) 0)];
                    tooltip.accept(Component.literal("{" + x + ", " + y + ", " + z + ", " + side + "}"));
                }
                break;
            }
            case AREA: {
                if (cpt.contains("xMin") && cpt.contains("yMin") && cpt.contains("zMin")
                        && cpt.contains("xMax") && cpt.contains("yMax") && cpt.contains("zMax")) {
                    int x = cpt.getInt("xMin").orElse(0);
                    int y = cpt.getInt("yMin").orElse(0);
                    int z = cpt.getInt("zMin").orElse(0);
                    int xLength = cpt.getInt("xMax").orElse(0) - x + 1;
                    int yLength = cpt.getInt("yMax").orElse(0) - y + 1;
                    int zLength = cpt.getInt("zMax").orElse(0) - z + 1;
                    tooltip.accept(Component.literal(
                        "{" + x + ", " + y + ", " + z + "} + {" + xLength + " x " + yLength + " x " + zLength + "}"));
                }
                break;
            }
            case PATH:
            case PATH_REPEATING: {
                ListTag pathNBT = cpt.getList("path").orElse(null);
                if (pathNBT != null && pathNBT.size() > 0) {
                    CompoundTag firstTag = pathNBT.getCompound(0).orElse(null);
                    if (firstTag != null) {
                        BlockPos first = readBlockPosNbt(firstTag);
                        tooltip.accept(Component.literal(
                            "{" + first.getX() + ", " + first.getY() + ", " + first.getZ()
                                + "}, (+" + (pathNBT.size() - 1) + " elements)"));
                    }
                }
                break;
            }
            default:
                break;
        }
        if (type != MapLocationType.CLEAN) {
            tooltip.accept(Component.translatable("buildcraft.item.nonclean.usage",
                Component.keybind("key.sneak"), Component.keybind("key.use")));
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            return clearMarkerData(stack);
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult clearMarkerData(@Nonnull ItemStack stack) {
        if (getTypeFromStack(stack) == MapLocationType.CLEAN) {
            return InteractionResult.PASS;
        }
        CompoundTag nbt = getCustomTag(stack);
        for (String key : STORAGE_TAGS) {
            nbt.remove(key);
        }
        nbt.putString(TAG_MAP_TYPE, MapLocationType.CLEAN.name());
        if (nbt.size() <= 1) {

            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            setCustomTag(stack, nbt);
        }
        updateModelData(stack, MapLocationType.CLEAN);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        if (getTypeFromStack(stack) != MapLocationType.CLEAN) {
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
        MapLocationType newType;

        if (tile instanceof IPathProvider) {
            List<BlockPos> path = ((IPathProvider) tile).getPath();

            if (path.size() > 1 && path.get(0).equals(path.get(path.size() - 1))) {
                newType = MapLocationType.PATH_REPEATING;
            } else {
                newType = MapLocationType.PATH;
            }

            ListTag pathNBT = new ListTag();
            for (BlockPos posInPath : path) {
                pathNBT.add(writeBlockPosNbt(posInPath));
            }
            cpt.put("path", pathNBT);
        } else if (tile instanceof IAreaProvider) {
            newType = MapLocationType.AREA;

            IAreaProvider areaTile = (IAreaProvider) tile;

            cpt.putInt("xMin", areaTile.min().getX());
            cpt.putInt("yMin", areaTile.min().getY());
            cpt.putInt("zMin", areaTile.min().getZ());
            cpt.putInt("xMax", areaTile.max().getX());
            cpt.putInt("yMax", areaTile.max().getY());
            cpt.putInt("zMax", areaTile.max().getZ());
        } else {
            newType = MapLocationType.SPOT;

            cpt.putByte("side", (byte) side.ordinal());
            cpt.putInt("x", pos.getX());
            cpt.putInt("y", pos.getY());
            cpt.putInt("z", pos.getZ());
        }

        cpt.putString(TAG_MAP_TYPE, newType.name());
        setCustomTag(modified, cpt);
        updateModelData(modified, newType);

        if (modified != stack) {

            if (!player.getInventory().add(modified)) {
                player.drop(modified, false);
            }
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
        MapLocationType type = getTypeFromStack(item);

        switch (type) {
            case SPOT: {
                int x = cpt.getInt("x").orElse(0);
                int y = cpt.getInt("y").orElse(0);
                int z = cpt.getInt("z").orElse(0);
                BlockPos pos = new BlockPos(x, y, z);
                return new Box(pos, pos);
            }
            default:
                return null;
        }
    }

    public static Direction getPointFace(@Nonnull ItemStack stack) {
        CompoundTag cpt = getCustomTag(stack);
        return Direction.values()[cpt.getByte("side").orElse((byte) 0)];
    }

    @Override
    public IBox getBox(@Nonnull ItemStack item) {
        MapLocationType type = getTypeFromStack(item);

        switch (type) {
            case AREA:
                return getAreaBox(item);
            case SPOT:
                return getPointBox(item);
            default:
                return null;
        }
    }

    @Override
    public Direction getPointSide(@Nonnull ItemStack item) {
        MapLocationType type = getTypeFromStack(item);

        if (type == MapLocationType.SPOT) {
            CompoundTag cpt = getCustomTag(item);
            return Direction.values()[cpt.getByte("side").orElse((byte) 0)];
        }
        return null;
    }

    @Override
    public BlockPos getPoint(@Nonnull ItemStack item) {
        CompoundTag cpt = getCustomTag(item);
        MapLocationType type = getTypeFromStack(item);

        if (type == MapLocationType.SPOT) {
            return new BlockPos(
                cpt.getInt("x").orElse(0),
                cpt.getInt("y").orElse(0),
                cpt.getInt("z").orElse(0)
            );
        }
        return null;
    }

    @Override
    public IZone getZone(@Nonnull ItemStack item) {
        MapLocationType type = getTypeFromStack(item);
        switch (type) {
            case ZONE:

                return null;
            case AREA:
                return getBox(item);
            case PATH:
            case PATH_REPEATING:
                return getPointBox(item);
            default:
                return null;
        }
    }

    @Override
    public List<BlockPos> getPath(@Nonnull ItemStack item) {
        CompoundTag cpt = getCustomTag(item);
        MapLocationType type = getTypeFromStack(item);
        switch (type) {
            case PATH:
            case PATH_REPEATING: {
                List<BlockPos> indexList = new ArrayList<>();
                ListTag pathNBT = cpt.getList("path").orElse(null);
                if (pathNBT != null) {
                    for (int i = 0; i < pathNBT.size(); i++) {
                        CompoundTag posTag = pathNBT.getCompound(i).orElse(null);
                        if (posTag != null) {
                            indexList.add(readBlockPosNbt(posTag));
                        }
                    }
                }
                return indexList;
            }
            case SPOT: {
                List<BlockPos> indexList = new ArrayList<>();
                indexList.add(new BlockPos(
                    cpt.getInt("x").orElse(0),
                    cpt.getInt("y").orElse(0),
                    cpt.getInt("z").orElse(0)
                ));
                return indexList;
            }
            default:
                return null;
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
