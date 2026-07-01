/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.boards;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.LocaleUtil;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

public class BCBoardNBT extends RedstoneBoardRobotNBT {
   public static final Map<String, BCBoardNBT> REGISTRY = new HashMap<>();

   private final String id;
   private final String name;
   private final String upperName;
   private final String boardType;
   private final Identifier robotTexture;
   private final Constructor<? extends RedstoneBoardRobot> boardCtor;

   public BCBoardNBT(String id, String name, Class<? extends RedstoneBoardRobot> boardClass, String boardType) {
      this.id = id;
      this.name = name;
      this.boardType = boardType;
      this.upperName = name.substring(0, 1).toUpperCase() + name.substring(1);
      this.robotTexture = Identifier.fromNamespaceAndPath("buildcraftrobotics", "entities/robot_" + name.toLowerCase());

      try {
         this.boardCtor = boardClass.getConstructor(EntityRobotBase.class);
      } catch (NoSuchMethodException e) {
         throw new RuntimeException("Board class " + boardClass.getName() + " lacks an (EntityRobotBase) constructor", e);
      }

      REGISTRY.put(name, this);
   }

   @Override
   public String getID() {
      return this.id;
   }

   @Override
   public void addInformation(ItemStack stack, Player player, List<String> tooltip, boolean advanced) {
      tooltip.add(LocaleUtil.localize("buildcraft.boardRobot" + this.upperName));
      tooltip.add(LocaleUtil.localize("buildcraft.boardRobot" + this.upperName + ".desc"));
   }

   @Override
   public RedstoneBoardRobot create(EntityRobotBase robot) {
      try {
         return this.boardCtor.newInstance(robot);
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException("Failed to instantiate robot board " + this.id, e);
      }
   }

   @Override
   public Identifier getRobotTexture() {
      return this.robotTexture;
   }

   @Override
   public String getItemModelLocation() {
      return "buildcraftrobotics:board/" + this.boardType;
   }

   @Override
   public String getDisplayName() {
      return LocaleUtil.localize("buildcraft.boardRobot" + this.upperName);
   }

   public String getBoardType() {
      return this.boardType;
   }
}
