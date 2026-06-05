package buildcraft.lib.cache;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;

public final class TileCacheRet {

    @Nullable
    public final BlockEntity tile;

    public TileCacheRet(BlockEntity tile) {
        this.tile = tile;
    }
}
