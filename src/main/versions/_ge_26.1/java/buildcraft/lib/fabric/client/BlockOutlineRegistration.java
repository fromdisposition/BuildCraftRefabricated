package buildcraft.lib.fabric.client;

import buildcraft.fabric.client.event.ExtractBlockOutlineRenderStateEvent;
import buildcraft.fabric.client.render.BlockOutlineRenderer;
import buildcraft.fabric.client.render.BlockOutlineRenderStore;
import java.util.Collections;
import java.util.List;
//? if >= 26.2 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
//?}
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Wires BC's custom block outlines (pipe plug previews) into Fabric's level-render events: collecting the
 * renderers during extraction, then dispatching them when vanilla is about to draw the outline.
 *
 * <p>Both halves used to be hand-written mixins on LevelRenderer/LevelExtractor. Neither is needed: Fabric injects
 * at exactly the same places (its own LevelExtractorMixin / LevelRendererMixin) and its contexts carry everything
 * those mixins dug out via {@code @Shadow}.
 *
 * <p>On the dispatch half specifically: vanilla returns early from {@code renderBlockOutline} unless
 * {@code outline.isTranslucent() == translucentPass}, and Fabric injects AFTER that check (at the
 * {@code CameraRenderState.pos} read), so {@code BEFORE_BLOCK_OUTLINE} only ever fires in the pass the outline
 * belongs to. The pass flag BC's renderers used to take is therefore implied -- its only use was skipping the
 * other pass. 26.2 dropped the flag from vanilla altogether (submit-based outline).
 *
 * <p>Installed from {@link buildcraft.fabric.BuildCraftFabricClient}; nodes below 26.1 shadow this class with a
 * no-op (versions/_lt_26.1).
 */
public final class BlockOutlineRegistration {
   private BlockOutlineRegistration() {
   }

   public static void install() {
      // 26.2 moved the extraction event to LevelExtractionEvents, leaving a deprecated alias on LevelRenderEvents;
      // 26.1 only has the LevelRenderEvents one. Both hand the listener the same (context, hitResult) pair.
      //? if >= 26.2 {
      LevelExtractionEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register(BlockOutlineRegistration::afterExtraction);
      //?} else {
      /*LevelRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register(BlockOutlineRegistration::afterExtraction);
      *///?}
      LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(BlockOutlineRegistration::beforeBlockOutline);
   }

   /** Lets BC attach custom renderers to the outline being extracted, or drop the vanilla outline entirely. */
   private static void afterExtraction(LevelExtractionContext context, HitResult hitResult) {
      LevelRenderState levelState = context.levelState();
      BlockOutlineRenderState outline = levelState.blockOutlineRenderState;
      if (outline == null || !ExtractBlockOutlineRenderStateEvent.hasListeners()) {
         return;
      }

      ClientLevel level = context.level();
      if (level == null || !(hitResult instanceof BlockHitResult blockHit)) {
         return;
      }

      BlockPos pos = blockHit.getBlockPos();
      BlockState state = level.getBlockState(pos);
      if (state.isAir()) {
         return;
      }

      Camera camera = context.camera();
      var cameraEntity = camera.entity() != null ? camera.entity() : Minecraft.getInstance().player;
      ExtractBlockOutlineRenderStateEvent event = new ExtractBlockOutlineRenderStateEvent(
         context.levelRenderer(), level, pos, state, blockHit, CollisionContext.of(cameraEntity), camera, levelState
      );
      ExtractBlockOutlineRenderStateEvent.fire(event);
      if (event.isCanceled()) {
         levelState.blockOutlineRenderState = null;
         BlockOutlineRenderStore.CUSTOM_OUTLINES.remove(outline);
      } else {
         BlockOutlineRenderStore.CUSTOM_OUTLINES.put(
            outline, event.getCustomRenderers().isEmpty() ? Collections.emptyList() : event.getCustomRenderers()
         );
      }
   }

   /** @return false to suppress the vanilla outline -- what a renderer claiming the draw asks for. */
   private static boolean beforeBlockOutline(LevelRenderContext context, BlockOutlineRenderState outline) {
      List<BlockOutlineRenderer> custom = BlockOutlineRenderStore.CUSTOM_OUTLINES.getOrDefault(outline, Collections.emptyList());
      if (custom.isEmpty()) {
         return true;
      }

      boolean claimed = false;
      for (BlockOutlineRenderer renderer : custom) {
         //? if >= 26.2 {
         claimed |= renderer.render(outline, context.submitNodeCollector(), context.poseStack(), context.levelState());
         //?} else {
         /*claimed |= renderer.render(outline, context.bufferSource(), context.poseStack(), context.levelState());
         *///?}
      }

      return !claimed;
   }
}
