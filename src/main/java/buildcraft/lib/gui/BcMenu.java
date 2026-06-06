package buildcraft.lib.gui;

import buildcraft.api.core.BCLog;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.net.BcEnvelopeCodec;
import buildcraft.lib.net.BcPacketDistributor;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.MessageContainerPayload;
import buildcraft.lib.net.PacketBufferBC;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public abstract class BcMenu extends RecipeBookMenu {
   public static final int NET_WIDGET = 0;
   public static final int NET_JEI_RECIPE_TRANSFER = 100;
   public static final int NET_GHOST_SLOT_SET = 101;
   public static final int NET_JEI_TRANSFER_ITEMS = 102;
   public static final int NET_JEI_TRANSFER_BUCKETS = 103;
   public final Player player;
   private final List<Widget_Neptune<?>> widgets = new ArrayList<>();

   protected BcMenu(MenuType<?> menuType, int containerId, Player player) {
      super(menuType, containerId);
      this.player = player;
   }

   protected void addFullPlayerInventory(int startX, int startY) {
      this.addFullPlayerInventory(startX, startY, this.player.getInventory());
   }

   protected void addFullPlayerInventory(int startX, int startY, Inventory inv) {
      for (int sy = 0; sy < 3; sy++) {
         for (int sx = 0; sx < 9; sx++) {
            this.addSlot(new Slot(inv, sx + sy * 9 + 9, startX + sx * 18, startY + sy * 18));
         }
      }

      for (int sx = 0; sx < 9; sx++) {
         this.addSlot(new Slot(inv, sx, startX + sx * 18, startY + 58));
      }
   }

   public <W extends Widget_Neptune<?>> W addWidget(W widget) {
      if (widget == null) {
         throw new NullPointerException("widget");
      }

      this.widgets.add(widget);
      return widget;
   }

   public ImmutableList<Widget_Neptune<?>> getWidgets() {
      return ImmutableList.copyOf(this.widgets);
   }

   public final void sendMessage(int id, IPayloadWriter writer) {
      byte[] bytes = BcEnvelopeCodec.encode(writer);
      if (bytes == null) {
         BCLog.logger.warn("[lib.container] Container message {} exceeds payload limit", id);
      } else {
         MessageContainerPayload payload = new MessageContainerPayload(this.containerId, id, bytes);
         if (this.player.level().isClientSide()) {
            BcPacketDistributor.sendToServer(payload);
         } else if (this.player instanceof ServerPlayer serverPlayer) {
            BcPacketDistributor.sendToPlayer(serverPlayer, payload);
         }
      }
   }

   void sendWidgetData(Widget_Neptune<?> widget, IPayloadWriter writer) {
      int widgetId = this.widgets.indexOf(widget);
      if (widgetId == -1) {
         BCLog.logger.warn("[lib.container] sendWidgetData: widget not found! (" + (widget == null ? "null" : widget.getClass()) + ") in " + this.getClass());
      } else {
         this.sendMessage(0, buf -> {
            buf.writeShort(widgetId);
            writer.write(buf);
         });
      }
   }

   public void readMessage(int id, PacketBufferBC buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == 0) {
         int widgetId = buffer.readUnsignedShort();
         if (widgetId < 0 || widgetId >= this.widgets.size()) {
            BCLog.logger.warn("[lib.container] Received invalid widget ID " + widgetId + " (have " + this.widgets.size() + " widgets)");
            return;
         }

         Widget_Neptune<?> widget = this.widgets.get(widgetId);

         try {
            if (isClient) {
               widget.handleWidgetDataClient(ctx, buffer);
            } else {
               widget.handleWidgetDataServer(ctx, buffer);
            }
         } catch (Exception e) {
            BCLog.logger.warn("[lib.container] Error handling widget data for widget " + widgetId, e);
         }
      } else if (id == 100 && !isClient) {
         Identifier recipeId = Identifier.tryParse(buffer.readUtf());
         if (recipeId == null) {
            return;
         }

         if (this.player.level() instanceof ServerLevel serverLevel) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, recipeId);
            Optional<RecipeHolder<CraftingRecipe>> holder = serverLevel.recipeAccess()
               .byKey(key)
               .filter(r -> r.value() instanceof CraftingRecipe)
               .map(r -> (RecipeHolder<CraftingRecipe>)r);
            holder.ifPresent(recipe -> this.handlePlacement(false, this.player.isCreative(), (RecipeHolder<?>)recipe, serverLevel, this.player.getInventory()));
         }
      } else if (id == 101 && !isClient) {
         int slotIdx = buffer.readUnsignedShort();
         String itemId = buffer.readUtf();
         if (slotIdx >= 0 && slotIdx < this.slots.size() && this.slots.get(slotIdx) instanceof SlotPhantom phantom) {
            Identifier itemIdentifier = Identifier.tryParse(itemId);
            if (itemIdentifier == null) {
               return;
            }

            BuiltInRegistries.ITEM.get(itemIdentifier).ifPresent(itemRef -> {
               ItemStack stack = new ItemStack((ItemLike)itemRef.value(), 1);
               phantom.set(stack);
            });
         }
      }
   }

   public void clicked(int slotId, int dragType, ContainerInput containerInput, Player player) {
      if ((slotId < 0 ? null : (Slot)this.slots.get(slotId)) instanceof SlotPhantom phantom) {
         ItemStack held = this.getCarried();
         if (held.isEmpty()) {
            phantom.set(ItemStack.EMPTY);
         } else {
            ItemStack copy = held.copy();
            copy.setCount(1);
            phantom.set(copy);
         }
      } else {
         super.clicked(slotId, dragType, containerInput, player);
      }
   }

   public ItemStack quickMoveStack(Player playerIn, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack slotStack = slot.getItem();
         itemstack = slotStack.copy();
         int playerInvSize = 36;
         int containerSlots = this.slots.size() - playerInvSize;
         if (index < containerSlots) {
            if (!this.moveItemStackTo(slotStack, containerSlots, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackToValid(slotStack, 0, containerSlots)) {
            return ItemStack.EMPTY;
         }

         if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         return itemstack;
      } else {
         return itemstack;
      }
   }

   private boolean moveItemStackToValid(ItemStack stack, int startIndex, int endIndex) {
      boolean moved = false;

      for (int i = startIndex; i < endIndex && !stack.isEmpty(); i++) {
         Slot targetSlot = (Slot)this.slots.get(i);
         if (targetSlot.mayPlace(stack)) {
            ItemStack existing = targetSlot.getItem();
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(stack, existing)) {
               int maxSize = Math.min(targetSlot.getMaxStackSize(stack), stack.getMaxStackSize());
               int space = maxSize - existing.getCount();
               if (space > 0) {
                  int transfer = Math.min(space, stack.getCount());
                  existing.grow(transfer);
                  stack.shrink(transfer);
                  targetSlot.set(existing);
                  moved = true;
               }
            }
         }
      }

      for (int i = startIndex; i < endIndex && !stack.isEmpty(); i++) {
         Slot targetSlot = (Slot)this.slots.get(i);
         if (targetSlot.mayPlace(stack) && targetSlot.getItem().isEmpty()) {
            int maxSize = Math.min(targetSlot.getMaxStackSize(stack), stack.getMaxStackSize());
            int transfer = Math.min(maxSize, stack.getCount());
            targetSlot.set(stack.split(transfer));
            moved = true;
         }
      }

      return moved;
   }

   public boolean stillValid(Player player) {
      return true;
   }

   public PostPlaceAction handlePlacement(boolean useMaxItems, boolean isCreative, RecipeHolder<?> recipe, ServerLevel level, Inventory playerInv) {
      return PostPlaceAction.NOTHING;
   }

   public void fillCraftSlotsStackedContents(StackedItemContents contents) {
   }

   public RecipeBookType getRecipeBookType() {
      return RecipeBookType.CRAFTING;
   }
}
