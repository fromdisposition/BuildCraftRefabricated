/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.ai;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.robotics.statement.StationActions;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

public class AIRobotLoad extends AIRobot {
   public static final int ANY_QUANTITY = -1;

   private IStackFilter filter;
   private int quantity;
   private int waitedCycles;

   public AIRobotLoad(EntityRobotBase robot) {
      super(robot);
   }

   public AIRobotLoad(EntityRobotBase robot, IStackFilter filter, int quantity) {
      super(robot);
      this.filter = filter;
      this.quantity = quantity;
   }

   @Override
   public void update() {
      if (this.filter == null) {
         this.terminate();
         return;
      }

      this.waitedCycles++;
      if (this.waitedCycles > 40) {
         this.setSuccess(load(this.robot, this.robot.getDockingStation(), this.filter, this.quantity, true));
         this.terminate();
      }
   }

   /**
    * Move matching items from the station's inventory into the robot.
    *
    * <p>Both sides are rolled back together unless {@code doLoad} commits: the source storage by the transaction,
    * the robot by an inventory snapshot. Station searches probe every candidate station with {@code doLoad=false},
    * so a simulation that could leave either side changed is an item dupe -- here it is structurally impossible.
    */
   public static boolean load(EntityRobotBase robot, DockingStation station, IStackFilter filter, int quantity, boolean doLoad) {
      if (station == null || filter == null || !(robot instanceof EntityRobot entityRobot)) {
         return false;
      }

      Storage<ItemVariant> source = station.getItemInput();
      if (source == null || !StationActions.canInteractWithItem(station, filter, StationActions.PROVIDE_ITEMS)) {
         return false;
      }

      ItemStack[] restore = entityRobot.snapshotInventory();
      int wanted = quantity == ANY_QUANTITY ? Integer.MAX_VALUE : quantity;
      int loaded = 0;
      boolean consistent = true;

      try (Transaction transaction = Transaction.openOuter()) {
         for (StorageView<ItemVariant> view : source) {
            if (loaded >= wanted) {
               break;
            }

            if (view.isResourceBlank() || view.getAmount() <= 0L) {
               continue;
            }

            ItemVariant variant = view.getResource();
            ItemStack probe = variant.toStack();
            if (!filter.matches(probe) || !StationActions.canExtractItem(station, probe)) {
               continue;
            }

            // One stack per view at most: it keeps roomFor's accounting honest and can never build a stack
            // larger than the item allows, whatever a modded storage reports as one view.
            int cap = (int) Math.min(Math.min(view.getAmount(), wanted - loaded), probe.getMaxStackSize());
            int take = Math.min(cap, entityRobot.roomFor(variant.toStack(cap)));
            if (take <= 0) {
               continue;
            }

            int extracted = (int) view.extract(variant, take, transaction);
            if (extracted <= 0) {
               continue;
            }

            if (!entityRobot.receiveItem(null, variant.toStack(extracted)).isEmpty()) {
               // The robot took less than roomFor promised. Committing here would void the difference, so
               // abandon the whole transfer instead; both sides roll back and the next cycle retries.
               consistent = false;
               break;
            }

            loaded += extracted;
         }

         if (doLoad && consistent && loaded > 0) {
            transaction.commit();
            return true;
         }
      }

      entityRobot.restoreInventory(restore);
      return consistent && loaded > 0;
   }

   /** Take up to {@code max} of one matching item -- a tool is one, a consumable like seed is a whole stack. */
   public static ItemStack take(DockingStation station, IStackFilter filter, int max, boolean doTake) {
      if (station == null || filter == null) {
         return ItemStack.EMPTY;
      }

      Storage<ItemVariant> source = station.getItemInput();
      if (source == null || !StationActions.canInteractWithItem(station, filter, StationActions.PROVIDE_ITEMS)) {
         return ItemStack.EMPTY;
      }

      try (Transaction transaction = Transaction.openOuter()) {
         for (StorageView<ItemVariant> view : source) {
            if (view.isResourceBlank() || view.getAmount() <= 0L) {
               continue;
            }

            ItemVariant variant = view.getResource();
            ItemStack probe = variant.toStack();
            if (!filter.matches(probe) || !StationActions.canExtractItem(station, probe)) {
               continue;
            }

            int want = Math.min(max, probe.getMaxStackSize());
            int taken = (int) view.extract(variant, want, transaction);
            if (taken <= 0) {
               continue;
            }

            if (doTake) {
               transaction.commit();
            }

            return variant.toStack(taken);
         }
      }

      return ItemStack.EMPTY;
   }

   @Override
   public long getPowerCost() {
      return MjAPI.MJ * 8L / 10L;
   }
}
