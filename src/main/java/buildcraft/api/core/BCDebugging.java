/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import java.util.Locale;

public class BCDebugging {
   private static final BCDebugging.DebugStatus DEBUG_STATUS;

   public static boolean shouldDebugComplex(String string) {
      return shouldDebug(string, BCDebugging.DebugLevel.COMPLEX);
   }

   public static boolean shouldDebugLog(String string) {
      return shouldDebug(string, BCDebugging.DebugLevel.LOG);
   }

   private static boolean shouldDebug(String option, BCDebugging.DebugLevel type) {
      String prop = getProp(option);
      String actual = System.getProperty(prop);
      if ("false".equals(actual)) {
         BCLog.logger.info("[debugger] Debugging manually disabled for \"" + option + "\" (" + type + ").");
         return false;
      }

      if ("true".equals(actual)) {
         BCLog.logger.info("[debugger] Debugging enabled for \"" + option + "\" (" + type + ").");
         return true;
      }

      if (type.isAllOn) {
         BCLog.logger.info("[debugger] Debugging automatically enabled for \"" + option + "\" (" + type + ").");
         return true;
      }

      if (!"complex".equals(actual) && !type.name.equals(actual)) {
         if (DEBUG_STATUS != BCDebugging.DebugStatus.NONE) {
            StringBuilder log = new StringBuilder();
            log.append("[debugger] To enable debugging for ");
            log.append(option);
            log.append(" add the option \"-D");
            log.append(prop);
            log.append("=true\" to your launch config as a VM argument (").append(type).append(").");
            BCLog.logger.info(log);
         }

         return false;
      } else {
         BCLog.logger.info("[debugger] Debugging enabled for \"" + option + "\" (" + type + ").");
         return true;
      }
   }

   private static String getProp(String string) {
      return "buildcraft." + string + ".debug";
   }

   static {
      boolean isDev = Boolean.getBoolean("buildcraft.dev");
      String value = System.getProperty("buildcraft.debug");
      if ("enable".equals(value)) {
         DEBUG_STATUS = BCDebugging.DebugStatus.ENABLE;
      } else if ("all".equals(value)) {
         DEBUG_STATUS = BCDebugging.DebugStatus.ALL;
      } else if ("disable".equals(value)) {
         DEBUG_STATUS = BCDebugging.DebugStatus.NONE;
      } else if ("log".equals(value)) {
         DEBUG_STATUS = BCDebugging.DebugStatus.LOGGING_ONLY;
      } else if (isDev) {
         DEBUG_STATUS = BCDebugging.DebugStatus.ENABLE;
      } else {
         DEBUG_STATUS = BCDebugging.DebugStatus.NONE;
      }

      if (DEBUG_STATUS == BCDebugging.DebugStatus.ALL) {
         BCLog.logger.info("[debugger] Debugging automatically enabled for ALL of buildcraft. Prepare for log spam.");
      } else if (DEBUG_STATUS == BCDebugging.DebugStatus.LOGGING_ONLY) {
         BCLog.logger.info("[debugger] Debugging automatically enabled for some non-spammy parts of buildcraft.");
      } else if (DEBUG_STATUS == BCDebugging.DebugStatus.ENABLE) {
         BCLog.logger.info("[debugger] Debugging not automatically enabled for all of buildcraft. Logging all possible debug options.");
         BCLog.logger.info("              To enable it for only logging messages add \"-Dbuildcraft.debug=log\" to your launch VM arguments");
         BCLog.logger.info("              To enable it for ALL debugging \"-Dbuildcraft.debug=all\" to your launch VM arguments");
         BCLog.logger.info("              To remove this message and all future ones add \"-Dbuildcraft.debug=disable\" to your launch VM arguments");
      }

      BCDebugging.DebugLevel.COMPLEX.isAllOn = DEBUG_STATUS == BCDebugging.DebugStatus.ALL;
      BCDebugging.DebugLevel.LOG.isAllOn = DEBUG_STATUS == BCDebugging.DebugStatus.ALL || DEBUG_STATUS == BCDebugging.DebugStatus.LOGGING_ONLY;
   }

   enum DebugLevel {
      LOG,
      COMPLEX;

      final String name = this.name().toLowerCase(Locale.ROOT);
      boolean isAllOn;
   }

   public enum DebugStatus {
      NONE,
      ENABLE,
      LOGGING_ONLY,
      ALL;
   }
}
