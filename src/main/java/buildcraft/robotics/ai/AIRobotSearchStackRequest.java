package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.StackRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Finds a station whose requester has an outstanding request matching the filter, and reserves it. */
public class AIRobotSearchStackRequest extends AIRobot {
   public StackRequest request;
   private Collection<ItemStack> blackList;
   private IStackFilter filter;

   public AIRobotSearchStackRequest(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotSearchStackRequest(EntityRobotBase robot, IStackFilter filter, Collection<ItemStack> blackList) {
      this(robot);
      this.filter = filter;
      this.blackList = blackList;
   }

   @Override
   public void start() {
      this.startDelegateAI(new AIRobotSearchStation(this.robot, new StationProviderFilter(), this.robot.getZoneToWork()));
   }

   @Override
   public void delegateAIEnded(AIRobot ai) {
      if (ai instanceof AIRobotSearchStation search) {
         if (ai.success()) {
            this.request = this.getOrderFromRequestingStation(search.targetStation, true);
         }

         this.terminate();
      }
   }

   @Override
   public boolean success() {
      return this.request != null;
   }

   private boolean isBlacklisted(ItemStack stack) {
      if (this.blackList != null) {
         for (ItemStack black : this.blackList) {
            if (ItemStack.isSameItemSameComponents(stack, black)) {
               return true;
            }
         }
      }

      return false;
   }

   private StackRequest getOrderFromRequestingStation(DockingStation station, boolean take) {
      for (StackRequest req : this.getAvailableRequests(station)) {
         if (!this.isBlacklisted(req.getStack()) && this.filter.matches(req.getStack())) {
            req.setStation(station);
            if (!take) {
               return req;
            }

            if (this.robot.getRegistry().take(req.getResourceId(this.robot.level()), this.robot)) {
               return req;
            }
         }
      }

      return null;
   }

   private Collection<StackRequest> getAvailableRequests(DockingStation station) {
      List<StackRequest> result = new ArrayList<>();
      IRequestProvider provider = station.getRequestProvider();
      if (provider == null) {
         return result;
      }

      for (int i = 0; i < provider.getRequestsCount(); i++) {
         if (provider.getRequest(i).isEmpty()) {
            continue;
         }

         StackRequest req = new StackRequest(provider, i, provider.getRequest(i));
         req.setStation(station);
         if (!this.robot.getRegistry().isTaken(req.getResourceId(this.robot.level()))) {
            result.add(req);
         }
      }

      return result;
   }

   private class StationProviderFilter implements IStationFilter {
      @Override
      public boolean matches(DockingStation station) {
         return AIRobotSearchStackRequest.this.getOrderFromRequestingStation(station, false) != null;
      }
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ / 5L;
   }
}
