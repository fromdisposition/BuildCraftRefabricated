/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.robots;

import buildcraft.api.core.BCLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class RobotManager {
   public static IRobotRegistryProvider registryProvider;
   public static ArrayList<Class<? extends AIRobot>> aiRobots = new ArrayList<>();
   private static Map<Class<? extends AIRobot>, String> aiRobotsNames = new HashMap<>();
   private static Map<String, Class<? extends AIRobot>> aiRobotsByNames = new HashMap<>();
   private static Map<String, Class<? extends AIRobot>> aiRobotsByLegacyClassNames = new HashMap<>();
   private static Map<Class<? extends ResourceId>, String> resourceIdNames = new HashMap<>();
   private static Map<String, Class<? extends ResourceId>> resourceIdByNames = new HashMap<>();
   private static Map<String, Class<? extends ResourceId>> resourceIdLegacyClassNames = new HashMap<>();
   private static Map<Class<? extends DockingStation>, String> dockingStationNames = new HashMap<>();
   private static Map<String, Class<? extends DockingStation>> dockingStationByNames = new HashMap<>();

   public static void registerAIRobot(Class<? extends AIRobot> aiRobot, String name) {
      registerAIRobot(aiRobot, name, null);
   }

   public static void registerAIRobot(Class<? extends AIRobot> aiRobot, String name, String legacyClassName) {
      if (aiRobotsByNames.containsKey(name)) {
         BCLog.logger.info("Overriding " + aiRobotsByNames.get(name).getName() + " with " + aiRobot.getName());
      }

      try {
         aiRobot.getConstructor(EntityRobotBase.class);
      } catch (NoSuchMethodException e) {
         throw new RuntimeException("AI class " + aiRobot.getName() + " lacks NBT load constructor! This is a bug!");
      }

      aiRobots.add(aiRobot);
      aiRobotsByNames.put(name, aiRobot);
      aiRobotsNames.put(aiRobot, name);
      if (legacyClassName != null) {
         aiRobotsByLegacyClassNames.put(legacyClassName, aiRobot);
      }
   }

   public static Class<?> getAIRobotByName(String aiRobotName) {
      return aiRobotsByNames.get(aiRobotName);
   }

   public static String getAIRobotName(Class<? extends AIRobot> aiRobotClass) {
      return aiRobotsNames.get(aiRobotClass);
   }

   public static Class<?> getAIRobotByLegacyClassName(String aiRobotLegacyClassName) {
      return aiRobotsByLegacyClassNames.get(aiRobotLegacyClassName);
   }

   public static void registerResourceId(Class<? extends ResourceId> resourceId, String name) {
      registerResourceId(resourceId, name, null);
   }

   public static void registerResourceId(Class<? extends ResourceId> resourceId, String name, String legacyClassName) {
      resourceIdByNames.put(name, resourceId);
      resourceIdNames.put(resourceId, name);
      if (legacyClassName != null) {
         resourceIdLegacyClassNames.put(legacyClassName, resourceId);
      }
   }

   public static Class<?> getResourceIdByName(String resourceIdName) {
      return resourceIdByNames.get(resourceIdName);
   }

   public static String getResourceIdName(Class<? extends ResourceId> resourceIdClass) {
      return resourceIdNames.get(resourceIdClass);
   }

   public static Class<?> getResourceIdByLegacyClassName(String resourceIdLegacyClassName) {
      return resourceIdLegacyClassNames.get(resourceIdLegacyClassName);
   }

   public static void registerDockingStation(Class<? extends DockingStation> dockingStation, String name) {
      dockingStationByNames.put(name, dockingStation);
      dockingStationNames.put(dockingStation, name);
   }

   public static Class<? extends DockingStation> getDockingStationByName(String dockingStationTypeName) {
      return dockingStationByNames.get(dockingStationTypeName);
   }

   public static String getDockingStationName(Class<? extends DockingStation> dockingStation) {
      return dockingStationNames.get(dockingStation);
   }

   static {
      registerResourceId(ResourceIdBlock.class, "resourceIdBlock", "buildcraft.core.robots.ResourceIdBlock");
      registerResourceId(ResourceIdRequest.class, "resourceIdRequest", "buildcraft.core.robots.ResourceIdRequest");
   }
}
