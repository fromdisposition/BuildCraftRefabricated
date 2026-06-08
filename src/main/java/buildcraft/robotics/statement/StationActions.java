/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.statement;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.robotics.filter.ArrayFluidFilter;
import buildcraft.robotics.filter.ArrayStackOrListFilter;
import buildcraft.robotics.filter.PassThroughStackFilter;
import buildcraft.robotics.filter.StatementParameterStackFilter;
import buildcraft.robotics.path.IFluidFilter;
import buildcraft.robotics.path.PassThroughFluidFilter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class StationActions {
   public static final String WORK_FILTER = "buildcraft:robot.work_filter";
   public static final String FILTER_TOOL = "buildcraft:robot.work_filter_tool";
   public static final String PROVIDE_ITEMS = "buildcraft:station.provide_items";
   public static final String ACCEPT_ITEMS = "buildcraft:station.accept_items";

   private StationActions() {
   }

   private static boolean hasTag(StatementSlot slot, String tag) {
      return slot.statement != null && tag.equals(slot.statement.getUniqueTag());
   }

   
   public static List<ItemStack> getGateFilterStacks(DockingStation station) {
      List<ItemStack> result = new ArrayList<>();
      if (station == null) {
         return result;
      }

      for (StatementSlot slot : station.getActiveActions()) {
         if (hasTag(slot, WORK_FILTER)) {
            for (IStatementParameter param : slot.parameters) {
               if (param instanceof StatementParameterItemStack stackParam) {
                  ItemStack stack = stackParam.getItemStack();
                  if (!stack.isEmpty()) {
                     result.add(stack);
                  }
               }
            }
         }
      }

      return result;
   }

   
   public static List<ItemStack> getGateToolFilterStacks(DockingStation station) {
      return getFilterStacks(station, FILTER_TOOL);
   }

   
   public static IStackFilter getGateFilter(DockingStation station) {
      List<ItemStack> stacks = getGateFilterStacks(station);
      return stacks.isEmpty() ? PassThroughStackFilter.INSTANCE : new ArrayStackOrListFilter(stacks.toArray(new ItemStack[0]));
   }

   
   public static IStackFilter getGateToolFilter(DockingStation station) {
      List<ItemStack> stacks = getGateToolFilterStacks(station);
      return stacks.isEmpty() ? PassThroughStackFilter.INSTANCE : new ArrayStackOrListFilter(stacks.toArray(new ItemStack[0]));
   }

   
   public static IFluidFilter getGateFluidFilter(DockingStation station) {
      List<ItemStack> stacks = getGateFilterStacks(station);
      return stacks.isEmpty() ? PassThroughFluidFilter.INSTANCE : new ArrayFluidFilter(stacks.toArray(new ItemStack[0]));
   }

   
   public static boolean isRobotForbidden(DockingStation station, EntityRobotBase robot) {
      if (station == null || robot == null) {
         return false;
      }

      for (StatementSlot slot : station.getActiveActions()) {
         if (slot.statement instanceof ActionStationForbidRobot forbid) {
            boolean matches = StatementParameterRobot.matchesAny(slot, robot);
            if (forbid.isInvert() ^ matches) {
               return true;
            }
         }
      }

      return false;
   }

   private static List<ItemStack> getFilterStacks(DockingStation station, String tag) {
      List<ItemStack> result = new ArrayList<>();
      if (station == null) {
         return result;
      }

      for (StatementSlot slot : station.getActiveActions()) {
         if (hasTag(slot, tag)) {
            for (IStatementParameter param : slot.parameters) {
               if (param instanceof StatementParameterItemStack stackParam) {
                  ItemStack stack = stackParam.getItemStack();
                  if (!stack.isEmpty()) {
                     result.add(stack);
                  }
               }
            }
         }
      }

      return result;
   }

   
   public static boolean canExtractItem(DockingStation station, ItemStack stack) {
      boolean hasFilter = false;
      if (station == null) {
         return true;
      }

      for (StatementSlot slot : station.getActiveActions()) {
         if (hasTag(slot, PROVIDE_ITEMS)) {
            StatementParameterStackFilter param = new StatementParameterStackFilter(slot.parameters);
            if (param.hasFilter()) {
               hasFilter = true;
               if (param.matches(stack)) {
                  return true;
               }
            }
         }
      }

      return !hasFilter;
   }

   
   public static boolean canInteractWithItem(DockingStation station, IStackFilter filter, String actionTag) {
      if (station == null) {
         return false;
      }

      for (StatementSlot slot : station.getActiveActions()) {
         if (hasTag(slot, actionTag)) {
            StatementParameterStackFilter param = new StatementParameterStackFilter(slot.parameters);
            if (!param.hasFilter() || param.matches(filter)) {
               return true;
            }
         }
      }

      return false;
   }
}
