/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.builders.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import buildcraft.api.core.BCLog;
import buildcraft.api.filler.IFillerPattern;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.fabric.menu.FillerPlannerMenuKey;
import buildcraft.lib.statement.FullStatement;

import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.BCBuildersStatements;
import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.builders.filler.FillerType;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraft.core.marker.volume.VolumeBox;

@SuppressWarnings("this-escape")
public class ContainerFillerPlanner extends ContainerBC_Neptune implements IContainerFilling {
    public static final int NET_STATEMENT = 11;
    public static final int NET_INVERT = 12;

    public final AddonFillerPlanner addon;

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
                    BCBuildersStatements.PATTERN_NONE,
                    BCBuildersStatements.PATTERN_BOX,
                    BCBuildersStatements.PATTERN_CLEAR,
                    BCBuildersStatements.PATTERN_FILL
                );
            }
            @Override public buildcraft.lib.gui.ISimpleDrawable getSourceIcon() { return null; }
        },
        new buildcraft.lib.statement.StatementContext.StatementGroup<IFillerPattern>() {
            @Override
            public java.util.List<IFillerPattern> getValues() {
                return java.util.Arrays.asList(
                    BCBuildersStatements.PATTERN_FRAME,
                    BCBuildersStatements.PATTERN_PYRAMID,
                    BCBuildersStatements.PATTERN_SPHERE,
                    BCBuildersStatements.PATTERN_EIGHTH_SPHERE
                );
            }
            @Override public buildcraft.lib.gui.ISimpleDrawable getSourceIcon() { return null; }
        },
        new buildcraft.lib.statement.StatementContext.StatementGroup<IFillerPattern>() {
            @Override
            public java.util.List<IFillerPattern> getValues() {
                return java.util.Arrays.asList(
                    BCBuildersStatements.PATTERN_HEMI_SPHERE,
                    BCBuildersStatements.PATTERN_QUARTER_SPHERE,
                    BCBuildersStatements.PATTERN_STAIRS
                );
            }
            @Override public buildcraft.lib.gui.ISimpleDrawable getSourceIcon() { return null; }
        },
        new buildcraft.lib.statement.StatementContext.StatementGroup<IFillerPattern>() {
            @Override
            public java.util.List<IFillerPattern> getValues() {
                return java.util.Arrays.asList(
                    BCBuildersStatements.PATTERN_ARC,
                    BCBuildersStatements.PATTERN_CIRCLE,
                    BCBuildersStatements.PATTERN_HEXAGON,
                    BCBuildersStatements.PATTERN_OCTAGON
                );
            }
            @Override public buildcraft.lib.gui.ISimpleDrawable getSourceIcon() { return null; }
        },
        new buildcraft.lib.statement.StatementContext.StatementGroup<IFillerPattern>() {
            @Override
            public java.util.List<IFillerPattern> getValues() {
                return java.util.Arrays.asList(
                    BCBuildersStatements.PATTERN_PENTAGON,
                    BCBuildersStatements.PATTERN_SEMI_CIRCLE,
                    BCBuildersStatements.PATTERN_SQUARE,
                    BCBuildersStatements.PATTERN_TRIANGLE
                );
            }
            @Override public buildcraft.lib.gui.ISimpleDrawable getSourceIcon() { return null; }
        }
    );

    public ContainerFillerPlanner(int containerId, Inventory playerInv, AddonFillerPlanner addon) {
        super(BCBuildersMenuTypes.FILLER_PLANNER, containerId, playerInv.player);
        this.addon = addon;
    }

    public ContainerFillerPlanner(int containerId, Inventory playerInv, FillerPlannerMenuKey key) {
        super(BCBuildersMenuTypes.FILLER_PLANNER, containerId, playerInv.player);
        this.addon = resolveAddon(key);
    }

    private static AddonFillerPlanner resolveAddon(FillerPlannerMenuKey key) {
        if (key == null) {
            return null;
        }
        for (VolumeBox vb : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
            if (vb.id.equals(key.boxId())) {
                Addon a = vb.addons.get(key.slot());
                if (a instanceof AddonFillerPlanner planner) {
                    return planner;
                }
                break;
            }
        }
        return null;
    }

    public void onStatementChange() {
        if (player != null && player.level() != null && player.level().isClientSide()) {
            sendMessage(NET_STATEMENT, (buf) -> {
                buildcraft.lib.net.PacketBufferBC buffer = new buildcraft.lib.net.PacketBufferBC(buf.unwrap());
                patternStatementClient.writeToBuffer(buffer);
            });
        }
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
        return addon != null ? addon.patternStatement : patternStatementClient;
    }

    @Override
    public boolean isInverted() {
        return addon != null && addon.inverted;
    }

    @Override
    public void setInverted(boolean value) {
        if (addon != null) {
            addon.inverted = value;
        }
    }

    @Override
    public void valuesChanged() {
        if (addon != null) {
            addon.updateBuildingInfo();
            if (player != null && player.level() != null && !player.level().isClientSide()) {
                WorldSavedDataVolumeBoxes.get(player.level()).markDirtyAndBroadcast();
            }
        }
    }

    @Override
    public void readMessage(int id, buildcraft.lib.net.PacketBufferBC buffer, boolean isClient,
                            buildcraft.fabric.network.BCPayloadContext ctx) {
        if (id == NET_STATEMENT) {
            try {
                if (isClient) {
                    patternStatementClient.readFromBuffer(buffer);
                } else if (addon != null) {
                    addon.patternStatement.readFromBuffer(buffer);
                    valuesChanged();
                }
            } catch (java.io.IOException e) {
                BCLog.logger.warn("[builders.filler] Failed to read filler planner data from the network buffer", e);
            }
            return;
        }

        super.readMessage(id, buffer, isClient, ctx);
        if (isClient) return;

        if (id == NET_INVERT) {
            if (addon != null) {
                addon.inverted = !addon.inverted;
                valuesChanged();
            }
        }
    }

    private byte[] lastStatementHash = null;

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (addon != null && player != null && player.level() != null && !player.level().isClientSide()) {
            io.netty.buffer.ByteBuf temp = io.netty.buffer.Unpooled.buffer();
            buildcraft.lib.net.PacketBufferBC bcBuf = new buildcraft.lib.net.PacketBufferBC(temp);
            addon.patternStatement.writeToBuffer(bcBuf);
            byte[] current = new byte[temp.readableBytes()];
            temp.readBytes(current);
            temp.release();

            if (lastStatementHash == null || !java.util.Arrays.equals(lastStatementHash, current)) {
                lastStatementHash = current;
                sendMessage(NET_STATEMENT, (buf) -> buf.writeBytes(current));
            }
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
