package buildcraft.transport.pipe.flow;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.items.FluidItemDrops;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventFluid;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.core.BCCoreConfig;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.transfer.fabric.TransferConvert;
import buildcraft.transport.BCTransportStatements;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class PipeFlowFluids extends PipeFlow implements IFlowFluid, IDebuggable {
   private static final int DIRECTION_COOLDOWN = 60;
   private static final int COOLDOWN_INPUT = -60;
   private static final int COOLDOWN_OUTPUT = 60;
   public static final int NET_FLUID_AMOUNTS = 2;
   public static final double FLOW_MULTIPLIER = 0.016;
   private final PipeApi.FluidTransferInfo fluidTransferInfo = PipeApi.getFluidTransferInfo(this.pipe.getDefinition());
   public final int capacity = Math.max(1000, this.fluidTransferInfo.transferPerTick * 10);
   private final Map<EnumPipePart, PipeFlowFluids.Section> sections = new EnumMap<>(EnumPipePart.class);
   @Nonnull
   private FluidStack currentFluid = FluidStack.EMPTY;
   private int currentDelay;
   private SafeTimeTracker tracker;
   private long lastMessage;
   private long lastMessageMinus1;
   public transient Fluid renderCacheFluid;
   public transient Identifier renderCacheSpriteId;
   public transient int renderCacheTintR = 255;
   public transient int renderCacheTintG = 255;
   public transient int renderCacheTintB = 255;
   public transient int renderCacheTintA = 255;
   public transient boolean renderCacheTranslucent;
   public transient boolean renderCacheBcGradient;
   public transient BCEnergyFluidsFabric.FluidEntry renderCacheEntry;
   public transient int renderCacheTexLight;
   public transient int renderCacheTexDark;
   public transient int renderCacheHeat;
   private final FluidPipeMovement.Host fluidMovementHost = new FluidPipeMovement.Host() {
      @Override
      public PipeFlowFluids flow() {
         return PipeFlowFluids.this;
      }

      @Override
      public FluidStack currentFluid() {
         return PipeFlowFluids.this.currentFluid;
      }

      @Override
      public int transferPerTick() {
         return PipeFlowFluids.this.fluidTransferInfo.transferPerTick;
      }

      @Override
      public int capacity() {
         return PipeFlowFluids.this.capacity;
      }

      @Override
      public boolean sectionCanOutput(EnumPipePart part) {
         return PipeFlowFluids.this.sections.get(part).getCurrentDirection().canOutput();
      }

      @Override
      public boolean sectionCanInput(EnumPipePart part) {
         return PipeFlowFluids.this.sections.get(part).getCurrentDirection().canInput();
      }

      @Override
      public int sectionDrain(EnumPipePart part, int max, boolean commit) {
         return PipeFlowFluids.this.sections.get(part).drainInternal(max, commit);
      }

      @Override
      public int sectionFill(EnumPipePart part, int max, boolean commit) {
         return PipeFlowFluids.this.sections.get(part).fill(max, commit);
      }

      @Override
      public int sectionMaxFill(EnumPipePart part) {
         return PipeFlowFluids.this.sections.get(part).getMaxFilled();
      }

      @Override
      public int centerMaxDrain() {
         return PipeFlowFluids.this.sections.get(EnumPipePart.CENTER).getMaxDrained();
      }

      @Override
      public int centerMaxFill() {
         return PipeFlowFluids.this.sections.get(EnumPipePart.CENTER).getMaxFilled();
      }

      @Override
      public int centerAmount() {
         return PipeFlowFluids.this.sections.get(EnumPipePart.CENTER).amount;
      }

      @Override
      public void setSectionCooldownOutput(EnumPipePart part) {
         PipeFlowFluids.this.sections.get(part).ticksInDirection = 60;
      }

      @Override
      public void setSectionCooldownInput(EnumPipePart part) {
         PipeFlowFluids.this.sections.get(part).ticksInDirection = -60;
      }

      @Override
      public boolean hasExternalFluidStorage(Direction direction) {
         return PipeNeighborFluidAccess.canConnect(PipeFlowFluids.this.pipe.getHolder(), direction);
      }

      @Override
      public Storage<FluidVariant> externalFluidStorage(Direction direction) {
         return PipeNeighborFluidAccess.storage(PipeFlowFluids.this.pipe.getHolder(), direction);
      }

      @Override
      public void fireEvent(PipeEvent event) {
         PipeFlowFluids.this.pipe.getHolder().fireEvent(event);
      }
   };

   private SafeTimeTracker getTracker() {
      if (this.tracker == null) {
         this.tracker = new SafeTimeTracker(BCCoreConfig.networkUpdateRate.get(), 4L);
      }

      return this.tracker;
   }

   public PipeFlowFluids(IPipe pipe) {
      super(pipe);

      for (EnumPipePart part : EnumPipePart.VALUES) {
         this.sections.put(part, new PipeFlowFluids.Section(part));
      }
   }

   public PipeFlowFluids(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);

      for (EnumPipePart part : EnumPipePart.VALUES) {
         this.sections.put(part, new PipeFlowFluids.Section(part));
      }

      if (nbt.contains("fluid_id")) {
         String fluidId = nbt.getStringOr("fluid_id", "");
         if (!fluidId.isEmpty()) {
            Identifier fluidRL = Identifier.parse(fluidId);
            Fluid fluid = (Fluid)BuiltInRegistries.FLUID.getValue(fluidRL);
            if (fluid != null && fluid != Fluids.EMPTY) {
               this.setFluid(new FluidStack(fluid, 1000));
            } else {
               this.setFluid(FluidStack.EMPTY);
            }
         } else {
            this.setFluid(FluidStack.EMPTY);
         }
      } else {
         this.setFluid(FluidStack.EMPTY);
      }

      for (EnumPipePart part : EnumPipePart.VALUES) {
         int direction = part.getIndex();
         String key = "tank[" + direction + "]";
         if (nbt.contains(key)) {
            CompoundTag compound = nbt.getCompoundOrEmpty(key);
            this.sections.get(part).readFromNbt(compound);
         }
      }
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      if (!this.currentFluid.isEmpty()) {
         nbt.putString("fluid_id", BuiltInRegistries.FLUID.getKey(this.currentFluid.getFluid()).toString());

         for (EnumPipePart part : EnumPipePart.VALUES) {
            int direction = part.getIndex();
            CompoundTag subTag = new CompoundTag();
            this.sections.get(part).writeToNbt(subTag);
            nbt.put("tank[" + direction + "]", subTag);
         }
      }

      return nbt;
   }

   @Override
   public void readFromNbt(CompoundTag nbt) {
      if (nbt.contains("fluid_id")) {
         String fluidId = nbt.getStringOr("fluid_id", "");
         if (!fluidId.isEmpty()) {
            Identifier fluidRL = Identifier.parse(fluidId);
            Fluid fluid = (Fluid)BuiltInRegistries.FLUID.getValue(fluidRL);
            if (fluid != null && fluid != Fluids.EMPTY) {
               this.currentFluid = new FluidStack(fluid, 1000);
            } else {
               this.currentFluid = FluidStack.EMPTY;
            }
         } else {
            this.currentFluid = FluidStack.EMPTY;
         }
      } else {
         this.currentFluid = FluidStack.EMPTY;
      }

      for (EnumPipePart part : EnumPipePart.VALUES) {
         int direction = part.getIndex();
         String key = "tank[" + direction + "]";
         if (nbt.contains(key)) {
            CompoundTag compound = nbt.getCompoundOrEmpty(key);
            this.sections.get(part).readFromNbt(compound);
         }
      }
   }

   @Override
   public boolean canConnect(Direction face, PipeFlow other) {
      return other instanceof IFlowFluid;
   }

   @Override
   public boolean canConnect(Direction face, BlockEntity oTile) {
      return PipeNeighborFluidAccess.canConnect(this.pipe.getHolder(), face);
   }

   @Nullable
   public Storage<FluidVariant> getFluidStorage(@Nullable Direction facing) {
      PipeFlowFluids.Section section = facing == null ? null : this.sections.get(EnumPipePart.fromFacing(facing));
      return section == null ? null : new PipeFluidSectionStorage(section);
   }

   @Override
   public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
      super.addDrops(toDrop, fortune);
      if (!this.currentFluid.isEmpty()) {
         int totalAmount = 0;

         for (EnumPipePart part : EnumPipePart.VALUES) {
            totalAmount += this.sections.get(part).amount;
         }

         if (totalAmount > 0) {
            FluidItemDrops.addFluidDrops(toDrop, this.currentFluid.copyWithAmount(totalAmount));
         }
      }
   }

   public boolean doesContainFluid() {
      for (EnumPipePart part : EnumPipePart.VALUES) {
         if (this.sections.get(part).amount > 0) {
            return true;
         }
      }

      return false;
   }

   @PipeEventHandler
   public static void addTriggers(PipeEventStatement.AddTriggerInternal event) {
      event.triggers.add(BCTransportStatements.TRIGGER_FLUIDS_TRAVERSING);
      event.triggers.add(BCTransportStatements.TRIGGER_PIPE_EMPTY);
   }

   @Override
   public FluidStack tryExtractFluid(int millibuckets, Direction from, FluidStack filter, boolean simulate) {
      if (from != null && millibuckets > 0) {
         Storage<FluidVariant> storage = PipeNeighborFluidAccess.storage(this.pipe.getHolder(), from);
         if (storage == null) {
            return null;
         }

         PipeFlowFluids.Section section = this.sections.get(EnumPipePart.fromFacing(from));
         PipeFlowFluids.Section middle = this.sections.get(EnumPipePart.CENTER);
         millibuckets = Math.min(millibuckets, this.capacity * 2 - section.amount - middle.amount);
         if (millibuckets <= 0) {
            return null;
         }

         FluidStack resource;
         if (filter != null && !filter.isEmpty()) {
            resource = filter.copyWithAmount(1);
         } else {
            resource = PipeNeighborTransfers.firstFluid(storage);
            if (resource == null || resource.isEmpty()) {
               return null;
            }
         }

         int extracted = PipeNeighborTransfers.extractFluidMb(storage, resource, millibuckets, !simulate);
         if (extracted <= 0) {
            return null;
         }

         FluidStack toAdd = resource.copyWithAmount(extracted);
         if (this.currentFluid.isEmpty() && !simulate) {
            this.setFluid(toAdd);
         }

         int reallyFilled = section.fillInternal(extracted, !simulate);
         int leftOver = extracted - reallyFilled;
         reallyFilled += middle.fillInternal(leftOver, !simulate);
         if (!simulate) {
            section.ticksInDirection = -60;
         }

         if (reallyFilled != extracted) {
            BCLog.logger.warn("[tryExtractFluid] Filled " + reallyFilled + " != extracted " + extracted + " @" + this.pipe.getHolder().getPipePos());
         }

         return toAdd;
      } else {
         return null;
      }
   }

   @Override
   public Object tryExtractFluidAdv(int millibuckets, Direction from, IFluidFilter filter, boolean simulate) {
      if (from != null && millibuckets > 0) {
         FluidStack stackFilter = null;
         if (filter != null) {
            Storage<FluidVariant> storage = PipeNeighborFluidAccess.storage(this.pipe.getHolder(), from);
            if (storage == null) {
               return null;
            }

            for (StorageView<FluidVariant> view : storage) {
               if (!view.isResourceBlank() && view.getAmount() > 0L) {
                  FluidStack probe = TransferConvert.toFluidStack((FluidVariant)view.getResource());
                  if (filter.matches(probe)) {
                     stackFilter = probe;
                     break;
                  }
               }
            }

            if (stackFilter == null) {
               return null;
            }
         }

         return this.tryExtractFluid(millibuckets, from, stackFilter, simulate);
      } else {
         return null;
      }
   }

   @Override
   public int insertFluidsForce(FluidStack fluid, @Nullable Direction from, boolean simulate) {
      PipeFlowFluids.Section s = this.sections.get(EnumPipePart.CENTER);
      if (fluid == null || fluid.isEmpty()) {
         return 0;
      }

      if (!this.currentFluid.isEmpty() && !FluidStack.isSameFluidSameComponents(this.currentFluid, fluid)) {
         return 0;
      }

      if (this.currentFluid.isEmpty() && !simulate) {
         this.setFluid(fluid.copy());
      }

      int filled = s.fill(fluid.getAmount(), !simulate);
      if (filled == 0) {
         return 0;
      }

      if (simulate) {
         return filled;
      }

      if (from != null) {
         this.sections.get(EnumPipePart.fromFacing(from)).ticksInDirection = -60;
      }

      return filled;
   }

   @Nullable
   @Override
   public FluidStack extractFluidsForce(int min, int max, @Nullable Direction section, boolean simulate) {
      if (min > max) {
         throw new IllegalArgumentException("Minimum (" + min + ") > maximum (" + max + ")");
      }

      if (max < 0) {
         return null;
      }

      PipeFlowFluids.Section s = this.sections.get(EnumPipePart.fromFacing(section));
      if (s.amount < min) {
         return null;
      }

      int amount = MathUtil.clamp(s.amount, min, max);
      FluidStack fluid = this.currentFluid.copyWithAmount(amount);
      if (!simulate) {
         s.amount -= amount;
         s.drainInternal(amount, false);
         if (s.amount == 0) {
            boolean isEmpty = true;

            for (PipeFlowFluids.Section s2 : this.sections.values()) {
               isEmpty &= s2.amount == 0;
            }

            if (isEmpty) {
               this.setFluid(FluidStack.EMPTY);
            }
         }
      }

      return fluid;
   }

   @Override
   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      boolean isClient = this.pipe.getHolder().getPipeWorld().isClientSide();
      left.add(" - FluidType = " + (this.currentFluid.isEmpty() ? "empty" : this.currentFluid.getHoverName().getString()));

      for (EnumPipePart part : EnumPipePart.VALUES) {
         PipeFlowFluids.Section section = this.sections.get(part);
         if (section != null) {
            StringBuilder line = new StringBuilder(" - ");
            line.append(part.face == null ? "center" : part.face.getName());
            line.append(" = ");
            int amount = isClient ? section.target : section.amount;
            line.append(amount > 0 ? ChatFormatting.GREEN : "");
            line.append(amount).append("").append(ChatFormatting.RESET).append("mB");
            line.append(" ").append(section.getCurrentDirection()).append(" (").append(section.ticksInDirection).append(")");
            line.append(" [");
            int last = -1;
            int skipped = 0;

            for (int i : section.incoming) {
               if (i != last) {
                  if (skipped > 0) {
                     line.append("...").append(skipped).append("... ");
                     skipped = 0;
                  }

                  last = i;
                  line.append(i).append(", ");
               } else {
                  skipped++;
               }
            }

            if (skipped > 0) {
               line.append("...").append(skipped).append("... ");
            }

            line.append("0]");
            left.add(line.toString());
         }
      }
   }

   public FluidStack getFluidStackForRender() {
      return this.currentFluid.isEmpty() ? null : this.currentFluid;
   }

   public void writeAmountsForRender(float partialTicks, double[] out) {
      boolean clientSide = this.pipe.getHolder().getPipeWorld().isClientSide();
      double pt = partialTicks;
      double invPt = 1.0F - partialTicks;

      for (EnumPipePart part : EnumPipePart.VALUES) {
         PipeFlowFluids.Section s = this.sections.get(part);
         int i = part.getIndex();
         if (clientSide) {
            out[i] = s.clientAmountLast * invPt + s.clientAmountThis * pt;
         } else {
            out[i] = s.amount;
         }
      }
   }

   public void writeOffsetsForRender(float partialTicks, double[] outX, double[] outY, double[] outZ) {
      double pt = partialTicks;
      double invPt = 1.0F - partialTicks;

      for (EnumPipePart part : EnumPipePart.VALUES) {
         PipeFlowFluids.Section s = this.sections.get(part);
         int i = part.getIndex();
         outX[i] = s.offsetLastX * invPt + s.offsetThisX * pt;
         outY[i] = s.offsetLastY * invPt + s.offsetThisY * pt;
         outZ[i] = s.offsetLastZ * invPt + s.offsetThisZ * pt;
      }
   }

   private void setFluid(@Nonnull FluidStack fluid) {
      this.currentFluid = fluid;
      if (!fluid.isEmpty()) {
         this.currentDelay = (int)PipeApi.getFluidTransferInfo(this.pipe.getDefinition()).transferDelayMultiplier;
      } else {
         this.currentDelay = (int)PipeApi.getFluidTransferInfo(this.pipe.getDefinition()).transferDelayMultiplier;
      }

      for (PipeFlowFluids.Section section : this.sections.values()) {
         section.incoming = new int[this.currentDelay];
         section.currentTime = 0;
         section.ticksInDirection = 0;
      }
   }

   @Override
   public boolean hasSimulationWork() {
      return !this.currentFluid.isEmpty();
   }

   @Override
   public boolean hasClientSimulationWork() {
      for (EnumPipePart part : EnumPipePart.VALUES) {
         if (this.sections.get(part).amount > 0) {
            return true;
         }
      }

      return !this.currentFluid.isEmpty();
   }

   @Override
   public void onTick() {
      Level world = this.pipe.getHolder().getPipeWorld();
      if (world.isClientSide()) {
         for (EnumPipePart part : EnumPipePart.VALUES) {
            this.sections.get(part).tickClient();
         }
      } else {
         if (!this.currentFluid.isEmpty()) {
            int totalFluid = 0;
            boolean canOutput = false;

            for (EnumPipePart part : EnumPipePart.VALUES) {
               PipeFlowFluids.Section section = this.sections.get(part);
               section.currentTime = (section.currentTime + 1) % this.currentDelay;
               section.advanceForMovement();
               totalFluid += section.amount;
               if (section.getCurrentDirection().canOutput()) {
                  canOutput = true;
               }
            }

            if (totalFluid == 0) {
               this.setFluid(FluidStack.EMPTY);
            } else {
               if (canOutput) {
                  FluidPipeMovement.moveFromPipe(this.fluidMovementHost);
               }

               FluidPipeMovement.moveFromCenter(this.fluidMovementHost);
               FluidPipeMovement.moveToCenter(this.fluidMovementHost);
            }

            for (EnumPipePart part : EnumPipePart.VALUES) {
               PipeFlowFluids.Section section = this.sections.get(part);
               if (section.ticksInDirection > 0) {
                  section.ticksInDirection--;
               } else if (section.ticksInDirection < 0) {
                  section.ticksInDirection++;
               }
            }
         }

         boolean send = false;

         for (EnumPipePart part : EnumPipePart.VALUES) {
            PipeFlowFluids.Section section = this.sections.get(part);
            if (section.amount != section.lastSentAmount) {
               send = true;
               break;
            }

            PipeFlowFluids.Dir should = PipeFlowFluids.Dir.get(section.ticksInDirection);
            if (section.lastSentDirection != should) {
               send = true;
               break;
            }
         }

         if (send && this.getTracker().markTimeIfDelay(world)) {
            this.sendPayload(2);
         }
      }
   }

   @Override
   public void writePayload(int id, FriendlyByteBuf buffer, Object side) {
      if (id == 2 || id == 0) {
         boolean full = id == 0;
         if (this.currentFluid.isEmpty()) {
            buffer.writeBoolean(false);
         } else {
            buffer.writeBoolean(true);
            buffer.writeUtf(BuiltInRegistries.FLUID.getKey(this.currentFluid.getFluid()).toString());
         }

         for (EnumPipePart part : EnumPipePart.VALUES) {
            PipeFlowFluids.Section section = this.sections.get(part);
            if (full) {
               buffer.writeShort(section.amount);
            } else if (section.amount == section.lastSentAmount) {
               buffer.writeBoolean(false);
            } else {
               buffer.writeBoolean(true);
               buffer.writeShort(section.amount);
               section.lastSentAmount = section.amount;
            }

            PipeFlowFluids.Dir should = PipeFlowFluids.Dir.get(section.ticksInDirection);
            buffer.writeEnum(should);
            section.lastSentDirection = should;
         }
      }
   }

   @Override
   public void readPayload(int id, FriendlyByteBuf buffer, Object side) throws IOException {
      if (id == 2 || id == 0) {
         boolean full = id == 0;
         if (buffer.readBoolean()) {
            String fluidId = buffer.readUtf();
            Identifier fluidRL = Identifier.parse(fluidId);
            Fluid fluid = (Fluid)BuiltInRegistries.FLUID.getValue(fluidRL);
            if (fluid != null && fluid != Fluids.EMPTY) {
               this.currentFluid = new FluidStack(fluid, 1000);
            }
         }

         for (EnumPipePart part : EnumPipePart.VALUES) {
            PipeFlowFluids.Section section = this.sections.get(part);
            if (full || buffer.readBoolean()) {
               section.target = buffer.readShort();
               if (full) {
                  section.clientAmountLast = section.clientAmountThis = section.target;
               }
            }

            PipeFlowFluids.Dir dir = (PipeFlowFluids.Dir)buffer.readEnum(PipeFlowFluids.Dir.class);
            section.ticksInDirection = dir == PipeFlowFluids.Dir.NONE ? 0 : (dir == PipeFlowFluids.Dir.IN ? -60 : 60);
         }

         this.lastMessageMinus1 = this.lastMessage;
         this.lastMessage = this.pipe.getHolder().getPipeWorld().getGameTime();
      }
   }

   enum Dir {
      IN(-1),
      NONE(0),
      OUT(1);

      final byte nbtValue;

      Dir(int nbtValue) {
         this.nbtValue = (byte)nbtValue;
      }

      public boolean isInput() {
         return this == IN;
      }

      public boolean canInput() {
         return this != OUT;
      }

      public boolean isOutput() {
         return this == OUT;
      }

      public boolean canOutput() {
         return this != IN;
      }

      public static PipeFlowFluids.Dir get(int dir) {
         if (dir == 0) {
            return NONE;
         } else {
            return dir < 0 ? IN : OUT;
         }
      }
   }

   class Section {
      final EnumPipePart part;
      int amount = 0;
      int lastSentAmount = 0;
      PipeFlowFluids.Dir lastSentDirection = PipeFlowFluids.Dir.NONE;
      int currentTime = 0;
      int[] incoming = new int[1];
      int incomingTotalCache = 0;
      int ticksInDirection = 0;
      int clientAmountThis;
      int clientAmountLast;
      int target = 0;
      double offsetLastX;
      double offsetLastY;
      double offsetLastZ;
      double offsetThisX;
      double offsetThisY;
      double offsetThisZ;

      Section(EnumPipePart part) {
         this.part = part;
      }

      void writeToNbt(CompoundTag nbt) {
         nbt.putShort("amount", (short)this.amount);
         nbt.putShort("lastSentAmount", (short)this.lastSentAmount);
         nbt.putShort("ticksInDirection", (short)this.ticksInDirection);

         for (int i = 0; i < this.incoming.length; i++) {
            nbt.putShort("in[" + i + "]", (short)this.incoming[i]);
         }
      }

      void readFromNbt(CompoundTag nbt) {
         this.amount = nbt.contains("amount") ? nbt.getShortOr("amount", (short)0) : nbt.getShortOr("capacity", (short)0);
         this.lastSentAmount = nbt.getShortOr("lastSentAmount", (short)0);
         this.ticksInDirection = nbt.getShortOr("ticksInDirection", (short)0);
         this.target = this.amount;
         this.clientAmountThis = this.amount;
         this.clientAmountLast = this.amount;
         this.incomingTotalCache = 0;

         for (int i = 0; i < this.incoming.length; i++) {
            this.incomingTotalCache = this.incomingTotalCache + (this.incoming[i] = nbt.getShortOr("in[" + i + "]", (short)0));
         }
      }

      int getMaxFilled() {
         return FluidPipeSectionLimits.maxFilled(
            PipeFlowFluids.this.capacity, this.amount, PipeFlowFluids.this.fluidTransferInfo.transferPerTick, this.incoming[this.currentTime]
         );
      }

      int getMaxDrained() {
         return FluidPipeSectionLimits.maxDrained(this.amount, this.incomingTotalCache, PipeFlowFluids.this.fluidTransferInfo.transferPerTick);
      }

      int fill(int maxFill, boolean doFill) {
         int amountToFill = Math.min(this.getMaxFilled(), maxFill);
         if (amountToFill <= 0) {
            return 0;
         }

         if (doFill) {
            this.incoming[this.currentTime] = this.incoming[this.currentTime] + amountToFill;
            this.incomingTotalCache += amountToFill;
            this.amount += amountToFill;
         }

         return amountToFill;
      }

      public int fillInternal(int maxFill, boolean doFill) {
         int amountToFill = Math.min(PipeFlowFluids.this.capacity - this.amount, maxFill);
         if (amountToFill <= 0) {
            return 0;
         }

         if (doFill) {
            this.incoming[this.currentTime] = this.incoming[this.currentTime] + amountToFill;
            this.incomingTotalCache += amountToFill;
            this.amount += amountToFill;
         }

         return amountToFill;
      }

      int drainInternal(int maxDrain, boolean doDrain) {
         maxDrain = Math.min(maxDrain, this.getMaxDrained());
         if (maxDrain <= 0) {
            return 0;
         }

         if (doDrain) {
            this.amount -= maxDrain;
         }

         return maxDrain;
      }

      void advanceForMovement() {
         this.incomingTotalCache = this.incomingTotalCache - this.incoming[this.currentTime];
         this.incoming[this.currentTime] = 0;
      }

      void setTime(int current) {
         this.currentTime = current;
      }

      PipeFlowFluids.Dir getCurrentDirection() {
         return this.ticksInDirection == 0 ? PipeFlowFluids.Dir.NONE : (this.ticksInDirection < 0 ? PipeFlowFluids.Dir.IN : PipeFlowFluids.Dir.OUT);
      }

      boolean tickClient() {
         this.clientAmountLast = this.clientAmountThis;
         if (this.target != this.clientAmountThis) {
            int delta = this.target - this.clientAmountThis;
            long msgDelta = PipeFlowFluids.this.lastMessage - PipeFlowFluids.this.lastMessageMinus1;
            msgDelta = MathUtil.clamp((int)msgDelta, 1, 60);
            if (Math.abs(delta) < msgDelta) {
               this.clientAmountThis += delta;
            } else {
               this.clientAmountThis += delta / (int)msgDelta;
            }
         }

         if (this.clientAmountThis == 0 && this.clientAmountLast == 0) {
            this.offsetThisX = 0.0;
            this.offsetThisY = 0.0;
            this.offsetThisZ = 0.0;
         }

         this.offsetLastX = this.offsetThisX;
         this.offsetLastY = this.offsetThisY;
         this.offsetLastZ = this.offsetThisZ;
         if (this.part.face == null) {
            double dirX = 0.0;
            double dirY = 0.0;
            double dirZ = 0.0;

            for (EnumPipePart p : EnumPipePart.FACES) {
               PipeFlowFluids.Section s = PipeFlowFluids.this.sections.get(p);
               if (s.ticksInDirection > 0) {
                  dirX += p.face.getStepX();
                  dirY += p.face.getStepY();
                  dirZ += p.face.getStepZ();
               }
            }

            for (EnumPipePart p : EnumPipePart.FACES) {
               PipeFlowFluids.Section s = PipeFlowFluids.this.sections.get(p);
               if (s.ticksInDirection < 0) {
                  dirX -= p.face.getStepX();
                  dirY -= p.face.getStepY();
                  dirZ -= p.face.getStepZ();
               }
            }

            dirX = Math.signum(dirX);
            dirY = Math.signum(dirY);
            dirZ = Math.signum(dirZ);
            this.offsetThisX += dirX * -0.016;
            this.offsetThisY += dirY * -0.016;
            this.offsetThisZ += dirZ * -0.016;
         } else {
            double mult = Math.signum(this.ticksInDirection);
            double delta = -0.016 * mult;
            this.offsetThisX = this.offsetLastX + this.part.face.getStepX() * delta;
            this.offsetThisY = this.offsetLastY + this.part.face.getStepY() * delta;
            this.offsetThisZ = this.offsetLastZ + this.part.face.getStepZ() * delta;
         }

         double dx = this.offsetThisX >= 0.5 ? -1.0 : (this.offsetThisX <= -0.5 ? 1.0 : 0.0);
         double dy = this.offsetThisY >= 0.5 ? -1.0 : (this.offsetThisY <= -0.5 ? 1.0 : 0.0);
         double dz = this.offsetThisZ >= 0.5 ? -1.0 : (this.offsetThisZ <= -0.5 ? 1.0 : 0.0);
         if (dx != 0.0 || dy != 0.0 || dz != 0.0) {
            this.offsetThisX += dx;
            this.offsetThisY += dy;
            this.offsetThisZ += dz;
            this.offsetLastX += dx;
            this.offsetLastY += dy;
            this.offsetLastZ += dz;
         }

         return this.clientAmountThis > 0 | this.clientAmountLast > 0;
      }

      int size() {
         return 1;
      }

      FluidStack getFluidStack(int index) {
         return index == 0 && !PipeFlowFluids.this.currentFluid.isEmpty() && this.amount > 0 ? PipeFlowFluids.this.currentFluid.copy() : FluidStack.EMPTY;
      }

      long getAmountAsLong(int index) {
         return index == 0 ? this.amount : 0L;
      }

      long getCapacityAsLong(int index, FluidStack fluid) {
         return index == 0 ? PipeFlowFluids.this.capacity : 0L;
      }

      boolean isValid(int index, FluidStack fluid) {
         return index != 0
            ? false
            : PipeFlowFluids.this.currentFluid.isEmpty() || FluidStack.isSameFluidSameComponents(PipeFlowFluids.this.currentFluid, fluid);
      }

      int insert(int index, FluidStack fluid, int insertAmount, TransactionContext transaction) {
         if (index != 0) {
            return 0;
         }

         FluidStack fluidStack = fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(insertAmount);
         if (this.getCurrentDirection().canInput() && PipeFlowFluids.this.pipe.isConnected(this.part.face) && !fluidStack.isEmpty()) {
            PipeEventFluid.TryInsert tryInsert = new PipeEventFluid.TryInsert(
               PipeFlowFluids.this.pipe.getHolder(), PipeFlowFluids.this, this.part.face, fluidStack
            );
            PipeFlowFluids.this.pipe.getHolder().fireEvent(tryInsert);
            if (tryInsert.isCanceled()) {
               return 0;
            }

            if (!PipeFlowFluids.this.currentFluid.isEmpty() && !FluidStack.isSameFluidSameComponents(PipeFlowFluids.this.currentFluid, fluidStack)) {
               return 0;
            }

            if (PipeFlowFluids.this.currentFluid.isEmpty()) {
               PipeFlowFluids.this.setFluid(fluidStack.copy());
            }

            int filled = this.fill(insertAmount, true);
            if (filled > 0) {
               this.ticksInDirection = -60;
            }

            return filled;
         } else {
            return 0;
         }
      }

      int extract(int index, FluidStack fluid, int extractAmount, TransactionContext transaction) {
         if (index != 0) {
            return 0;
         }

         if (!this.getCurrentDirection().canOutput()) {
            return 0;
         }

         if (this.part.face != null && !PipeFlowFluids.this.pipe.isConnected(this.part.face)) {
            return 0;
         }

         if (PipeFlowFluids.this.currentFluid.isEmpty() || this.amount <= 0) {
            return 0;
         }

         if (!FluidStack.isSameFluidSameComponents(PipeFlowFluids.this.currentFluid, fluid)) {
            return 0;
         }

         int drained = this.drainInternal(extractAmount, true);
         if (drained > 0) {
            this.ticksInDirection = 60;
            boolean isEmpty = true;

            for (PipeFlowFluids.Section s2 : PipeFlowFluids.this.sections.values()) {
               if (s2.amount > 0) {
                  isEmpty = false;
                  break;
               }
            }

            if (isEmpty) {
               PipeFlowFluids.this.setFluid(FluidStack.EMPTY);
            }
         }

         return drained;
      }
   }
}
