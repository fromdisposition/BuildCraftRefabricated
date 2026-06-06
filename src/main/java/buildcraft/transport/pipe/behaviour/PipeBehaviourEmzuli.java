package buildcraft.transport.pipe.behaviour;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.pipe.IFlowItems;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventActionActivate;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.container.ContainerEmzuliPipe_BC8;
import buildcraft.transport.statements.ActionExtractionPreset;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class PipeBehaviourEmzuli extends PipeBehaviourWood {
   public final EnumMap<PipeBehaviourEmzuli.SlotIndex, DyeColor> slotColours = new EnumMap<>(PipeBehaviourEmzuli.SlotIndex.class);
   public final ItemHandlerSimple invFilters = new ItemHandlerSimple(4);
   private final EnumSet<PipeBehaviourEmzuli.SlotIndex> activeSlots;
   private final byte[] activatedTtl = new byte[PipeBehaviourEmzuli.SlotIndex.VALUES.length];
   private PipeBehaviourEmzuli.SlotIndex currentSlot = null;
   private final IStackFilter filter = this::filterMatches;

   public PipeBehaviourEmzuli(IPipe pipe) {
      super(pipe);
      this.activeSlots = EnumSet.noneOf(PipeBehaviourEmzuli.SlotIndex.class);
   }

   public PipeBehaviourEmzuli(IPipe pipe, CompoundTag nbt) {
      super(pipe, nbt);
      CompoundTag filtersTag = nbt.getCompoundOrEmpty("Filters");
      if (!filtersTag.isEmpty()) {
         this.invFilters.deserializeNBT(filtersTag);
      }

      this.activeSlots = EnumSet.noneOf(PipeBehaviourEmzuli.SlotIndex.class);
      this.currentSlot = NBTUtilBC.readEnum(nbt.get("currentSlot"), PipeBehaviourEmzuli.SlotIndex.class);

      for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
         byte c = nbt.getByteOr("slotColors[" + index.ordinal() + "]", (byte)0);
         if (c > 0 && c <= 16) {
            this.slotColours.put(index, DyeColor.byId(c - 1));
         }
      }
   }

   @Override
   public CompoundTag writeToNbt() {
      CompoundTag nbt = super.writeToNbt();
      nbt.put("Filters", this.invFilters.serializeNBT());
      if (this.currentSlot != null) {
         nbt.put("currentSlot", NBTUtilBC.writeEnum(this.currentSlot));
      }

      for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
         DyeColor c = this.slotColours.get(index);
         nbt.putByte("slotColors[" + index.ordinal() + "]", (byte)(c == null ? 0 : c.getId() + 1));
      }

      return nbt;
   }

   @Override
   public void readFromNbt(CompoundTag nbt) {
      super.readFromNbt(nbt);
      this.invFilters.deserializeNBT(nbt.getCompoundOrEmpty("Filters"));
      this.currentSlot = NBTUtilBC.readEnum(nbt.get("currentSlot"), PipeBehaviourEmzuli.SlotIndex.class);

      for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
         byte c = nbt.getByteOr("slotColors[" + index.ordinal() + "]", (byte)0);
         if (c > 0 && c <= 16) {
            this.slotColours.put(index, DyeColor.byId(c - 1));
         } else {
            this.slotColours.remove(index);
         }
      }
   }

   @Override
   public void writePayload(FriendlyByteBuf buffer) {
      super.writePayload(buffer);

      for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
         DyeColor c = this.slotColours.get(index);
         buffer.writeByte(c == null ? -1 : c.getId());
      }

      int mask = 0;

      for (PipeBehaviourEmzuli.SlotIndex index : this.activeSlots) {
         mask |= 1 << index.ordinal();
      }

      buffer.writeByte(mask);
      buffer.writeByte(this.currentSlot == null ? -1 : this.currentSlot.ordinal());
   }

   @Override
   public void readPayload(FriendlyByteBuf buffer, Object ctx) throws IOException {
      super.readPayload(buffer, ctx);

      for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
         int c = buffer.readByte();
         if (c >= 0 && c < 16) {
            this.slotColours.put(index, DyeColor.byId(c));
         } else {
            this.slotColours.remove(index);
         }
      }

      int mask = buffer.readUnsignedByte();
      this.activeSlots.clear();

      for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
         if ((mask & 1 << index.ordinal()) != 0) {
            this.activeSlots.add(index);
         }
      }

      int slotOrd = buffer.readByte();
      this.currentSlot = slotOrd >= 0 && slotOrd < PipeBehaviourEmzuli.SlotIndex.VALUES.length ? PipeBehaviourEmzuli.SlotIndex.VALUES[slotOrd] : null;
   }

   @Override
   protected int extractItems(IFlowItems flow, Direction dir, int count, boolean simulate) {
      if (this.currentSlot == null && this.activeSlots.size() > 0) {
         this.currentSlot = this.getNextSlot();
      }

      if (this.currentSlot == null) {
         return 0;
      }

      int extracted = flow.tryExtractItems(count, dir, this.slotColours.get(this.currentSlot), this.filter, simulate);
      if (extracted > 0 && !simulate) {
         this.currentSlot = this.getNextSlot();
         this.pipe.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
      }

      return extracted;
   }

   private boolean filterMatches(ItemStack stack) {
      if (this.currentSlot == null) {
         return false;
      }

      ItemStack current = this.invFilters.getStackInSlot(this.currentSlot.ordinal());
      return StackUtil.isMatchingItemOrList(current, stack);
   }

   @Override
   public boolean hasSimulationWork() {
      if (this.pipe.getHolder().getPipeWorld().isClientSide()) {
         return false;
      }

      for (byte ttl : this.activatedTtl) {
         if (ttl > 0) {
            return true;
         }
      }

      return !this.activeSlots.isEmpty();
   }

   @Override
   public void onTick() {
      super.onTick();
      if (!this.pipe.getHolder().getPipeWorld().isClientSide()) {
         for (PipeBehaviourEmzuli.SlotIndex index : PipeBehaviourEmzuli.SlotIndex.VALUES) {
            byte val = this.activatedTtl[index.ordinal()];
            if (val > 0) {
               this.activatedTtl[index.ordinal()] = --val;
            }

            if (val == 0) {
               this.activeSlots.remove(index);
               if (this.currentSlot == index) {
                  this.currentSlot = this.getNextSlot();
                  this.pipe.getHolder().scheduleNetworkUpdate(IPipeHolder.PipeMessageReceiver.BEHAVIOUR);
               }
            }
         }
      }
   }

   private PipeBehaviourEmzuli.SlotIndex getNextSlot() {
      PipeBehaviourEmzuli.SlotIndex current = this.currentSlot == null ? PipeBehaviourEmzuli.SlotIndex.CROSS : this.currentSlot;
      int i = PipeBehaviourEmzuli.SlotIndex.VALUES.length;

      while (i-- > 0) {
         current = current.next();
         if (this.activeSlots.contains(current) && !this.invFilters.getStackInSlot(current.ordinal()).isEmpty()) {
            return current;
         }
      }

      return null;
   }

   public PipeBehaviourEmzuli.SlotIndex getCurrentSlot() {
      return this.currentSlot;
   }

   public EnumSet<PipeBehaviourEmzuli.SlotIndex> getActiveSlots() {
      return this.activeSlots;
   }

   @Override
   public boolean onPipeActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
      if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
         final PipeBehaviourEmzuli self = this;
         serverPlayer.openMenu(new MenuProvider() {
            public Component getDisplayName() {
               return Component.translatable("gui.pipes.emzuli.title");
            }

            public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player p) {
               return new ContainerEmzuliPipe_BC8(containerId, playerInv, self);
            }
         });
      }

      return true;
   }

   @Override
   public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
      for (int i = 0; i < this.invFilters.getSlots(); i++) {
         ItemStack stack = this.invFilters.getStackInSlot(i);
         if (!stack.isEmpty()) {
            toDrop.add(stack);
         }
      }
   }

   @PipeEventHandler
   public void addActions(PipeEventStatement.AddActionInternal event) {
      Collections.addAll(event.actions, BCTransportStatements.ACTION_EXTRACTION_PRESET);
   }

   @PipeEventHandler
   @Override
   public void onActionActivate(PipeEventActionActivate event) {
      if (event.action instanceof ActionExtractionPreset preset) {
         this.activeSlots.add(preset.index);
         this.activatedTtl[preset.index.ordinal()] = 2;
      }
   }

   public enum SlotIndex {
      SQUARE(DyeColor.RED),
      CIRCLE(DyeColor.GREEN),
      TRIANGLE(DyeColor.BLUE),
      CROSS(DyeColor.YELLOW);

      public static final PipeBehaviourEmzuli.SlotIndex[] VALUES = values();
      public final DyeColor colour;

      SlotIndex(DyeColor colour) {
         this.colour = colour;
      }

      public PipeBehaviourEmzuli.SlotIndex next() {
         switch (this) {
            case SQUARE:
               return CIRCLE;
            case CIRCLE:
               return TRIANGLE;
            case TRIANGLE:
               return CROSS;
            case CROSS:
               return SQUARE;
            default:
               throw new IllegalStateException("Unknown SlotIndex - " + this);
         }
      }
   }
}
