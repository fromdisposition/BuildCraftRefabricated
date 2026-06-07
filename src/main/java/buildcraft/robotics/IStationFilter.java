package buildcraft.robotics;

import buildcraft.api.robots.DockingStation;

/** Predicate used to search for a docking station matching some criterion (item provider, fluid acceptor, etc.). */
public interface IStationFilter {
   boolean matches(DockingStation station);
}
