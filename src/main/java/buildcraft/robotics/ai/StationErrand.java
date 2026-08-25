/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.RobotIdleReason;
import buildcraft.robotics.path.IFluidFilter;

/**
 * What a robot is going to a station to do. There are exactly four of these -- load or unload, items or fluid --
 * and each answers the same two questions: whether a candidate station can serve it, and what the robot does once
 * docked there. Those answers are all that ever differed between the errands, so they are the only thing stated
 * per errand; finding the station and travelling to it is one shared implementation.
 */
public final class StationErrand {
   public enum Kind {
      LOAD_ITEMS,
      UNLOAD_ITEMS,
      LOAD_FLUIDS,
      UNLOAD_FLUIDS
   }

   private final Kind kind;
   private final IStackFilter stackFilter;
   private final IFluidFilter fluidFilter;
   private final int quantity;

   private StationErrand(Kind kind, IStackFilter stackFilter, IFluidFilter fluidFilter, int quantity) {
      this.kind = kind;
      this.stackFilter = stackFilter;
      this.fluidFilter = fluidFilter;
      this.quantity = quantity;
   }

   public static StationErrand loadItems(IStackFilter filter, int quantity) {
      return new StationErrand(Kind.LOAD_ITEMS, filter, null, quantity);
   }

   public static StationErrand unloadItems() {
      return new StationErrand(Kind.UNLOAD_ITEMS, null, null, 0);
   }

   public static StationErrand loadFluids(IFluidFilter filter) {
      return new StationErrand(Kind.LOAD_FLUIDS, null, filter, 0);
   }

   public static StationErrand unloadFluids() {
      return new StationErrand(Kind.UNLOAD_FLUIDS, null, null, 0);
   }

   public Kind kind() {
      return this.kind;
   }

   public boolean isLoad() {
      return this.kind == Kind.LOAD_ITEMS || this.kind == Kind.LOAD_FLUIDS;
   }

   /** What to tell the player when no station could serve this errand. */
   public RobotIdleReason idleReason() {
      return this.isLoad() ? RobotIdleReason.NO_SOURCE : RobotIdleReason.NO_DESTINATION;
   }

   /** An errand with no filter can never match anything, so it is not worth flying anywhere for. */
   public boolean isValid() {
      return switch (this.kind) {
         case LOAD_ITEMS -> this.stackFilter != null;
         case LOAD_FLUIDS -> this.fluidFilter != null;
         default -> true;
      };
   }

   /** Whether this station can serve the errand right now. Simulation only -- it must change nothing. */
   public boolean possibleAt(EntityRobotBase robot, DockingStation station) {
      return switch (this.kind) {
         case LOAD_ITEMS -> AIRobotLoad.load(robot, station, this.stackFilter, this.quantity, false);
         case UNLOAD_ITEMS -> AIRobotUnload.unload(robot, station, false);
         case LOAD_FLUIDS -> AIRobotLoadFluids.load(robot, station, this.fluidFilter, false) > 0;
         case UNLOAD_FLUIDS -> AIRobotUnloadFluids.unload(robot, station, false) > 0;
      };
   }

   /** The AI that carries the errand out, once the robot has docked at a station that can serve it. */
   public AIRobot work(EntityRobotBase robot) {
      return switch (this.kind) {
         case LOAD_ITEMS -> new AIRobotLoad(robot, this.stackFilter, this.quantity);
         case UNLOAD_ITEMS -> new AIRobotUnload(robot);
         case LOAD_FLUIDS -> new AIRobotLoadFluids(robot, this.fluidFilter);
         case UNLOAD_FLUIDS -> new AIRobotUnloadFluids(robot);
      };
   }
}
