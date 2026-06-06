package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileMiningWell;
import buildcraft.lib.client.render.tile.BcBerState;
import net.minecraft.core.Direction;

public class MiningWellRenderState extends BcBerState<TileMiningWell> {
   public Direction facing;
   public int powerColour;
   public int statusColour;
}
