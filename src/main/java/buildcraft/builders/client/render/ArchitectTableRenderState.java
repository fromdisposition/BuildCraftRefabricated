package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.lib.client.render.tile.BcBerState;
import net.minecraft.core.Direction;

public class ArchitectTableRenderState extends BcBerState<TileArchitectTable> {
   public Direction facing;
   public Direction skipFace;
   public int greenColour;
   public int redColour;
}
