package buildcraft.api.blocks;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public enum CustomPaintHelper {
   INSTANCE;

   private static final boolean DEBUG = BCDebugging.shouldDebugLog("api.painting");
   private final Map<Block, List<ICustomPaintHandler>> handlers = Maps.newIdentityHashMap();
   private final List<ICustomPaintHandler> allHandlers = Lists.newArrayList();

   public void registerHandlerForAll(ICustomPaintHandler handler) {
      if (DEBUG) {
         BCLog.logger.info("[api.painting] Adding a paint handler for ALL blocks (" + handler.getClass() + ")");
      }

      this.allHandlers.add(handler);
   }

   public void registerHandlerForAll(Class<? extends Block> blockClass, ICustomPaintHandler handler) {
      for (Block block : new Block[0]) {
         Class<? extends Block> foundClass = (Class<? extends Block>)block.getClass();
         if (blockClass.isAssignableFrom(foundClass)) {
            if (DEBUG) {
               BCLog.logger.info("[api.painting] Found an assignable block " + block.toString() + " (" + foundClass + ") for " + blockClass);
            }

            this.registerHandlerInternal(block, handler);
         }
      }
   }

   public void registerHandler(Block block, ICustomPaintHandler handler) {
      if (this.registerHandlerInternal(block, handler)) {
         if (DEBUG) {
            BCLog.logger.info("[api.painting] Setting a paint handler for block " + block.toString() + "(" + handler.getClass() + ")");
         }
      } else if (DEBUG) {
         BCLog.logger.info("[api.painting] Adding another paint handler for block " + block.toString() + "(" + handler.getClass() + ")");
      }
   }

   private boolean registerHandlerInternal(Block block, ICustomPaintHandler handler) {
      if (!this.handlers.containsKey(block)) {
         List<ICustomPaintHandler> forBlock = Lists.newArrayList();
         forBlock.add(handler);
         this.handlers.put(block, forBlock);
         return true;
      } else {
         this.handlers.get(block).add(handler);
         return false;
      }
   }

   public InteractionResult attemptPaintBlock(Level world, BlockPos pos, BlockState state, Vec3 hitPos, @Nullable Direction hitSide, @Nullable DyeColor paint) {
      Block block = state.getBlock();
      if (block instanceof ICustomPaintHandler) {
         return ((ICustomPaintHandler)block).attemptPaint(world, pos, state, hitPos, hitSide, paint);
      }

      List<ICustomPaintHandler> custom = this.handlers.get(block);
      if (custom != null && !custom.isEmpty()) {
         for (ICustomPaintHandler handler : custom) {
            InteractionResult result = handler.attemptPaint(world, pos, state, hitPos, hitSide, paint);
            if (result != InteractionResult.PASS) {
               return result;
            }
         }

         return this.defaultAttemptPaint(world, pos, state, hitPos, hitSide, paint);
      } else {
         return this.defaultAttemptPaint(world, pos, state, hitPos, hitSide, paint);
      }
   }

   private InteractionResult defaultAttemptPaint(Level world, BlockPos pos, BlockState state, Vec3 hitPos, Direction hitSide, @Nullable DyeColor paint) {
      for (ICustomPaintHandler handler : this.allHandlers) {
         InteractionResult result = handler.attemptPaint(world, pos, state, hitPos, hitSide, paint);
         if (result != InteractionResult.PASS) {
            return result;
         }
      }

      return paint == null ? InteractionResult.FAIL : InteractionResult.FAIL;
   }
}
