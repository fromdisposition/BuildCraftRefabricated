package buildcraft.robotics.statement;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.robotics.filter.ArrayStackFilter;
import buildcraft.robotics.filter.PassThroughStackFilter;
import buildcraft.robotics.filter.StatementParameterStackFilter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * Port of BuildCraft 7.1.x gate-action filtering (ActionRobotFilter / ActionStationProvideItems). Actions are matched
 * by their unique tag rather than by subclass, since the port models every station action with the generic
 * {@link ActionStation}.
 */
public final class StationActions {
   public static final String WORK_FILTER = "buildcraft:robot.work_filter";
   public static final String PROVIDE_ITEMS = "buildcraft:station.provide_items";
   public static final String ACCEPT_ITEMS = "buildcraft:station.accept_items";

   private StationActions() {
   }

   private static boolean hasTag(StatementSlot slot, String tag) {
      return slot.statement != null && tag.equals(slot.statement.getUniqueTag());
   }

   /** Collects the item stacks configured on the station's work-filter actions. */
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

   /** The combined work filter for a station: pass-through when no work-filter action is configured. */
   public static IStackFilter getGateFilter(DockingStation station) {
      List<ItemStack> stacks = getGateFilterStacks(station);
      return stacks.isEmpty() ? PassThroughStackFilter.INSTANCE : new ArrayStackFilter(stacks.toArray(new ItemStack[0]));
   }

   /** Whether a "provide items" action permits extracting the given stack (no filter = anything allowed). */
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

   /**
    * Whether the station has an active action with the given tag whose parameters intersect the requested filter (or
    * which has no parameters). Mirrors {@code ActionRobotFilter.canInteractWithItem}.
    */
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
