package buildcraft.fabric;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.robots.RobotManager;
import buildcraft.lib.recipe.ProgrammingRecipeRegistry;
import buildcraft.robotics.BCRoboticsBlockEntities;
import buildcraft.robotics.BCRoboticsBlocks;
import buildcraft.robotics.BCRoboticsBoards;
import buildcraft.robotics.BCRoboticsEntities;
import buildcraft.robotics.ai.AIRobotMain;
import buildcraft.robotics.ai.AIRobotSleep;
import buildcraft.robotics.boards.BoardRobotBomber;
import buildcraft.robotics.boards.BoardRobotBuilder;
import buildcraft.robotics.boards.BoardRobotButcher;
import buildcraft.robotics.boards.BoardRobotCarrier;
import buildcraft.robotics.boards.BoardRobotDelivery;
import buildcraft.robotics.boards.BoardRobotEmpty;
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
import buildcraft.robotics.entity.EntityRobot;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.BCRoboticsPlugs;
import buildcraft.robotics.BCRoboticsStatements;
import buildcraft.robotics.BoardProgrammingRecipe;
import buildcraft.robotics.robot.DockingStationPipe;
import buildcraft.robotics.robot.RobotRegistryProvider;
import buildcraft.robotics.tile.TileRequester;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

public final class BCRoboticsFabric {
   private BCRoboticsFabric() {
   }

   public static void register() {
      BCRoboticsBlocks.register();
      BCRoboticsItems.register();
      BCRoboticsBlockEntities.register();
      BCRoboticsMenuTypes.register();
      BCRoboticsEntities.register();
      FabricDefaultAttributeRegistry.register(BCRoboticsEntities.ROBOT, EntityRobot.createAttributes());
      BCRoboticsBoards.init();
      RobotManager.registerAIRobot(AIRobotMain.class, "buildcraft:aiRobotMain");
      RobotManager.registerAIRobot(AIRobotSleep.class, "buildcraft:aiRobotSleep");
      RobotManager.registerAIRobot(BoardRobotEmpty.class, "buildcraft:boardRobotEmpty");
      registerNavigationAI();
      registerBoards();
      RobotManager.registryProvider = new RobotRegistryProvider();
      RobotManager.registerDockingStation(DockingStationPipe.class, "dockingStationPipe");
      BCRoboticsPlugs.preInit();
      BCRoboticsStatements.preInit();
      registerNativeTransfer();
      if (BuildcraftRecipeRegistry.programmingTable == null) {
         BuildcraftRecipeRegistry.programmingTable = ProgrammingRecipeRegistry.INSTANCE;
      }

      BuildcraftRecipeRegistry.programmingTable.addRecipe(new BoardProgrammingRecipe());
      buildcraft.robotics.RobotIntegrationRecipe.init();
   }

   private static void registerNavigationAI() {
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotStraightMoveTo.class, "buildcraft:aiRobotStraightMoveTo");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotGotoBlock.class, "buildcraft:aiRobotGotoBlock");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotGotoStation.class, "buildcraft:aiRobotGotoStation");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotGoAndLinkToDock.class, "buildcraft:aiRobotGoAndLinkToDock");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotGotoSleep.class, "buildcraft:aiRobotGotoSleep");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotShutdown.class, "buildcraft:aiRobotShutdown");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotRecharge.class, "buildcraft:aiRobotRecharge");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotSearchStation.class, "buildcraft:aiRobotSearchStation");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotSearchAndGotoStation.class, "buildcraft:aiRobotSearchAndGotoStation");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotLoad.class, "buildcraft:aiRobotLoad");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotUnload.class, "buildcraft:aiRobotUnload");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotGotoStationToLoad.class, "buildcraft:aiRobotGotoStationToLoad");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotGotoStationAndLoad.class, "buildcraft:aiRobotGotoStationAndLoad");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotGotoStationToUnload.class, "buildcraft:aiRobotGotoStationToUnload");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotGotoStationAndUnload.class, "buildcraft:aiRobotGotoStationAndUnload");
      RobotManager.registerAIRobot(buildcraft.robotics.ai.AIRobotDisposeItems.class, "buildcraft:aiRobotDisposeItems");
   }

   private static void registerBoards() {
      RobotManager.registerAIRobot(BoardRobotPicker.class, "buildcraft:boardRobotPicker");
      RobotManager.registerAIRobot(BoardRobotCarrier.class, "buildcraft:boardRobotCarrier");
      RobotManager.registerAIRobot(BoardRobotFluidCarrier.class, "buildcraft:boardRobotFluidCarrier");
      RobotManager.registerAIRobot(BoardRobotLumberjack.class, "buildcraft:boardRobotLumberjack");
      RobotManager.registerAIRobot(BoardRobotHarvester.class, "buildcraft:boardRobotHarvester");
      RobotManager.registerAIRobot(BoardRobotMiner.class, "buildcraft:boardRobotMiner");
      RobotManager.registerAIRobot(BoardRobotPlanter.class, "buildcraft:boardRobotPlanter");
      RobotManager.registerAIRobot(BoardRobotFarmer.class, "buildcraft:boardRobotFarmer");
      RobotManager.registerAIRobot(BoardRobotLeaveCutter.class, "buildcraft:boardRobotLeaveCutter");
      RobotManager.registerAIRobot(BoardRobotButcher.class, "buildcraft:boardRobotButcher");
      RobotManager.registerAIRobot(BoardRobotShovelman.class, "buildcraft:boardRobotShovelman");
      RobotManager.registerAIRobot(BoardRobotPump.class, "buildcraft:boardRobotPump");
      RobotManager.registerAIRobot(BoardRobotDelivery.class, "buildcraft:boardRobotDelivery");
      RobotManager.registerAIRobot(BoardRobotKnight.class, "buildcraft:boardRobotKnight");
      RobotManager.registerAIRobot(BoardRobotBomber.class, "buildcraft:boardRobotBomber");
      RobotManager.registerAIRobot(BoardRobotStripes.class, "buildcraft:boardRobotStripes");
      RobotManager.registerAIRobot(BoardRobotBuilder.class, "buildcraft:boardRobotBuilder");
   }

   private static void registerNativeTransfer() {
      if (BCRoboticsBlockEntities.REQUESTER != null) {
         ItemStorage.SIDED
            .registerForBlockEntity(
               (blockEntity, direction) -> blockEntity instanceof TileRequester requester ? requester.getSidedItemStorage(direction) : null,
               BCRoboticsBlockEntities.REQUESTER
            );
      }
   }
}
