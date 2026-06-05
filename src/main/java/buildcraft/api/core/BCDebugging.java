/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.core;

import java.util.Locale;

public class BCDebugging {
    public enum DebugStatus {
        NONE,
        ENABLE,
        LOGGING_ONLY,
        ALL
    }

    enum DebugLevel {
        LOG,
        COMPLEX;

        final String name = name().toLowerCase(Locale.ROOT);
        boolean isAllOn;
    }

    private static final DebugStatus DEBUG_STATUS;

    static {

        boolean isDev = Boolean.getBoolean("buildcraft.dev");

        String value = System.getProperty("buildcraft.debug");
        if ("enable".equals(value)) DEBUG_STATUS = DebugStatus.ENABLE;
        else if ("all".equals(value)) DEBUG_STATUS = DebugStatus.ALL;
        else if ("disable".equals(value)) {

            DEBUG_STATUS = DebugStatus.NONE;
        } else if ("log".equals(value)) {

            DEBUG_STATUS = DebugStatus.LOGGING_ONLY;
        } else if (isDev) {
            DEBUG_STATUS = DebugStatus.ENABLE;
        } else {

            DEBUG_STATUS = DebugStatus.NONE;
        }

        if (DEBUG_STATUS == DebugStatus.ALL) {
            BCLog.logger.info("[debugger] Debugging automatically enabled for ALL of buildcraft. Prepare for log spam.");
        } else if (DEBUG_STATUS == DebugStatus.LOGGING_ONLY) {
            BCLog.logger.info("[debugger] Debugging automatically enabled for some non-spammy parts of buildcraft.");
        } else if (DEBUG_STATUS == DebugStatus.ENABLE) {
            BCLog.logger.info("[debugger] Debugging not automatically enabled for all of buildcraft. Logging all possible debug options.");
            BCLog.logger.info("              To enable it for only logging messages add \"-Dbuildcraft.debug=log\" to your launch VM arguments");
            BCLog.logger.info("              To enable it for ALL debugging \"-Dbuildcraft.debug=all\" to your launch VM arguments");
            BCLog.logger.info("              To remove this message and all future ones add \"-Dbuildcraft.debug=disable\" to your launch VM arguments");
        }

        DebugLevel.COMPLEX.isAllOn = DEBUG_STATUS == DebugStatus.ALL;
        DebugLevel.LOG.isAllOn = DEBUG_STATUS == DebugStatus.ALL || DEBUG_STATUS == DebugStatus.LOGGING_ONLY;
    }

    public static boolean shouldDebugComplex(String string) {
        return shouldDebug(string, DebugLevel.COMPLEX);
    }

    public static boolean shouldDebugLog(String string) {
        return shouldDebug(string, DebugLevel.LOG);
    }

    private static boolean shouldDebug(String option, DebugLevel type) {
        String prop = getProp(option);
        String actual = System.getProperty(prop);
        if ("false".equals(actual)) {
            BCLog.logger.info("[debugger] Debugging manually disabled for \"" + option + "\" (" + type + ").");
            return false;
        } else if ("true".equals(actual)) {
            BCLog.logger.info("[debugger] Debugging enabled for \"" + option + "\" (" + type + ").");
            return true;
        }
        if (type.isAllOn) {
            BCLog.logger.info("[debugger] Debugging automatically enabled for \"" + option + "\" (" + type + ").");
            return true;
        }
        if ("complex".equals(actual) || type.name.equals(actual)) {
            BCLog.logger.info("[debugger] Debugging enabled for \"" + option + "\" (" + type + ").");
            return true;
        } else if (DEBUG_STATUS != DebugStatus.NONE) {
            StringBuilder log = new StringBuilder();
            log.append("[debugger] To enable debugging for ");
            log.append(option);
            log.append(" add the option \"-D");
            log.append(prop);
            log.append("=true\" to your launch config as a VM argument (").append(type).append(").");
            BCLog.logger.info(log);
        }
        return false;
    }

    private static String getProp(String string) {
        return "buildcraft." + string + ".debug";
    }
}
