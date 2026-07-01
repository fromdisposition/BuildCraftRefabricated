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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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

   @Override
   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Direction face = context.getClickedFace();
      BlockEntity tile = level.getBlockEntity(pos);
      if (!(tile instanceof IPipeHolder holder)) {
         return InteractionResult.PASS;
      }

      PipePluggable plug = holder.getPluggable(face);
      if (!(plug instanceof IDockingStationProvider provider)) {
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
}
