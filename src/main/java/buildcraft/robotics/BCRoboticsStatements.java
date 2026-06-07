package buildcraft.robotics;

import buildcraft.api.statements.ITrigger;
import buildcraft.api.statements.StatementManager;
import buildcraft.robotics.statement.ActionRobotGotoStation;
import buildcraft.robotics.statement.ActionStation;
import buildcraft.robotics.statement.ActionStationForbidRobot;
import buildcraft.robotics.statement.RobotsActionProvider;
import buildcraft.robotics.statement.RobotsTriggerProvider;
import buildcraft.robotics.statement.TriggerRobotInStation;
import buildcraft.robotics.statement.TriggerRobotLinked;
import buildcraft.robotics.statement.TriggerRobotSleep;

public class BCRoboticsStatements {
   public static final TriggerRobotSleep TRIGGER_ROBOT_SLEEP = new TriggerRobotSleep();
   public static final TriggerRobotInStation TRIGGER_ROBOT_IN_STATION = new TriggerRobotInStation();
   public static final TriggerRobotLinked TRIGGER_ROBOT_LINKED = new TriggerRobotLinked(false);
   public static final TriggerRobotLinked TRIGGER_ROBOT_RESERVED = new TriggerRobotLinked(true);
   public static final ITrigger[] TRIGGER_ROBOT_LINKED_ALL = new ITrigger[]{TRIGGER_ROBOT_LINKED, TRIGGER_ROBOT_RESERVED};

   public static final ActionStation ACTION_ROBOT_WAKEUP = new ActionStation(
      "buildcraft:robot.wakeup", "gate.action.robot.wakeup", BCRoboticsSprites.ACTION_ROBOT_WAKEUP, 0, false
   );
   public static final ActionStation ACTION_ROBOT_FILTER = new ActionStation(
      "buildcraft:robot.work_filter", "gate.action.robot.filter", BCRoboticsSprites.ACTION_ROBOT_FILTER, 3, true
   );
   public static final ActionStation ACTION_ROBOT_FILTER_TOOL = new ActionStation(
      "buildcraft:robot.work_filter_tool", "gate.action.robot.filter_tool", BCRoboticsSprites.ACTION_ROBOT_FILTER_TOOL, 1, true
   );
   public static final ActionStationForbidRobot ACTION_STATION_FORBID_ROBOT = new ActionStationForbidRobot(false);
   public static final ActionStationForbidRobot ACTION_STATION_FORCE_ROBOT = new ActionStationForbidRobot(true);
   public static final ActionRobotGotoStation ACTION_ROBOT_GOTO_STATION = new ActionRobotGotoStation();
   public static final ActionStation ACTION_ROBOT_WORK_IN_AREA = new ActionStation(
      "buildcraft:robot.work_in_area", "gate.action.robot.work_in_area", BCRoboticsSprites.ACTION_ROBOT_WORK_IN_AREA, 1, true
   );
   public static final ActionStation ACTION_ROBOT_LOAD_UNLOAD_AREA = new ActionStation(
      "buildcraft:robot.load_unload_area", "gate.action.robot.load_unload_area", BCRoboticsSprites.ACTION_ROBOT_LOAD_UNLOAD_AREA, 1, true
   );
   public static final ActionStation ACTION_STATION_PROVIDE_ITEMS = new ActionStation(
      "buildcraft:station.provide_items", "gate.action.station.provide_items", BCRoboticsSprites.ACTION_STATION_PROVIDE_ITEMS, 3, true
   );
   public static final ActionStation ACTION_STATION_REQUEST_ITEMS = new ActionStation(
      "buildcraft:station.request_items", "gate.action.station.request_items", BCRoboticsSprites.ACTION_STATION_REQUEST_ITEMS, 3, true
   );
   public static final ActionStation ACTION_STATION_ACCEPT_ITEMS = new ActionStation(
      new String[]{"buildcraft:station.accept_items", "buildcraft:station.drop_in_pipe"},
      "gate.action.station.accept_items",
      BCRoboticsSprites.ACTION_STATION_ACCEPT_ITEMS,
      0,
      false
   );
   public static final ActionStation ACTION_STATION_PROVIDE_FLUIDS = new ActionStation(
      "buildcraft:station.provide_fluids", "gate.action.station.provide_fluids", BCRoboticsSprites.ACTION_STATION_PROVIDE_FLUIDS, 1, true
   );
   public static final ActionStation ACTION_STATION_ACCEPT_FLUIDS = new ActionStation(
      "buildcraft:station.accept_fluids", "gate.action.station.accept_fluids", BCRoboticsSprites.ACTION_STATION_ACCEPT_FLUIDS, 0, false
   );
   public static final ActionStation ACTION_STATION_MACHINE_REQUEST = new ActionStation(
      "buildcraft:station.machine_request_items", "gate.action.station.machine_request_items", BCRoboticsSprites.ACTION_STATION_MACHINE_REQUEST, 0, false
   );

   public static void preInit() {
      StatementManager.registerStatement(TRIGGER_ROBOT_SLEEP);
      StatementManager.registerStatement(TRIGGER_ROBOT_IN_STATION);
      StatementManager.registerStatement(TRIGGER_ROBOT_LINKED);
      StatementManager.registerStatement(TRIGGER_ROBOT_RESERVED);
      StatementManager.registerStatement(ACTION_ROBOT_WAKEUP);
      StatementManager.registerStatement(ACTION_ROBOT_FILTER);
      StatementManager.registerStatement(ACTION_ROBOT_FILTER_TOOL);
      StatementManager.registerStatement(ACTION_STATION_FORBID_ROBOT);
      StatementManager.registerStatement(ACTION_STATION_FORCE_ROBOT);
      StatementManager.registerStatement(ACTION_ROBOT_GOTO_STATION);
      StatementManager.registerStatement(ACTION_ROBOT_WORK_IN_AREA);
      StatementManager.registerStatement(ACTION_ROBOT_LOAD_UNLOAD_AREA);
      StatementManager.registerStatement(ACTION_STATION_PROVIDE_ITEMS);
      StatementManager.registerStatement(ACTION_STATION_REQUEST_ITEMS);
      StatementManager.registerStatement(ACTION_STATION_ACCEPT_ITEMS);
      StatementManager.registerStatement(ACTION_STATION_PROVIDE_FLUIDS);
      StatementManager.registerStatement(ACTION_STATION_ACCEPT_FLUIDS);
      StatementManager.registerStatement(ACTION_STATION_MACHINE_REQUEST);
      StatementManager.registerTriggerProvider(new RobotsTriggerProvider());
      StatementManager.registerActionProvider(new RobotsActionProvider());
   }
}
