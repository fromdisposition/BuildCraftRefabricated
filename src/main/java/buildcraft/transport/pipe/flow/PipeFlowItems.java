package buildcraft.transport.pipe.flow;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.misc.data.DelayedList;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.net.MessageMultiPipeItem;
import buildcraft.transport.net.PipeItemMessageQueue;
import buildcraft.transport.tile.TilePipeHolder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class PipeFlowItems extends PipeFlow implements IFlowItems {
   private static final double EXTRACT_SPEED = 0.08;
   public static final int NET_CREATE_ITEM = 2;
   private final DelayedList<TravellingItem> items = new DelayedList<>();
   private final List<ItemStack> postDropCache = new ArrayList<>();
   private final PipeFlowItems.PipeExtractJournal extractJournal = new PipeFlowItems.PipeExtractJournal();

   public PipeFlowItems(IPipe pipe) {
      super(pipe);
   }

   public PipeFlowItems(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      ListTag list = nbt.getListOrEmpty("items");
      Level world = pipe.getHolder().getPipeWorld();
      long tickNow = world != null ? world.getGameTime() : 0L;

      for (int i = 0; i < list.size(); i++) {
         if (list.get(i) instanceof CompoundTag compound) {
            TravellingItem item = new TravellingItem(compound, tickNow);
            if (!item.stack.isEmpty()) {
               this.items.add(item.getCurrentDelay(tickNow), item);
            }
         }
      }
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      List<List<TravellingItem>> allItems = this.items.getAllElements();
      ListTag list = new ListTag();
      long tickNow = this.pipe.getHolder().getPipeWorld().getGameTime();

      for (List<TravellingItem> l : allItems) {
         for (TravellingItem item : l) {
            list.add(item.writeToNbt(tickNow));
         }
      }

      nbt.put("items", list);
      return nbt;
   }

   void sendItemDataToClient(TravellingItem item) {
      Level world = this.pipe.getHolder().getPipeWorld();
      if (!world.isClientSide()) {
         int ttd = item.timeToDest > 32767 ? 32767 : item.timeToDest;
         PipeItemMessageQueue.appendTravellingItem(
            world, this.pipe.getHolder().getPipePos(), item.stack, item.stack.getCount(), item.toCenter, item.side, item.colour, ttd
         );
      }
   }

   public void handleClientReceivedItems(List<MessageMultiPipeItem.TravellingItemData> list) {
      for (MessageMultiPipeItem.TravellingItemData data : list) {
         this.handleClientReceivedItem(data);
      }
   }

   private void handleClientReceivedItem(MessageMultiPipeItem.TravellingItemData data) {
      TravellingItem item = new TravellingItem(data.stack);
      item.stackSize = data.stackCount;
      item.toCenter = data.toCenter;
      item.side = data.side;
      item.colour = data.colour;
      item.timeToDest = data.timeToDest;
      item.tickStarted = this.pipe.getHolder().getPipeWorld().getGameTime() + 1L;
      item.tickFinished = item.tickStarted + item.timeToDest;
      this.items.add(item.timeToDest + 1, item);
   }

   @Override
   public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
      super.addDrops(toDrop, fortune);

      for (List<TravellingItem> list : this.items.getAllElements()) {
         for (TravellingItem item : list) {
            if (!item.isPhantom) {
               toDrop.add(item.stack);
            }
         }
      }
   }

   @Override
   public int tryExtractItems(int count, Direction from, @Nullable DyeColor colour, IStackFilter filter, boolean simulate) {
      if (this.pipe.getHolder().getPipeWorld().isClientSide()) {
         throw new IllegalStateException("Cannot extract items on the client side!");
      }

      if (from == null) {
         return 0;
      }

      IPipeHolder holder = this.pipe.getHolder();
      Storage<ItemVariant> storage = PipeNeighborItemAccess.storage(holder, from);
      if (storage == null) {
         return 0;
      }

      PipeNeighborTransfers.ItemProbe probe = PipeNeighborTransfers.findMatchingItem(storage, count, filter);
      if (probe == null) {
         return 0;
      }

      int simulated = PipeNeighborTransfers.extractFromView(probe.view(), probe.resource(), Math.min(probe.available(), count), false);
      if (simulated <= 0) {
         return 0;
      }

      ItemStack possible = probe.resource().toStack(simulated);
      if (possible.getCount() > possible.getMaxStackSize()) {
         possible.setCount(possible.getMaxStackSize());
         count = possible.getMaxStackSize();
      }

      PipeEventItem.TryInsert tryInsert = new PipeEventItem.TryInsert(holder, this, colour, from, possible);
      holder.fireEvent(tryInsert);
      if (!tryInsert.isCanceled() && tryInsert.accepted > 0) {
         count = Math.min(count, tryInsert.accepted);
         int actuallyExtracted = PipeNeighborTransfers.extractFromView(probe.view(), probe.resource(), count, !simulate);
         if (actuallyExtracted <= 0) {
            return 0;
         }

         ItemStack stack = probe.resource().toStack(actuallyExtracted);
         if (!simulate) {
            this.insertItemEvents(stack, colour, 0.08, from);
         }

         return actuallyExtracted;
      } else {
         return 0;
      }
   }

   @Override
   public void sendPhantomItem(@Nonnull ItemStack stack, @Nullable Direction from, @Nullable Direction to, @Nullable DyeColor colour) {
      if (from != null || to != null) {
         Direction face0 = from;
         Direction face1 = from == null ? to : null;
         Direction face2 = to;
         long now = this.pipe.getHolder().getPipeWorld().getGameTime();
         TravellingItem firstItem = new TravellingItem(stack);
         firstItem.isPhantom = true;
         firstItem.toCenter = face1 == null;
         firstItem.colour = colour;
         firstItem.side = face0 == null ? face1 : face0;
         firstItem.speed = 0.08;
         firstItem.genTimings(now, this.getPipeLength(firstItem.side));
         this.items.add(firstItem.timeToDest, firstItem);
         this.sendItemDataToClient(firstItem);
         boolean twoItems = from != null && to != null;
         if (twoItems) {
            TravellingItem secondItem = new TravellingItem(stack);
            secondItem.isPhantom = true;
            secondItem.toCenter = false;
            secondItem.colour = colour;
            secondItem.side = face2;
            secondItem.speed = 0.08;
            secondItem.genTimings(firstItem.tickFinished, this.getPipeLength(secondItem.side));
            this.items.add(secondItem.timeToDest, secondItem);
            this.sendItemDataToClient(secondItem);
         }
      }
   }

   public IInjectable getInjectable(Direction facing) {
      return this;
   }

   @Override
   public boolean canConnect(Direction face, PipeFlow other) {
      return other instanceof IFlowItems;
   }

   @Override
   public boolean canConnect(Direction face, BlockEntity oTile) {
      return PipeNeighborItemAccess.canConnect(this.pipe.getHolder(), face);
   }

   @Override
   public boolean hasSimulationWork() {
      return this.doesContainItems();
   }

   @Override
   public boolean hasClientSimulationWork() {
      return !this.pipe.getHolder().getPipeWorld().isClientSide() && this.doesContainItems();
   }

   @Override
   public void onTick() {
      Level world = this.pipe.getHolder().getPipeWorld();
      List<TravellingItem> toTick = this.items.advance();
      long currentTime = world.getGameTime();

      for (TravellingItem item : toTick) {
         if (item.tickFinished > currentTime) {
            this.items.add((int)(item.tickFinished - currentTime), item);
         } else if (item.isPhantom) {
            this.postDropCache.add(item.stack);
         } else if (!world.isClientSide()) {
            if (item.toCenter) {
               this.onItemReachCenter(item);
            } else {
               this.onItemReachEnd(item);
            }
         }
      }
   }

   @Override
   public void postPluggableTick() {
      this.postDropCache.clear();
   }

   private void onItemReachCenter(TravellingItem item) {
      this.markSaveDirty();
      IPipeHolder holder = this.pipe.getHolder();
      PipeEventItem.ReachCenter reachCenter = new PipeEventItem.ReachCenter(holder, this, item.colour, item.stack, item.side);
      holder.fireEvent(reachCenter);
      if (!reachCenter.getStack().isEmpty()) {
         PipeEventItem.SideCheck sideCheck = new PipeEventItem.SideCheck(holder, this, reachCenter.colour, reachCenter.from, reachCenter.getStack());
         sideCheck.disallow(reachCenter.from);

         for (Direction face : Direction.values()) {
            if (item.tried.contains(face) || !this.pipe.isConnected(face)) {
               sideCheck.disallow(face);
            }
         }

         holder.fireEvent(sideCheck);
         List<EnumSet<Direction>> order = sideCheck.getOrder();
         if (order.isEmpty()) {
            PipeEventItem.TryBounce tryBounce = new PipeEventItem.TryBounce(holder, this, reachCenter.colour, reachCenter.from, reachCenter.getStack());
            holder.fireEvent(tryBounce);
            if (!tryBounce.canBounce) {
               this.dropItem(item.stack, null, item.side.getOpposite(), item.speed);
               return;
            }

            order = ImmutableList.of(EnumSet.of(reachCenter.from));
         }

         PipeEventItem.ItemEntry entry = new PipeEventItem.ItemEntry(reachCenter.colour, reachCenter.getStack(), reachCenter.from);
         PipeEventItem.Split split = new PipeEventItem.Split(holder, this, order, entry);
         holder.fireEvent(split);
         ImmutableList<PipeEventItem.ItemEntry> entries = ImmutableList.copyOf(split.items);
         PipeEventItem.FindDest findDest = new PipeEventItem.FindDest(holder, this, order, entries);
         holder.fireEvent(findDest);
         Level world = holder.getPipeWorld();
         long now = world.getGameTime();
         UnmodifiableIterator var13 = findDest.items.iterator();

         while (var13.hasNext()) {
            PipeEventItem.ItemEntry itemEntry = (PipeEventItem.ItemEntry)var13.next();
            if (!itemEntry.stack.isEmpty()) {
               PipeEventItem.ModifySpeed modifySpeed = new PipeEventItem.ModifySpeed(holder, this, itemEntry, item.speed);
               double newSpeed;
               if (holder.fireEvent(modifySpeed)) {
                  double target = modifySpeed.targetSpeed;
                  double maxDelta = modifySpeed.maxSpeedChange;
                  if (item.speed < target) {
                     newSpeed = Math.min(target, item.speed + maxDelta);
                  } else if (item.speed > target) {
                     newSpeed = Math.max(target, item.speed - maxDelta);
                  } else {
                     newSpeed = item.speed;
                  }
               } else if (item.speed > 0.03) {
                  newSpeed = Math.max(0.03, item.speed - 0.008);
               } else {
                  newSpeed = item.speed;
               }

               List<Direction> destinations = itemEntry.to;
               if (destinations == null || destinations.size() == 0) {
                  destinations = findDest.generateRandomOrder();
               }

               if (destinations.size() == 0) {
                  this.dropItem(itemEntry.stack, null, item.side.getOpposite(), newSpeed);
               } else {
                  TravellingItem newItem = new TravellingItem(itemEntry.stack);
                  newItem.tried.addAll(item.tried);
                  newItem.toCenter = false;
                  newItem.colour = itemEntry.colour;
                  newItem.side = destinations.get(0);
                  newItem.speed = newSpeed;
                  newItem.genTimings(now, this.getPipeLength(newItem.side));
                  this.items.add(newItem.timeToDest, newItem);
                  this.sendItemDataToClient(newItem);
               }
            }
         }
      }
   }

   private void onItemReachEnd(TravellingItem item) {
      this.markSaveDirty();
      IPipeHolder holder = this.pipe.getHolder();
      PipeEventItem.ReachEnd reachEnd = new PipeEventItem.ReachEnd(holder, this, item.colour, item.stack, item.side);
      holder.fireEvent(reachEnd);
      item.colour = reachEnd.colour;
      item.stack = reachEnd.getStack();
      ItemStack excess = item.stack;
      if (!excess.isEmpty()) {
         if (this.pipe.isConnected(item.side)) {
            IPipe.ConnectedType type = this.pipe.getConnectedType(item.side);
            Direction oppositeSide = item.side.getOpposite();
            switch (type) {
               case PIPE:
                  IPipe oPipe = this.pipe.getConnectedPipe(item.side);
                  if (oPipe != null && oPipe.getFlow() instanceof IFlowItems oFlow) {
                     ItemStack before = excess;
                     excess = oFlow.injectItem(excess.copy(), true, oppositeSide, item.colour, item.speed);
                     if (!excess.isEmpty()) {
                        before.shrink(excess.getCount());
                     }
                  }
                  break;
               case TILE:
                  BlockEntity tile = this.pipe.getConnectedTile(item.side);
                  if (tile != null) {
                     Storage<ItemVariant> tileStorage = PipeNeighborItemAccess.storage(holder, item.side);
                     if (tileStorage != null) {
                        int inserted = PipeNeighborTransfers.insertItems(tileStorage, excess, true);
                        if (inserted > 0) {
                           excess.shrink(inserted);
                           if (excess.isEmpty()) {
                              excess = ItemStack.EMPTY;
                           }
                        }
                     }
                  }
            }
         }

         if (excess.isEmpty()) {
            this.postDropCache.add(item.stack);
         } else {
            item.tried.add(item.side);
            item.toCenter = true;
            item.stack = excess;
            item.genTimings(holder.getPipeWorld().getGameTime(), this.getPipeLength(item.side));
            this.items.add(item.timeToDest, item);
            this.sendItemDataToClient(item);
         }
      }
   }

   private void dropItem(ItemStack stack, @Nullable Direction side, Direction motion, double speed) {
      if (stack != null && !stack.isEmpty()) {
         IPipeHolder holder = this.pipe.getHolder();
         Level world = holder.getPipeWorld();
         BlockPos pos = holder.getPipePos();
         double x = pos.getX() + 0.5 + motion.getStepX() * 0.5;
         double y = pos.getY() + 0.5 + motion.getStepY() * 0.5;
         double z = pos.getZ() + 0.5 + motion.getStepZ() * 0.5;
         speed += 0.01;
         speed *= 2.0;
         ItemEntity ent = new ItemEntity(world, x, y, z, stack);
         ent.setDeltaMovement(motion.getStepX() * speed, motion.getStepY() * speed, motion.getStepZ() * speed);
         PipeEventItem.Drop drop = new PipeEventItem.Drop(holder, this, ent);
         holder.fireEvent(drop);
         if (!ent.getItem().isEmpty() && ent.isAlive()) {
            world.addFreshEntity(ent);
         }
      }
   }

   @Override
   public boolean canInjectItems(Direction from) {
      return this.pipe.isConnected(from);
   }

   @Nonnull
   @Override
   public ItemStack injectItem(@Nonnull ItemStack stack, boolean doAdd, Direction from, DyeColor colour, double speed) {
      if (this.pipe.getHolder().getPipeWorld().isClientSide()) {
         throw new IllegalStateException("Cannot inject items on the client side!");
      }

      if (!this.canInjectItems(from)) {
         return stack;
      }

      if (speed < 0.01) {
         speed = 0.01;
      }

      PipeEventItem.TryInsert tryInsert = new PipeEventItem.TryInsert(this.pipe.getHolder(), this, colour, from, stack);
      this.pipe.getHolder().fireEvent(tryInsert);
      if (!tryInsert.isCanceled() && tryInsert.accepted > 0) {
         ItemStack toSplit = stack.copy();
         ItemStack toInsert = toSplit.split(tryInsert.accepted);
         if (doAdd) {
            this.insertItemEvents(toInsert, colour, speed, from);
            this.wakeHolder();
         }

         if (toSplit.isEmpty()) {
            toSplit = StackUtil.EMPTY;
         }

         return toSplit;
      } else {
         return stack;
      }
   }

   @Override
   public void insertItemsForce(@Nonnull ItemStack stack, Direction from, @Nullable DyeColor colour, double speed) {
      Level world = this.pipe.getHolder().getPipeWorld();
      if (world.isClientSide()) {
         throw new IllegalStateException("Cannot inject items on the client side!");
      }

      if (!stack.isEmpty()) {
         if (speed < 0.01) {
            speed = 0.01;
         }

         long now = world.getGameTime();
         TravellingItem item = new TravellingItem(stack);
         if (from == null) {
            for (Direction f : Direction.values()) {
               if (!this.pipe.isConnected(f)) {
                  item.side = f;
                  break;
               }
            }

            if (item.side == null) {
               item.side = Direction.UP;
            }
         } else {
            item.side = from;
         }

         item.toCenter = true;
         item.speed = speed;
         item.colour = colour;
         item.genTimings(now, 0.0);
         if (from != null) {
            item.tried.add(from);
         }

         this.addItemTryMerge(item);
         this.wakeHolder();
      }
   }

   private void wakeHolder() {
      if (this.pipe.getHolder().getPipeTile() instanceof TilePipeHolder holder) {
         holder.wakePipe();
      }
   }

   private void insertItemEvents(@Nonnull ItemStack toInsert, DyeColor colour, double speed, Direction from) {
      IPipeHolder holder = this.pipe.getHolder();
      PipeEventItem.OnInsert onInsert = new PipeEventItem.OnInsert(holder, this, colour, toInsert, from);
      holder.fireEvent(onInsert);
      if (!onInsert.getStack().isEmpty()) {
         Level world = this.pipe.getHolder().getPipeWorld();
         long now = world.getGameTime();
         TravellingItem item = new TravellingItem(toInsert);
         item.side = from;
         item.toCenter = true;
         item.speed = speed;
         item.colour = onInsert.colour;
         item.stack = onInsert.getStack();
         item.genTimings(now, this.getPipeLength(from));
         item.tried.add(from);
         this.addItemTryMerge(item);
      }
   }

   private void addItemTryMerge(TravellingItem item) {
      for (List<TravellingItem> list : this.items.getAllElements()) {
         for (TravellingItem item2 : list) {
            if (item2.mergeWith(item)) {
               this.markSaveDirty();
               return;
            }
         }
      }

      this.items.add(item.timeToDest, item);
      this.markSaveDirty();
      this.sendItemDataToClient(item);
   }

   private void markSaveDirty() {
      if (this.pipe.getHolder().getPipeTile() instanceof TilePipeHolder tile) {
         tile.markPipeSaveDirty();
      }
   }

   @PipeEventHandler
   public static void addTriggers(PipeEventStatement.AddTriggerInternal event) {
      event.triggers.add(BCTransportStatements.TRIGGER_ITEMS_TRAVERSING);
      event.triggers.add(BCTransportStatements.TRIGGER_PIPE_EMPTY);
   }

   public boolean doesContainItems() {
      return this.items.getMaxDelay() > 0 || !this.postDropCache.isEmpty();
   }

   public boolean containsItemMatching(ItemStack filter) {
      if (filter.isEmpty()) {
         return this.doesContainItems();
      }

      for (List<TravellingItem> list : this.items.getAllElements()) {
         for (TravellingItem item : list) {
            if (StackUtil.matchesStackOrList(filter, item.stack)) {
               return true;
            }
         }
      }

      for (ItemStack stack : this.postDropCache) {
         if (StackUtil.matchesStackOrList(filter, stack)) {
            return true;
         }
      }

      return false;
   }

   double getPipeLength(Direction side) {
      if (side == null) {
         return 0.0;
      } else if (this.pipe.isConnected(side)) {
         return this.pipe.getConnectedType(side) == IPipe.ConnectedType.TILE ? 0.75 : 0.5;
      } else {
         return 0.25;
      }
   }

   public void forEachItemForRender(Consumer<TravellingItem> consumer) {
      for (List<TravellingItem> innerList : this.items.getAllElements()) {
         for (TravellingItem item : innerList) {
            consumer.accept(item);
         }
      }
   }

   public Storage<ItemVariant> getItemStorage(Direction side) {
      return new PipeItemInjectStorage(this, side);
   }

   public int extractItemsForExternalSide(Direction side, ItemVariant resource, int amount, TransactionContext transaction) {
      Level world = this.pipe.getHolder().getPipeWorld();
      if (world != null && !world.isClientSide() && !resource.isBlank() && amount > 0) {
         int remaining = amount;
         int extracted = 0;
         this.extractJournal.updateSnapshots(transaction);

         for (List<TravellingItem> bucket : this.items.getAllElements()) {
            Iterator<TravellingItem> iterator = bucket.iterator();

            while (iterator.hasNext() && remaining > 0) {
               TravellingItem item = iterator.next();
               if (isExtractableOnSide(item, side, resource)) {
                  int take = Math.min(remaining, item.stack.getCount());
                  if (take > 0) {
                     extracted += take;
                     remaining -= take;
                     if (take >= item.stack.getCount()) {
                        iterator.remove();
                     } else {
                        item.stack.shrink(take);
                     }
                  }
               }
            }
         }

         if (extracted > 0) {
            this.markSaveDirty();
            this.wakeHolder();
         }

         return extracted;
      } else {
         return 0;
      }
   }

   private static boolean isExtractableOnSide(TravellingItem item, Direction side, ItemVariant resource) {
      return !item.isPhantom && !item.toCenter && item.side == side && !item.stack.isEmpty() && resource.matches(item.stack);
   }

   private static final class ItemsState {
      private final List<List<PipeFlowItems.TravellingItemSnapshot>> elements;
      private final List<ItemStack> postDrop;

      private ItemsState(List<List<PipeFlowItems.TravellingItemSnapshot>> elements, List<ItemStack> postDrop) {
         this.elements = elements;
         this.postDrop = postDrop;
      }

      static PipeFlowItems.ItemsState capture(DelayedList<TravellingItem> items, List<ItemStack> postDropCache) {
         List<List<PipeFlowItems.TravellingItemSnapshot>> copy = new ArrayList<>();
         List<List<TravellingItem>> allItems = items.getAllElements();

         for (int delay = 0; delay < allItems.size(); delay++) {
            List<PipeFlowItems.TravellingItemSnapshot> bucket = new ArrayList<>();

            for (TravellingItem item : allItems.get(delay)) {
               bucket.add(PipeFlowItems.TravellingItemSnapshot.from(delay, item));
            }

            copy.add(bucket);
         }

         List<ItemStack> drops = new ArrayList<>(postDropCache.size());

         for (ItemStack stack : postDropCache) {
            drops.add(stack.copy());
         }

         return new PipeFlowItems.ItemsState(copy, drops);
      }

      void restore(DelayedList<TravellingItem> items, List<ItemStack> postDropCache) {
         items.clear();
         postDropCache.clear();

         for (List<PipeFlowItems.TravellingItemSnapshot> bucket : this.elements) {
            for (PipeFlowItems.TravellingItemSnapshot snapshot : bucket) {
               TravellingItem item = snapshot.toItem();
               items.add(snapshot.delay, item);
            }
         }

         for (ItemStack stack : this.postDrop) {
            postDropCache.add(stack.copy());
         }
      }
   }

   private final class PipeExtractJournal extends SnapshotParticipant<PipeFlowItems.ItemsState> {
      protected PipeFlowItems.ItemsState createSnapshot() {
         return PipeFlowItems.ItemsState.capture(PipeFlowItems.this.items, PipeFlowItems.this.postDropCache);
      }

      protected void readSnapshot(PipeFlowItems.ItemsState snapshot) {
         snapshot.restore(PipeFlowItems.this.items, PipeFlowItems.this.postDropCache);
      }

      protected void onFinalCommit() {
         PipeFlowItems.this.markSaveDirty();
      }
   }

   private static final class TravellingItemSnapshot {
      private final int delay;
      private final ItemStack stack;
      private final DyeColor colour;
      private final boolean toCenter;
      private final double speed;
      private final long tickStarted;
      private final long tickFinished;
      private final int timeToDest;
      private final Direction side;
      private final EnumSet<Direction> tried;
      private final boolean isPhantom;

      private TravellingItemSnapshot(
         int delay,
         ItemStack stack,
         DyeColor colour,
         boolean toCenter,
         double speed,
         long tickStarted,
         long tickFinished,
         int timeToDest,
         Direction side,
         EnumSet<Direction> tried,
         boolean isPhantom
      ) {
         this.delay = delay;
         this.stack = stack;
         this.colour = colour;
         this.toCenter = toCenter;
         this.speed = speed;
         this.tickStarted = tickStarted;
         this.tickFinished = tickFinished;
         this.timeToDest = timeToDest;
         this.side = side;
         this.tried = tried;
         this.isPhantom = isPhantom;
      }

      static PipeFlowItems.TravellingItemSnapshot from(int delay, TravellingItem item) {
         return new PipeFlowItems.TravellingItemSnapshot(
            delay,
            item.stack.copy(),
            item.colour,
            item.toCenter,
            item.speed,
            item.tickStarted,
            item.tickFinished,
            item.timeToDest,
            item.side,
            item.tried.clone(),
            item.isPhantom
         );
      }

      TravellingItem toItem() {
         TravellingItem item = new TravellingItem(this.stack.copy());
         item.colour = this.colour;
         item.toCenter = this.toCenter;
         item.speed = this.speed;
         item.tickStarted = this.tickStarted;
         item.tickFinished = this.tickFinished;
         item.timeToDest = this.timeToDest;
         item.side = this.side;
         item.tried.addAll(this.tried);
         item.isPhantom = this.isPhantom;
         return item;
      }
   }
}
