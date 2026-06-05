/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import buildcraft.lib.gui.slot.SlotBase;

import buildcraft.api.core.BCLog;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.tiles.IControllable;

import buildcraft.lib.gui.ContainerBCTile;
import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.filler.FillerType;
import buildcraft.builders.tile.TileFiller;

@SuppressWarnings("this-escape")
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
        FillerType.INSTANCE,
        4,
        (statement, paramIndex) -> onStatementChange()
    );

    public final buildcraft.lib.statement.StatementContext<IFillerPattern> possiblePatternsContext = () -> java.util.List.of(
        new buildcraft.lib.statement.StatementContext.StatementGroup<IFillerPattern>() {
            @Override
            public java.util.List<IFillerPattern> getValues() {
                return java.util.Arrays.asList(
                    buildcraft.builders.BCBuildersStatements.PATTERN_NONE,
                    buildcraft.builders.BCBuildersStatements.PATTERN_BOX,
                    buildcraft.builders.BCBuildersStatements.PATTERN_CLEAR,
                    buildcraft.builders.BCBuildersStatements.PATTERN_FILL
                );
            }
            @Override public buildcraft.lib.gui.ISimpleDrawable getSourceIcon() { return null; }
        },
        new buildcraft.lib.statement.StatementContext.StatementGroup<IFillerPattern>() {
            @Override
            public java.util.List<IFillerPattern> getValues() {
                return java.util.Arrays.asList(
                    buildcraft.builders.BCBuildersStatements.PATTERN_FRAME,
                    buildcraft.builders.BCBuildersStatements.PATTERN_PYRAMID,
                    buildcraft.builders.BCBuildersStatements.PATTERN_SPHERE,
                    buildcraft.builders.BCBuildersStatements.PATTERN_EIGHTH_SPHERE
                );
            }
            @Override public buildcraft.lib.gui.ISimpleDrawable getSourceIcon() { return null; }
        },
        new buildcraft.lib.statement.StatementContext.StatementGroup<IFillerPattern>() {
            @Override
            public java.util.List<IFillerPattern> getValues() {
                return java.util.Arrays.asList(
                    buildcraft.builders.BCBuildersStatements.PATTERN_HEMI_SPHERE,
                    buildcraft.builders.BCBuildersStatements.PATTERN_QUARTER_SPHERE,
                    buildcraft.builders.BCBuildersStatements.PATTERN_STAIRS
                );
            }
            @Override public buildcraft.lib.gui.ISimpleDrawable getSourceIcon() { return null; }
        },
        new buildcraft.lib.statement.StatementContext.StatementGroup<IFillerPattern>() {
            @Override
            public java.util.List<IFillerPattern> getValues() {
                return java.util.Arrays.asList(
                    buildcraft.builders.BCBuildersStatements.PATTERN_ARC,
                    buildcraft.builders.BCBuildersStatements.PATTERN_CIRCLE,
                    buildcraft.builders.BCBuildersStatements.PATTERN_HEXAGON,
                    buildcraft.builders.BCBuildersStatements.PATTERN_OCTAGON
                );
            }
            @Override public buildcraft.lib.gui.ISimpleDrawable getSourceIcon() { return null; }
        },
        new buildcraft.lib.statement.StatementContext.StatementGroup<IFillerPattern>() {
            @Override
            public java.util.List<IFillerPattern> getValues() {
                return java.util.Arrays.asList(
                    buildcraft.builders.BCBuildersStatements.PATTERN_PENTAGON,
                    buildcraft.builders.BCBuildersStatements.PATTERN_SEMI_CIRCLE,
                    buildcraft.builders.BCBuildersStatements.PATTERN_SQUARE,
                    buildcraft.builders.BCBuildersStatements.PATTERN_TRIANGLE
                );
            }
            @Override public buildcraft.lib.gui.ISimpleDrawable getSourceIcon() { return null; }
        }
    );

    public void onStatementChange() {
        if (player != null && player.level() != null && player.level().isClientSide()) {
            sendMessage(NET_STATEMENT, (buf) -> {
                buildcraft.lib.net.PacketBufferBC buffer = new buildcraft.lib.net.PacketBufferBC(buf.unwrap());
                patternStatementClient.writeToBuffer(buffer);
            });
        }
    }

    public ContainerFiller(int containerId, Inventory playerInv, BlockPos pos) {
        this(containerId, playerInv, getTile(playerInv, pos));
    }

    public ContainerFiller(int containerId, Inventory playerInv, TileFiller tile) {
        super(BCBuildersMenuTypes.FILLER, containerId, playerInv.player, tile);

        if (tile != null && tile.getLevel() != null && !tile.getLevel().isClientSide()) {
            this.data = new ContainerData() {
                @Override
                public int get(int index) {
                    return switch (index) {
                        case DATA_CAN_EXCAVATE -> tile.getCanExcavate() ? 1 : 0;
                        case DATA_INVERTED -> tile.inverted ? 1 : 0;
                        case DATA_FINISHED -> tile.getFinished() ? 1 : 0;
                        case DATA_LOCKED -> tile.getLockedTicks();
                        case DATA_MODE -> tile.getModeOrdinal();
                        case DATA_TO_PLACE -> tile.getCountToPlace();
                        case DATA_TO_BREAK -> tile.getCountToBreak();
                        default -> 0;
                    };
                }

                @Override
                public void set(int index, int value) {

                }

                @Override
                public int getCount() {
                    return DATA_COUNT;
                }
            };
        } else {
            this.data = new SimpleContainerData(DATA_COUNT);
        }

        addDataSlots(this.data);

        if (tile != null) {
            for (int sy = 0; sy < 3; sy++) {
                for (int sx = 0; sx < 9; sx++) {
                    addSlot(new SlotBase(tile.invResources, sx + sy * 9, 8 + sx * 18, 85 + sy * 18));
                }
            }
        }

        addFullPlayerInventory(8, 153, playerInv);
    }

    private static TileFiller getTile(Inventory playerInv, BlockPos pos) {
        if (playerInv.player.level() != null) {
            var be = playerInv.player.level().getBlockEntity(pos);
            if (be instanceof TileFiller filler) {
                return filler;
            }
        }
        return null;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public FullStatement<IFillerPattern> getPatternStatementClient() {
        return patternStatementClient;
    }

    @Override
    public FullStatement<IFillerPattern> getPatternStatement() {
        return tile.addon != null ? tile.addon.patternStatement : tile.patternStatement;
    }

    @Override
    public boolean isInverted() {
        return data.get(DATA_INVERTED) != 0;
    }

    public static final int NET_EXCAVATE = 10;
    public static final int NET_STATEMENT = 11;
    public static final int NET_INVERT = 12;

    @Override
    public void setInverted(boolean value) {
        if (tile.addon != null) {
            tile.addon.inverted = value;
        } else {
            tile.inverted = value;
        }
    }

    @Override
    public void readMessage(int id, buildcraft.lib.net.PacketBufferBC buffer, boolean isClient, buildcraft.fabric.network.BCPayloadContext ctx) {
        if (id == NET_STATEMENT) {
            try {
                if (isClient) {
                    patternStatementClient.readFromBuffer(buffer);
                } else if (tile != null) {
                    if (tile.isLocked()) {
                        valuesChanged();
                        return;
                    }
                    buildcraft.lib.statement.FullStatement<IFillerPattern> stat = getPatternStatement();
                    if (stat != null) {
                        stat.readFromBuffer(buffer);
                        tile.onStatementChange();
                        tile.setChanged();
                    }
                }
            } catch (java.io.IOException e) {
                BCLog.logger.warn("[builders.filler] Failed to read filler data from the network buffer", e);
            }
            return;
        }

        super.readMessage(id, buffer, isClient, ctx);
        if (isClient) return;

        if (id == NET_EXCAVATE) {
            if (tile != null) {
                tile.setCanExcavate(!tile.getCanExcavate());
                tile.setChanged();
                valuesChanged();
            }
        } else if (id == NET_INVERT) {
            if (tile != null) {
                if (tile.addon != null) {
                    tile.addon.inverted = !tile.addon.inverted;
                } else {
                    tile.inverted = !tile.inverted;
                }
                tile.setChanged();
                valuesChanged();
            }
        }
    }

    @Override
    public void valuesChanged() {
        if (tile.addon != null) {
            tile.addon.updateBuildingInfo();
        }
        if (tile.getLevel() != null && !tile.getLevel().isClientSide()) {
            tile.onStatementChange();
        }
    }

    private byte[] lastStatementHash = null;

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (tile != null && tile.getLevel() != null && !tile.getLevel().isClientSide()) {
            buildcraft.lib.statement.FullStatement<IFillerPattern> stat = getPatternStatement();
            if (stat != null) {
                io.netty.buffer.ByteBuf temp = io.netty.buffer.Unpooled.buffer();
                buildcraft.lib.net.PacketBufferBC bcBuf = new buildcraft.lib.net.PacketBufferBC(temp);
                stat.writeToBuffer(bcBuf);
                byte[] current = new byte[temp.readableBytes()];
                temp.readBytes(current);
                temp.release();

                if (lastStatementHash == null || !java.util.Arrays.equals(lastStatementHash, current)) {
                    lastStatementHash = current;
                    sendMessage(NET_STATEMENT, (buf) -> {
                        buf.writeBytes(current);
                    });
                }
            }
        }
    }

    public boolean getSyncedCanExcavate() {
        return data.get(DATA_CAN_EXCAVATE) != 0;
    }

    public boolean getSyncedFinished() {
        return data.get(DATA_FINISHED) != 0;
    }

    public boolean getSyncedLocked() {
        return data.get(DATA_LOCKED) > 0;
    }

    public IControllable.Mode getSyncedMode() {
        int ordinal = data.get(DATA_MODE);
        IControllable.Mode[] values = IControllable.Mode.values();
        if (ordinal >= 0 && ordinal < values.length) return values[ordinal];
        return IControllable.Mode.ON;
    }

    public int getSyncedToPlace() {
        return data.get(DATA_TO_PLACE);
    }

    public int getSyncedToBreak() {
        return data.get(DATA_TO_BREAK);
    }
}
