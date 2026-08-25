/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.robots;

/**
 * Why a robot last failed to make progress. A robot that cannot work looks exactly like one that is resting, so
 * without this the only symptom of a broken setup -- and of a bug -- is "it just sits there". Recorded by the
 * shared AIs at the point they give up and shown by {@code EntityRobot}'s inspect interaction.
 *
 * <p>Live diagnostic state: deliberately not persisted, since it is re-established by the first work cycle after
 * a reload.
 */
public enum RobotIdleReason {
   /** Making progress, or has not finished a cycle yet. */
   WORKING("working"),
   /** The board needs a tool and no reachable station provides one that passes its filters. */
   NO_TOOL("no_tool"),
   /** Nothing to work on inside the leash: no matching block, entity, dropped item or request. */
   NO_WORK("no_work"),
   /** Nothing to load: no reachable station provides the items or fluid the board asked for. */
   NO_SOURCE("no_source"),
   /** Nowhere to put the cargo: no reachable station accepts what the robot is carrying. */
   NO_DESTINATION("no_destination"),
   /** Out of power with no powered station reachable. */
   NO_POWER("no_power");

   /** Localization key of the one-line explanation shown to the player. */
   public final String langKey;

   RobotIdleReason(String name) {
      this.langKey = "buildcraft.robot.idle." + name;
   }
}
