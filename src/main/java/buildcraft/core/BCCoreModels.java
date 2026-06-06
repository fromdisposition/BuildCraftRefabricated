package buildcraft.core;

import buildcraft.api.enums.EnumPowerStage;
import buildcraft.lib.client.model.ModelHolderVariable;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.EngineModelCache;
import buildcraft.lib.engine.TileEngineBase_BC8;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.ExpressionCompat;
import net.minecraft.core.Direction;

public class BCCoreModels {
   private static final NodeVariableDouble ENGINE_PROGRESS;
   private static final NodeVariableObject<EnumPowerStage> ENGINE_STAGE;
   private static final NodeVariableObject<Direction> ENGINE_FACING;
   private static final EngineModelCache ENGINE_WOOD;
   private static final EngineModelCache ENGINE_CREATIVE;

   public static MutableQuad[] getWoodEngineQuads(TileEngineBase_BC8 tile, float partialTicks) {
      return ENGINE_WOOD.getQuads(tile, partialTicks);
   }

   public static MutableQuad[] getCreativeEngineQuads(TileEngineBase_BC8 tile, float partialTicks) {
      return ENGINE_CREATIVE.getQuads(tile, partialTicks);
   }

   static {
      FunctionContext fnCtx = new FunctionContext(ExpressionCompat.ENUM_POWER_STAGE, DefaultContexts.createWithAll());
      ENGINE_PROGRESS = fnCtx.putVariableDouble("progress");
      ENGINE_STAGE = fnCtx.putVariableObject("stage", EnumPowerStage.class);
      ENGINE_FACING = fnCtx.putVariableObject("direction", Direction.class);
      ENGINE_WOOD = new EngineModelCache(
         new ModelHolderVariable("buildcraftcore:models/compat/engine_wood.json", fnCtx), ENGINE_PROGRESS, ENGINE_STAGE, ENGINE_FACING
      );
      ENGINE_CREATIVE = new EngineModelCache(
         new ModelHolderVariable("buildcraftcore:models/compat/engine_creative.json", fnCtx), ENGINE_PROGRESS, ENGINE_STAGE, ENGINE_FACING
      );
   }
}
