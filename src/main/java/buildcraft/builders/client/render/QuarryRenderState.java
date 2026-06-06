package buildcraft.builders.client.render;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.client.render.tile.BcBerState;
import net.minecraft.core.Direction;

public class QuarryRenderState extends BcBerState<TileQuarry> {
   public Direction rear;
   public int greenColour;
   public int redColour;
}
