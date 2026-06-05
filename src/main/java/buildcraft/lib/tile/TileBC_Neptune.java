/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import buildcraft.lib.tile.item.ItemHandlerManager;

public abstract class TileBC_Neptune extends BlockEntity {

    protected final ItemHandlerManager itemManager = new ItemHandlerManager(
        (handler, slot, before, after) -> this.setChanged()
    );

    private final Set<Player> usingPlayers = new HashSet<>();
    @Nullable
    private GameProfile owner;

    @SuppressWarnings("this-escape")
    public TileBC_Neptune(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void onPlayerOpen(Player player) {
        usingPlayers.add(player);
    }

    public void onPlayerClose(Player player) {
        usingPlayers.remove(player);
    }

    public boolean canInteractWith(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Nullable
    public GameProfile getOwner() {
        return owner;
    }

    public void setOwner(@Nullable GameProfile owner) {
        this.owner = owner;
    }

    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof Player player) {
            setOwner(player.getGameProfile());

            setChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (owner != null && owner.id() != null) {
            output.putString("ownerUUID", owner.id().toString());
            if (owner.name() != null) {
                output.putString("ownerName", owner.name());
            }
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String uuidStr = input.getStringOr("ownerUUID", "");
        if (!uuidStr.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                String name = input.getStringOr("ownerName", "Unknown");
                owner = new GameProfile(uuid, name);
            } catch (IllegalArgumentException e) {
                owner = null;
            }
        }
    }

    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        itemManager.addDrops(toDrop);
    }

    @Nullable
    public buildcraft.lib.transfer.ResourceHandler<buildcraft.lib.transfer.item.ItemResource> getItemHandler(net.minecraft.core.Direction facing) {
        return itemManager.getItemHandler(facing);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
