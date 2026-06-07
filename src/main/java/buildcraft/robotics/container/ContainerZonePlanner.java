package buildcraft.robotics.container;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.gui.slot.SlotOutput;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.robotics.BCRoboticsMenuTypes;
import buildcraft.robotics.tile.TileZonePlanner;
import buildcraft.robotics.zone.ZonePlan;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerZonePlanner extends ContainerBCTile<TileZonePlanner> {
   public static final int NET_PAINT = 200;
   public static final int NET_REQUEST_LAYERS = 201;
   public static final int NET_LAYERS = 202;
   private static final int PLAYER_SLOTS_END = 36;
   private static final int MACHINE_SLOTS_END = 58;

   public ContainerZonePlanner(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getTile(playerInv, pos));
   }

   public ContainerZonePlanner(int containerId, Inventory playerInv, TileZonePlanner tile) {
      super(BCRoboticsMenuTypes.ZONE_PLANNER, containerId, playerInv.player, tile);
      this.addFullPlayerInventory(88, 146);

      for (int x = 0; x < 4; x++) {
         for (int y = 0; y < 4; y++) {
            this.addSlot(new SlotBase(tile.invPaintbrushes, x * 4 + y, 8 + x * 18, 146 + y * 18));
         }
      }

      this.addSlot(new SlotBase(tile.invInputPaintbrush, 0, 8, 125));
      this.addSlot(new SlotBase(tile.invInputMapLocation, 0, 26, 125));
      this.addSlot(new SlotOutput(tile.invInputResult, 0, 74, 125));
      this.addSlot(new SlotBase(tile.invOutputPaintbrush, 0, 233, 9));
      this.addSlot(new SlotBase(tile.invOutputMapLocation, 0, 233, 27));
      this.addSlot(new SlotOutput(tile.invOutputResult, 0, 233, 75));

      this.addDataSlot(new DataSlot() {
         @Override
         public int get() {
            return tile != null ? tile.getProgressInput() : 0;
         }

         @Override
         public void set(int value) {
            if (tile != null) {
               tile.setProgressInput(value);
            }
         }
      });
      this.addDataSlot(new DataSlot() {
         @Override
         public int get() {
            return tile != null ? tile.getProgressOutput() : 0;
         }

         @Override
         public void set(int value) {
            if (tile != null) {
               tile.setProgressOutput(value);
            }
         }
      });
   }

   public void sendPaint(int layer, int x, int z, boolean set) {
      this.sendMessage(200, buf -> {
         buf.writeByte(layer);
         buf.writeVarInt(x);
         buf.writeVarInt(z);
         buf.writeBoolean(set);
      });
   }

   public void requestLayers() {
      this.sendMessage(201, buf -> {});
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == 200 && !isClient) {
         int layer = buffer.readByte() & 255;
         int x = buffer.readVarInt();
         int z = buffer.readVarInt();
         boolean set = buffer.readBoolean();
         if (this.tile != null) {
            this.tile.applyPaint(layer, x, z, set);
         }
      } else if (id == 201 && !isClient) {
         if (this.tile != null) {
            this.sendMessage(202, buf -> {
               for (ZonePlan planx : this.tile.layers) {
                  (planx == null ? new ZonePlan() : planx).writeToByteBuf(buf);
               }
            });
         }
      } else if (id == 202 && isClient) {
         if (this.tile != null) {
            for (int i = 0; i < this.tile.layers.length; i++) {
               ZonePlan plan = new ZonePlan();
               plan.readFromByteBuf(buffer);
               this.tile.layers[i] = plan;
            }
         }
      } else {
         super.readMessage(id, buffer, isClient, ctx);
      }
   }

   private static TileZonePlanner getTile(Inventory playerInv, BlockPos pos) {
      return playerInv.player.level() != null && playerInv.player.level().getBlockEntity(pos) instanceof TileZonePlanner planner ? planner : null;
   }

   @Override
   public ItemStack quickMoveStack(Player player, int slotIndex) {
      ItemStack result = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(slotIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         result = stack.copy();
         if (slotIndex < 36) {
            if (!this.moveItemStackTo(stack, 36, 58, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 0, 36, true)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(player, stack);
      }

      return result;
   }
}
