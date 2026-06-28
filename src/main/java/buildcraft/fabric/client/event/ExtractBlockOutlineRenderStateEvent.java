package buildcraft.fabric.client.event;

import buildcraft.fabric.client.render.BlockOutlineRenderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.state.level.LevelRenderState;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class ExtractBlockOutlineRenderStateEvent {
   private static final List<Consumer<ExtractBlockOutlineRenderStateEvent>> LISTENERS = new ArrayList<>();
   private final LevelRenderer levelRenderer;
   private final ClientLevel level;
   private final BlockPos pos;
   private final BlockState state;
   private final BlockHitResult hitResult;
   private final CollisionContext collisionContext;
   private final Camera camera;
   //? if >= 1.21.10 {
   private final LevelRenderState levelRenderState;
   //?}
   private final List<BlockOutlineRenderer> customRenderers = new ArrayList<>();
   private boolean canceled;

   public static void register(Consumer<ExtractBlockOutlineRenderStateEvent> listener) {
      LISTENERS.add(listener);
   }

   public static boolean hasListeners() {
      return !LISTENERS.isEmpty();
   }

   public static void fire(ExtractBlockOutlineRenderStateEvent event) {
      for (Consumer<ExtractBlockOutlineRenderStateEvent> listener : LISTENERS) {
         listener.accept(event);
      }
   }

   public ExtractBlockOutlineRenderStateEvent(
      LevelRenderer levelRenderer,
      ClientLevel level,
      BlockPos pos,
      BlockState state,
      BlockHitResult hitResult,
      CollisionContext collisionContext,
      //? if >= 1.21.10 {
      Camera camera,
      LevelRenderState levelRenderState
      //?} else {
      /*Camera camera
      *///?}
   ) {
      this.levelRenderer = levelRenderer;
      this.level = level;
      this.pos = pos;
      this.state = state;
      this.hitResult = hitResult;
      this.collisionContext = collisionContext;
      this.camera = camera;
      //? if >= 1.21.10 {
      this.levelRenderState = levelRenderState;
      //?}
   }

   public LevelRenderer getLevelRenderer() {
      return this.levelRenderer;
   }

   public ClientLevel getLevel() {
      return this.level;
   }

   public BlockPos getBlockPos() {
      return this.pos;
   }

   public BlockState getBlockState() {
      return this.state;
   }

   public BlockHitResult getHitResult() {
      return this.hitResult;
   }

   public CollisionContext getCollisionContext() {
      return this.collisionContext;
   }

   public Camera getCamera() {
      return this.camera;
   }

   //? if >= 1.21.10 {
   public LevelRenderState getLevelRenderState() {
      return this.levelRenderState;
   }
   //?}

   public void addCustomRenderer(BlockOutlineRenderer renderer) {
      this.customRenderers.add(renderer);
   }

   public List<BlockOutlineRenderer> getCustomRenderers() {
      return Collections.unmodifiableList(this.customRenderers);
   }

   public boolean isCanceled() {
      return this.canceled;
   }

   public void setCanceled(boolean canceled) {
      this.canceled = canceled;
   }
}
