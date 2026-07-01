/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import buildcraft.lib.net.BcPayloadBuffers;
import net.minecraft.network.FriendlyByteBuf;
import buildcraft.api.core.BCLog;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.tiles.IControllable;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.filler.FillerPatternStatementGroups;
import buildcraft.builders.filler.FillerType;
import buildcraft.builders.tile.TileFiller;
import buildcraft.fabric.network.BCPayloadContext;
import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.gui.slot.SlotBase;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;
import buildcraft.lib.statement.StatementContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class ContainerFiller extends ContainerBCTile<TileFiller> implements IContainerFilling {
   private static final int DATA_CAN_EXCAVATE = 0;
   private static final int DATA_INVERTED = 1;
   private static final int DATA_FINISHED = 2;
   private static final int DATA_LOCKED = 3;
   private static final int DATA_MODE = 4;
   private static final int DATA_TO_PLACE = 5;
   private static final int DATA_TO_BREAK = 6;
   private static final int DATA_COUNT = 7;
   private final ContainerData data;
   private final FullStatement<IFillerPattern> patternStatementClient = new FullStatement<>(
      FillerType.INSTANCE, 4, (statement, paramIndex) -> this.onStatementChange()
   );
   public final StatementContext<IFillerPattern> possiblePatternsContext = FillerPatternStatementGroups.CONTEXT;
   public static final int NET_EXCAVATE = 10;
   public static final int NET_STATEMENT = 11;
   public static final int NET_INVERT = 12;
   private byte[] lastStatementHash = null;

   @Override
   public void onStatementChange() {
      if (this.player != null && this.player.level() != null && this.player.level().isClientSide()) {
         this.sendMessage(11, buf -> {
            PacketBufferBC buffer = BcPayloadBuffers.ensure(buf.unwrap());
            this.patternStatementClient.writeToBuffer(buffer);
         });
      }
   }

   public ContainerFiller(int containerId, Inventory playerInv, BlockPos pos) {
      this(containerId, playerInv, getTile(playerInv, pos));
   }

   public ContainerFiller(int containerId, Inventory playerInv, final TileFiller tile) {
      super(BCBuildersMenuTypes.FILLER, containerId, playerInv.player, tile);
      if (tile != null && tile.getLevel() != null && !tile.getLevel().isClientSide()) {
         this.data = new ContainerData() {
            public int get(int index) {
               return switch (index) {
                  case 0 -> tile.getCanExcavate() ? 1 : 0;
                  case 1 -> tile.inverted ? 1 : 0;
                  case 2 -> tile.getFinished() ? 1 : 0;
                  case 3 -> tile.getLockedTicks();
                  case 4 -> tile.getModeOrdinal();
                  case 5 -> tile.getCountToPlace();
                  case 6 -> tile.getCountToBreak();
                  default -> 0;
               };
            }

            public void set(int index, int value) {
            }

            public int getCount() {
               return 7;
            }
         };
      } else {
         this.data = new SimpleContainerData(7);
      }

      this.addDataSlots(this.data);
      if (tile != null) {
         for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
               this.addSlot(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 85 + sy * 18));
            }
         }
      }

      this.addFullPlayerInventory(8, 153, playerInv);
   }

   private static TileFiller getTile(Inventory playerInv, BlockPos pos) {
      return playerInv.player.level() != null && playerInv.player.level().getBlockEntity(pos) instanceof TileFiller filler ? filler : null;
   }

   @Override
   public Player getPlayer() {
      return this.player;
   }

   @Override
   public FullStatement<IFillerPattern> getPatternStatementClient() {
      return this.patternStatementClient;
   }

   @Override
   public FullStatement<IFillerPattern> getPatternStatement() {
      return this.tile.addon != null ? this.tile.addon.patternStatement : this.tile.patternStatement;
   }

   @Override
   public boolean isInverted() {
      return this.data.get(1) != 0;
   }

   @Override
   public void setInverted(boolean value) {
      if (this.tile.addon != null) {
         this.tile.addon.inverted = value;
      } else {
         this.tile.inverted = value;
      }
   }

   @Override
   public void readMessage(int id, FriendlyByteBuf buffer, boolean isClient, BCPayloadContext ctx) {
      if (id == 11) {
         if (isClient) {
            this.patternStatementClient.readFromBuffer(buffer);
         } else if (this.tile != null) {
            if (this.tile.isLocked()) {
               this.valuesChanged();
               return;
            }

            FullStatement<IFillerPattern> stat = this.getPatternStatement();
            if (stat != null) {
               stat.readFromBuffer(buffer);
               this.tile.onStatementChange();
               this.tile.setChanged();
            }
         }
      } else {
         super.readMessage(id, buffer, isClient, ctx);
         if (!isClient) {
            if (id == 10) {
               if (this.tile != null) {
                  this.tile.setCanExcavate(!this.tile.getCanExcavate());
                  this.tile.setChanged();
                  this.valuesChanged();
               }
            } else if (id == 12 && this.tile != null) {
               if (this.tile.addon != null) {
                  this.tile.addon.inverted = !this.tile.addon.inverted;
               } else {
                  this.tile.inverted = !this.tile.inverted;
               }

               this.tile.setChanged();
               this.valuesChanged();
            }
         }
      }
   }

   @Override
   public void valuesChanged() {
      if (this.tile.addon != null) {
         this.tile.addon.updateBuildingInfo();
      }

      if (this.tile.getLevel() != null && !this.tile.getLevel().isClientSide()) {
         this.tile.onStatementChange();
      }
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      if (this.tile != null && this.tile.getLevel() != null && !this.tile.getLevel().isClientSide()) {
         FullStatement<IFillerPattern> stat = this.getPatternStatement();
         if (stat != null) {
            ByteBuf temp = Unpooled.buffer();
            PacketBufferBC bcBuf = BcPayloadBuffers.ensure(temp);
            stat.writeToBuffer(bcBuf);
            byte[] current = new byte[temp.readableBytes()];
            temp.readBytes(current);
            temp.release();
            if (this.lastStatementHash == null || !Arrays.equals(this.lastStatementHash, current)) {
               this.lastStatementHash = current;
               this.sendMessage(11, buf -> buf.writeBytes(current));
            }
         }
      }
   }

   public boolean getSyncedCanExcavate() {
      return this.data.get(0) != 0;
   }

   public boolean getSyncedFinished() {
      return this.data.get(2) != 0;
   }

   public boolean getSyncedLocked() {
      return this.data.get(3) > 0;
   }

   public IControllable.Mode getSyncedMode() {
      int ordinal = this.data.get(4);
      IControllable.Mode[] values = IControllable.Mode.values();
      return ordinal >= 0 && ordinal < values.length ? values[ordinal] : IControllable.Mode.ON;
   }

   public int getSyncedToPlace() {
      return this.data.get(5);
   }

   public int getSyncedToBreak() {
      return this.data.get(6);
   }
}
