/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.item;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IDockingStationProvider;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.robotics.boards.BCBoardNBT;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ItemRobot extends Item {
   public ItemRobot(Properties properties) {
      super(properties.stacksTo(1));
   }

   public static ItemStack createRobotStack(RedstoneBoardRobotNBT board, long energy) {
      ItemStack stack = new ItemStack(BCRoboticsItems.ROBOT);
      CompoundTag data = NBTUtilBC.getItemData(stack);
      CompoundTag boardTag = new CompoundTag();
      board.createBoard(boardTag);
      data.put("board", boardTag);
      data.putLong("energy", energy);
      NBTUtilBC.setItemData(stack, data);
      return stack;
   }

   public static RedstoneBoardRobotNBT getRobotNBT(ItemStack stack) {
      CompoundTag data = NBTUtilBC.getItemData(stack);
      CompoundTag boardTag = BcNbt.getCompound(data, "board");
      RedstoneBoardNBT<?> board = boardTag != null
         ? RedstoneBoardRegistry.instance.getRedstoneBoard(boardTag)
         : RedstoneBoardRegistry.instance.getEmptyRobotBoard();
      return board instanceof RedstoneBoardRobotNBT robotBoard ? robotBoard : RedstoneBoardRegistry.instance.getEmptyRobotBoard();
   }

   public static long getEnergy(ItemStack stack) {
      return buildcraft.lib.nbt.BcNbt.getLong(NBTUtilBC.getItemData(stack), "energy", 0L);
   }

   /** Show which board (and thus which kind of robot) the stack carries -- the board NBT already localizes its
    * own name and description, exactly like the redstone board item's tooltip. */
   public static void appendTooltipLines(ItemRobot item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      BCBoardNBT.appendColouredInfo(getRobotNBT(stack), stack, flag.isAdvanced(), tooltip);
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Direction face = context.getClickedFace();
      BlockEntity tile = level.getBlockEntity(pos);
      if (!(tile instanceof IPipeHolder holder)) {
         return InteractionResult.PASS;
      }

      IDockingStationProvider provider = resolveStation(holder, face, pos, context.getClickLocation());
      if (provider == null) {
         return InteractionResult.PASS;
      }

      ItemStack stack = context.getItemInHand();
      RedstoneBoardRobotNBT boardNBT = getRobotNBT(stack);
      if (boardNBT == RedstoneBoardRegistry.instance.getEmptyRobotBoard()) {
         return InteractionResult.PASS;
      }

      if (level.isClientSide()) {
         return InteractionResult.SUCCESS;
      }

      DockingStation station = provider.getStation();
      if (station == null || station.isTaken()) {
         return InteractionResult.FAIL;
      }

      EntityRobot robot = EntityRobot.create(level, boardNBT);
      if (context.getPlayer() != null) {
         robot.setOwner(context.getPlayer().getGameProfile());
      }
      robot.getBattery().setStored(getEnergy(stack));
      Vec3 spawn = Vec3.atCenterOf(pos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
      robot.setPos(spawn.x, spawn.y, spawn.z);
      robot.getRegistry().registerRobot(robot);
      level.addFreshEntity(robot);
      if (station.takeAsMain(robot)) {
         robot.setLinkedStation(station);
         robot.dock(station);
      }

      if (!context.getPlayer().getAbilities().instabuild) {
         stack.shrink(1);
      }

      return InteractionResult.SUCCESS;
   }

   /**
    * Resolve the docking-station pluggable the player aimed at. {@code getClickedFace()} only matches the station's
    * side when the ray hits the station box's outward face; aiming at the box's protruding side edges yields a
    * different face, so {@code getPluggable(face)} misses and the deploy silently fails. Fall back to the hit
    * location tested against each pluggable's bounding box -- the same way {@link buildcraft.transport.block.BlockPipeHolder}
    * resolves clicks on pluggables.
    */
   private static IDockingStationProvider resolveStation(IPipeHolder holder, Direction clickedFace, BlockPos pos, Vec3 hit) {
      if (holder.getPluggable(clickedFace) instanceof IDockingStationProvider direct) {
         return direct;
      }

      double lx = hit.x - pos.getX();
      double ly = hit.y - pos.getY();
      double lz = hit.z - pos.getZ();
      for (Direction dir : Direction.values()) {
         PipePluggable plug = holder.getPluggable(dir);
         if (plug instanceof IDockingStationProvider provider) {
            AABB box = plug.getBoundingBox().inflate(0.02);
            if (lx >= box.minX && lx <= box.maxX && ly >= box.minY && ly <= box.maxY && lz >= box.minZ && lz <= box.maxZ) {
               return provider;
            }
         }
      }

      return null;
   }
}
