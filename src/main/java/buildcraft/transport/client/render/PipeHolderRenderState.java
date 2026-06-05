package buildcraft.transport.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

import buildcraft.transport.tile.TilePipeHolder;

public class PipeHolderRenderState extends BlockEntityRenderState {

    public TilePipeHolder pipe;
    public float partialTick;

    public List<ItemRenderEntry> itemEntries = new ArrayList<>();

    public static class ItemRenderEntry {
        public final ItemStackRenderState renderState;
        public final double posX, posY, posZ;
        public final Direction direction;
        public final DyeColor colour;
        public final int stackCount;

        public ItemRenderEntry(ItemStackRenderState renderState,
                               double posX, double posY, double posZ,
                               Direction direction, DyeColor colour, int stackCount) {
            this.renderState = renderState;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.direction = direction;
            this.colour = colour;
            this.stackCount = stackCount;
        }
    }
}
