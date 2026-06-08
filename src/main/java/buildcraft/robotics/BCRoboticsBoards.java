/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.mj.MjAPI;
import buildcraft.robotics.boards.BCBoardNBT;
import buildcraft.robotics.boards.BoardRobotBomber;
import buildcraft.robotics.boards.BoardRobotBuilder;
import buildcraft.robotics.boards.BoardRobotButcher;
import buildcraft.robotics.boards.BoardRobotCarrier;
import buildcraft.robotics.boards.BoardRobotDelivery;
import buildcraft.robotics.boards.BoardRobotFarmer;
import buildcraft.robotics.boards.BoardRobotFluidCarrier;
import buildcraft.robotics.boards.BoardRobotHarvester;
import buildcraft.robotics.boards.BoardRobotKnight;
import buildcraft.robotics.boards.BoardRobotLeaveCutter;
import buildcraft.robotics.boards.BoardRobotLumberjack;
import buildcraft.robotics.boards.BoardRobotMiner;
import buildcraft.robotics.boards.BoardRobotPicker;
import buildcraft.robotics.boards.BoardRobotPlanter;
import buildcraft.robotics.boards.BoardRobotPump;
import buildcraft.robotics.boards.BoardRobotShovelman;
import buildcraft.robotics.boards.BoardRobotStripes;
import buildcraft.robotics.boards.RedstoneBoardRobotEmptyNBT;

public final class BCRoboticsBoards {
   private BCRoboticsBoards() {
   }

   public static void init() {
      if (RedstoneBoardRegistry.instance == null) {
         RedstoneBoardRegistry.instance = new ImplRedstoneBoardRegistry();
      }

      RedstoneBoardRegistry.instance.setEmptyRobotBoard(RedstoneBoardRobotEmptyNBT.INSTANCE);
      long mj = MjAPI.MJ;
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotPicker", "picker", BoardRobotPicker.class, "green"), 8000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotCarrier", "carrier", BoardRobotCarrier.class, "green"), 8000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotFluidCarrier", "fluidCarrier", BoardRobotFluidCarrier.class, "green"), 8000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotLumberjack", "lumberjack", BoardRobotLumberjack.class, "blue"), 32000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotHarvester", "harvester", BoardRobotHarvester.class, "blue"), 32000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:miner", "miner", BoardRobotMiner.class, "blue"), 32000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotPlanter", "planter", BoardRobotPlanter.class, "blue"), 32000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotFarmer", "farmer", BoardRobotFarmer.class, "blue"), 32000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:leave_cutter", "leaveCutter", BoardRobotLeaveCutter.class, "blue"), 32000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotButcher", "butcher", BoardRobotButcher.class, "blue"), 32000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:shovelman", "shovelman", BoardRobotShovelman.class, "blue"), 32000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotPump", "pump", BoardRobotPump.class, "blue"), 32000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotDelivery", "delivery", BoardRobotDelivery.class, "green"), 128000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotKnight", "knight", BoardRobotKnight.class, "red"), 128000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotBomber", "bomber", BoardRobotBomber.class, "red"), 128000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotStripes", "stripes", BoardRobotStripes.class, "yellow"), 128000L * mj);
      RedstoneBoardRegistry.instance.registerBoardType(new BCBoardNBT("buildcraft:boardRobotBuilder", "builder", BoardRobotBuilder.class, "yellow"), 512000L * mj);
   }
}
