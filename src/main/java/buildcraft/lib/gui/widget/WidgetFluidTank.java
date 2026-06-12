/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.widget;

import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.ISolidCoolant;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.fabric.transfer.FluidStorageOps;
import buildcraft.lib.fabric.transfer.FluidStorageSnapshot;
import buildcraft.lib.fabric.transfer.ItemFluidLookup;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.gui.BcMenu;
import buildcraft.lib.gui.Widget_Neptune;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.net.PacketBufferBC;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class WidgetFluidTank extends Widget_Neptune<BcMenu> {
   private static final byte NET_CLICK = 0;
   private @Nullable Storage<FluidVariant> tank;

   public WidgetFluidTank(BcMenu container, @Nullable Storage<FluidVariant> tank) {
      super(container);
      this.tank = tank;
   }

   public void setTank(@Nullable Storage<FluidVariant> tank) {
      this.tank = tank;
   }

   public @Nullable Storage<FluidVariant> getTankStorage() {
      return this.tank;
   }

   private boolean canAccessTank() {
      return this.tank != null;
   }

   @Override
   public void handleWidgetDataServer(BCPayloadContext ctx, FriendlyByteBuf buffer) {
      byte id = buffer.readByte();
      if (id == 0) {
         this.onGuiClicked();
      }
   }

   public void sendClick() {
      this.sendWidgetData(buf -> buf.writeByte(0));
   }

   private boolean drainTankIntoInventoryBucket(Player player) {
      if (!this.canAccessTank()) {
         return false;
      }

      FluidStorageSnapshot snapshot = FluidStorageSnapshot.of(this.tank);
      if (snapshot.isEmpty()) {
         return false;
      }

      Inventory inv = player.getInventory();
      int size = inv.getContainerSize();

      for (int i = 0; i < size; i++) {
         ItemStack invStack = inv.getItem(i);
         if (!invStack.isEmpty()) {
            ContainerItemContext slotContext = ContainerItemContext.ofPlayerSlot(player, PlayerInventoryStorage.of(player).getSlot(i));
            Storage<FluidVariant> slotStorage = ItemFluidLookup.storage(invStack, slotContext);
            if (slotStorage != null && FluidStorageSnapshot.of(slotStorage).isEmpty() && FluidStorageOps.move(this.tank, slotStorage, Long.MAX_VALUE) > 0L) {
               return true;
            }
         }
      }

      return false;
   }

   private void onGuiClicked() {
      if (this.canAccessTank()) {
         Player player = this.container.player;
         ItemStack held = player.containerMenu.getCarried();
         if (!held.isEmpty()) {
            this.transferStackToTank(player);
            if (player instanceof ServerPlayer sp) {
               sp.containerMenu.broadcastChanges();
            }
         }
      }
   }

   private void transferStackToTank(Player player) {
      if (!player.level().isClientSide()) {
         if (this.canAccessTank()) {
            ItemStack carried = player.containerMenu.getCarried();
            boolean isCreative = player.getAbilities().instabuild;
            if (isCreative) {
               ItemStack bucketCopy = carried.copy();
               ContainerItemContext copyContext = ContainerItemContext.withConstant(bucketCopy);
               Storage<FluidVariant> bucketStorage = ItemFluidLookup.storage(bucketCopy, copyContext);
               if (bucketStorage != null) {
                  FluidStorageSnapshot bucketSnapshot = FluidStorageSnapshot.of(bucketStorage);
                  if (!bucketSnapshot.isEmpty()) {
                     Transaction tx = Transaction.openOuter();

                     try {
                        int filled = FluidStorageOps.insertFluidMb(this.tank, bucketSnapshot.fluid(), bucketSnapshot.amountMb(), tx);
                        if (filled > 0) {
                           tx.commit();
                        }
                     } catch (Throwable var18) {
                        if (tx != null) {
                           try {
                              tx.close();
                           } catch (Throwable var17) {
                              var18.addSuppressed(var17);
                           }
                        }

                        throw var18;
                     }

                     if (tx != null) {
                        tx.close();
                     }

                     return;
                  }

                  FluidStorageSnapshot snapshot = FluidStorageSnapshot.of(this.tank);
                  if (!snapshot.isEmpty()) {
                     int toDrain = Math.min(1000, snapshot.amountMb());
                     Transaction tx = Transaction.openOuter();

                     try {
                        int drained = FluidStorageOps.extractFluidMb(this.tank, snapshot.fluid(), toDrain, tx);
                        if (drained > 0) {
                           tx.commit();
                        }
                     } catch (Throwable var19) {
                        if (tx != null) {
                           try {
                              tx.close();
                           } catch (Throwable var16) {
                              var19.addSuppressed(var16);
                           }
                        }

                        throw var19;
                     }

                     if (tx != null) {
                        tx.close();
                     }
                  }

                  return;
               }
            } else {
               ItemStack original = carried.copy();
               ContainerItemContext cursorContext = ContainerItemContext.ofPlayerCursor(player, player.containerMenu);
               Storage<FluidVariant> handStorage = ItemFluidLookup.storage(original, cursorContext);
               if (handStorage != null) {
                  if (FluidStorageOps.move(handStorage, this.tank, Long.MAX_VALUE) > 0L) {
                     return;
                  }

                  if (FluidStorageOps.move(this.tank, handStorage, Long.MAX_VALUE) > 0L) {
                     return;
                  }

                  if (this.drainTankIntoInventoryBucket(player)) {
                     return;
                  }
               }
            }

            if (BuildcraftFuelRegistry.coolant != null) {
               ItemStack stack = player.containerMenu.getCarried();
               ItemStack singleCopyCoolant = stack.copyWithCount(1);
               ISolidCoolant solidCoolant = BuildcraftFuelRegistry.coolant.getSolidCoolant(singleCopyCoolant);
               if (solidCoolant != null) {
                  FluidStack fluidCoolant = solidCoolant.getFluidFromSolidCoolant(singleCopyCoolant);
                  if (fluidCoolant != null && !fluidCoolant.isEmpty()) {
                     Transaction tx = Transaction.openOuter();

                     try {
                        int filled = FluidStorageOps.insertFluidMb(this.tank, fluidCoolant, fluidCoolant.getAmount(), tx);
                        if (filled == fluidCoolant.getAmount()) {
                           tx.commit();
                           AdvancementUtil.unlockAdvancement(player, Identifier.parse("buildcraftenergy:ice_cool"));
                           if (!isCreative) {
                              stack.shrink(1);
                           }
                        }
                     } catch (Throwable var20) {
                        if (tx != null) {
                           try {
                              tx.close();
                           } catch (Throwable var15) {
                              var20.addSuppressed(var15);
                           }
                        }

                        throw var20;
                     }

                     if (tx != null) {
                        tx.close();
                     }
                  }
               }
            }
         }
      }
   }
}
