/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render.pip;

//? if >= 1.21.10 {

import buildcraft.lib.client.render.BCLibRenderTypes;
import buildcraft.lib.client.render.GeometrySink;
import buildcraft.robotics.zone.ZonePlannerChunkKeys;
import buildcraft.robotics.zone.ZonePlannerMapColours;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
//? if >= 26.2 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
*///?}
//? if >= 26.1 {
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
//?} else {
/*import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
*///?}
import org.joml.Matrix4f;

public abstract class ZoneMapPipRendererBase<S extends PictureInPictureRenderState> extends PictureInPictureRenderer<S> {
   //? if >= 26.1 {
   private ProjectionMatrixBuffer perspBuffer;
   private ProjectionMatrixBuffer orthoRestoreBuffer;
   private final Projection orthoRestore = new Projection();
   //?} else {
   /*private PerspectiveProjectionMatrixBuffer perspBuffer;
   *///?}

   //? if >= 26.2 {
   protected ZoneMapPipRendererBase() {
      super();
   }
   //?} else {
   /*protected ZoneMapPipRendererBase(BufferSource bufferSource) {
      super(bufferSource);
   }
   *///?}

   protected abstract ZoneMapPipRenderState camera(S state);

   protected abstract void emit(S state, PoseStack poseStack, GeometrySink sink);

   protected void beforeRender(S state) {
   }

   //? if >= 26.2 {
   @Override
   protected void renderToTexture(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
      this.beforeRender(state);

      if (this.perspBuffer == null) {
         this.perspBuffer = new ProjectionMatrixBuffer("PIP zone map persp");
      }

      RenderSystem.setProjectionMatrix(this.perspBuffer.getBuffer(this.camera(state).projMatrix()), ProjectionType.PERSPECTIVE);
      Pose pose = poseStack.last();
      pose.pose().set(this.camera(state).viewMatrix());

      this.emit(state, poseStack, GeometrySink.of((SubmitNodeStorage) submitNodeCollector));

      //? if < 26.3-pre-1 {
      /*Minecraft.getInstance().gameRenderer.featureRenderDispatcher().renderAllFeatures((SubmitNodeStorage) submitNodeCollector);
      *///?}
   }
   //?} else if >= 26.1 {
   /*@Override
   protected void renderToTexture(S state, PoseStack poseStack) {
      this.beforeRender(state);
      Minecraft mc = Minecraft.getInstance();
      int guiScale = mc.getWindow().getGuiScale();
      int width = (state.x1() - state.x0()) * guiScale;
      int height = (state.y1() - state.y0()) * guiScale;

      if (this.perspBuffer == null) {
         this.perspBuffer = new ProjectionMatrixBuffer("PIP zone map persp");
         this.orthoRestoreBuffer = new ProjectionMatrixBuffer("PIP zone map ortho");
      }

      RenderSystem.setProjectionMatrix(this.perspBuffer.getBuffer(this.camera(state).projMatrix()), ProjectionType.PERSPECTIVE);
      Pose pose = poseStack.last();
      pose.pose().set(this.camera(state).viewMatrix());

      this.emit(state, poseStack, GeometrySink.of(this.bufferSource));
      this.bufferSource.endBatch();

      this.orthoRestore.setupOrtho(-1000.0F, 1000.0F, width, height, true);
      RenderSystem.setProjectionMatrix(this.orthoRestoreBuffer.getBuffer(this.orthoRestore), ProjectionType.ORTHOGRAPHIC);
   }
   *///?} else {
   /*@Override
   protected void renderToTexture(S state, PoseStack poseStack) {
      this.beforeRender(state);
      if (this.perspBuffer == null) {
         this.perspBuffer = new PerspectiveProjectionMatrixBuffer("PIP zone map persp");
      }

      // backup/restore returns the GUI's ortho projection that the PIP framework set up.
      RenderSystem.backupProjectionMatrix();
      RenderSystem.setProjectionMatrix(this.perspBuffer.getBuffer(this.camera(state).projMatrix()), ProjectionType.PERSPECTIVE);
      Pose pose = poseStack.last();
      pose.pose().set(this.camera(state).viewMatrix());
      this.emit(state, poseStack, GeometrySink.of(this.bufferSource));
      this.bufferSource.endBatch();
      RenderSystem.restoreProjectionMatrix();
   }
   *///?}

   @Override
   public void close() {
      super.close();
      if (this.perspBuffer != null) {
         this.perspBuffer.close();
         this.perspBuffer = null;
      }

      //? if >= 26.1 {
      if (this.orthoRestoreBuffer != null) {
         this.orthoRestoreBuffer.close();
         this.orthoRestoreBuffer = null;
      }
      //?}
   }

   
   static final class FloatList {
      private float[] data = new float[256];
      private int size;

      void add(float v) {
         if (this.size == this.data.length) {
            float[] grown = new float[this.data.length * 2];
            System.arraycopy(this.data, 0, grown, 0, this.size);
            this.data = grown;
         }

         this.data[this.size++] = v;
      }

      float[] toArray() {
         float[] out = new float[this.size];
         System.arraycopy(this.data, 0, out, 0, this.size);
         return out;
      }
   }

   
   static final class IntList {
      private int[] data = new int[256];
      private int size;

      void add(int v) {
         if (this.size == this.data.length) {
            int[] grown = new int[this.data.length * 2];
            System.arraycopy(this.data, 0, grown, 0, this.size);
            this.data = grown;
         }

         this.data[this.size++] = v;
      }

      int[] toArray() {
         int[] out = new int[this.size];
         System.arraycopy(this.data, 0, out, 0, this.size);
         return out;
      }
   }
}
//?}
