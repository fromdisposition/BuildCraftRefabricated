/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.RenderPipelines;
//?}

/**
 * Thin GuiGraphicsExtractor wrapper. On 1.21.5+ the GuiGraphicsExtractor draw methods take a RenderPipeline first arg (always
 * GUI_TEXTURED for these flat 2D blits) and pose() is a 2D Matrix3x2fStack; on 1.21.1 the draw methods have no
 * pipeline arg and pose() is the PoseStack. The public API here hides both: blit/blitSprite/fill take no pipeline
 * (it is supplied internally on modern), and GUI transforms go through pushPoseGui/translateGui/popPoseGui.
 */
public final class BCGraphics {
   public final GuiGraphicsExtractor raw;

   public BCGraphics(GuiGraphicsExtractor raw) {
      this.raw = raw;
   }

   public void text(Font font, Component text, int x, int y, int color) {
      this.raw.text(font, text, x, y, color);
   }

   public void text(Font font, Component text, int x, int y, int color, boolean dropShadow) {
      this.raw.text(font, text, x, y, color, dropShadow);
   }

   public void text(Font font, String text, int x, int y, int color) {
      this.raw.text(font, text, x, y, color);
   }

   public void text(Font font, String text, int x, int y, int color, boolean dropShadow) {
      this.raw.text(font, text, x, y, color, dropShadow);
   }

   public void text(Font font, FormattedCharSequence text, int x, int y, int color) {
      this.raw.text(font, text, x, y, color);
   }

   public void text(Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {
      this.raw.text(font, text, x, y, color, dropShadow);
   }

   public void item(ItemStack stack, int x, int y) {
      this.raw.item(stack, x, y);
   }

   public void item(ItemStack stack, int x, int y, int seed) {
      this.raw.item(stack, x, y, seed);
   }

   public void fakeItem(ItemStack stack, int x, int y) {
      this.raw.fakeItem(stack, x, y);
   }

   public void fakeItem(ItemStack stack, int x, int y, int seed) {
      this.raw.fakeItem(stack, x, y, seed);
   }

   public void itemDecorations(Font font, ItemStack stack, int x, int y) {
      this.raw.itemDecorations(font, stack, x, y);
   }

   public void itemDecorations(Font font, ItemStack stack, int x, int y, String text) {
      this.raw.itemDecorations(font, stack, x, y, text);
   }

   public void fill(int x1, int y1, int x2, int y2, int color) {
      this.raw.fill(x1, y1, x2, y2, color);
   }

   public void enableScissor(int x1, int y1, int x2, int y2) {
      this.raw.enableScissor(x1, y1, x2, y2);
   }

   public void disableScissor() {
      this.raw.disableScissor();
   }

   public void nextStratum() {
      //? if >= 1.21.10 {
      this.raw.nextStratum();
      //?}
   }

   public void pushPoseGui() {
      //? if >= 1.21.10 {
      this.raw.pose().pushMatrix();
      //?} else {
      /*this.raw.pose().pushPose();
      *///?}
   }

   public void popPoseGui() {
      //? if >= 1.21.10 {
      this.raw.pose().popMatrix();
      //?} else {
      /*this.raw.pose().popPose();
      *///?}
   }

   public void translateGui(float x, float y) {
      //? if >= 1.21.10 {
      this.raw.pose().translate(x, y);
      //?} else {
      /*this.raw.pose().translate(x, y, 0.0F);
      *///?}
   }

   /**
    * Rotates the GUI pose around its origin by {@code radians}. Screen space is y-down, so a positive angle turns
    * clockwise: local (x, y) lands at (-y, x). Determinant stays +1, which matters because GUI_TEXTURED is a
    * back-face-culling pipeline -- a mirroring pose would flip the quad winding and drop the draw entirely.
    */
   public void rotateGui(float radians) {
      //? if >= 1.21.10 {
      this.raw.pose().rotate(radians);
      //?} else {
      /*this.raw.pose().mulPose(com.mojang.math.Axis.ZP.rotation(radians));
      *///?}
   }

   public void blitSprite(TextureAtlasSprite sprite, int x, int y, int width, int height) {
      //? if >= 1.21.10 {
      this.raw.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
      //?} else {
      /*this.raw.blit(x, y, 0, width, height, sprite);
      *///?}
   }

   public void blitSprite(Identifier texture, int x, int y, int width, int height) {
      //? if >= 1.21.10 {
      this.raw.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height);
      //?} else {
      /*this.raw.blitSprite(texture, x, y, width, height);
      *///?}
   }

   public void blitSprite(TextureAtlasSprite sprite, int x, int y, int width, int height, int color) {
      //? if >= 1.21.10 {
      this.raw.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, color);
      //?} else {
      /*float a = (color >>> 24 & 0xFF) / 255.0F;
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      this.raw.blit(x, y, 0, width, height, sprite, r, g, b, a);
      *///?}
   }

   public void blitSprite(Identifier texture, int x, int y, int width, int height, int color) {
      //? if >= 1.21.10 {
      this.raw.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, width, height, color);
      //?} else {
      /*this.raw.blitSprite(texture, x, y, width, height, color);
      *///?}
   }

   public void blit(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
      //? if >= 1.21.10 {
      this.raw.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
      //?} else {
      /*this.raw.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
      *///?}
   }

   public void blit(
      Identifier texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight, int textureSize
   ) {
      //? if >= 1.21.10 {
      this.raw.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureSize);
      //?} else {
      /*// 1.21.1 scaled-region blit: blit(rl, x, y, width, height, u, v, regionWidth, regionHeight, texW, texH).
      this.raw.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureSize, textureSize);
      *///?}
   }

   public void blit(
      Identifier texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight, int textureWidth, int textureHeight
   ) {
      //? if >= 1.21.10 {
      this.raw.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight);
      //?} else {
      /*this.raw.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
      *///?}
   }

   public void blit(
      Identifier texture,
      int x,
      int y,
      float u,
      float v,
      int width,
      int height,
      int regionWidth,
      int regionHeight,
      int textureWidth,
      int textureHeight,
      int color
   ) {
      //? if >= 1.21.10 {
      this.raw.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, color);
      //?} else {
      /*// 1.21.1 GuiGraphicsExtractor has no colour-tinted Identifier region blit; apply the tint via the
      // global setColor multiply around the untinted scaled-region blit (used for fluid sprites in GUIs).
      float ca = (color >>> 24 & 0xFF) / 255.0F;
      float cr = (color >> 16 & 0xFF) / 255.0F;
      float cg = (color >> 8 & 0xFF) / 255.0F;
      float cb = (color & 0xFF) / 255.0F;
      if (ca == 0.0F) {
         ca = 1.0F;
      }
      this.raw.setColor(cr, cg, cb, ca);
      this.raw.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
      this.raw.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      *///?}
   }

   public void setTooltipForNextFrame(Component text, int x, int y) {
      //? if >= 1.21.10 {
      this.raw.setTooltipForNextFrame(text, x, y);
      //?} else {
      /*this.raw.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, text, x, y);
      *///?}
   }

   public void setTooltipForNextFrame(Font font, Component text, int x, int y) {
      //? if >= 1.21.10 {
      this.raw.setTooltipForNextFrame(font, text, x, y);
      //?} else {
      /*this.raw.renderTooltip(font, text, x, y);
      *///?}
   }

   public void setTooltipForNextFrame(Font font, ItemStack stack, int x, int y) {
      //? if >= 1.21.10 {
      this.raw.setTooltipForNextFrame(font, stack, x, y);
      //?} else {
      /*this.raw.renderTooltip(font, stack, x, y);
      *///?}
   }

   public void setTooltipForNextFrame(List<FormattedCharSequence> lines, int x, int y) {
      //? if >= 1.21.10 {
      this.raw.setTooltipForNextFrame(lines, x, y);
      //?} else {
      /*this.raw.renderTooltip(net.minecraft.client.Minecraft.getInstance().font, lines, x, y);
      *///?}
   }

   public void setTooltipForNextFrame(Font font, List<FormattedCharSequence> lines, int x, int y) {
      //? if >= 1.21.10 {
      this.raw.setTooltipForNextFrame(font, lines, x, y);
      //?} else {
      /*this.raw.renderTooltip(font, lines, x, y);
      *///?}
   }

   public void setTooltipForNextFrame(Font font, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, int x, int y) {
      //? if >= 1.21.10 {
      this.raw.setTooltipForNextFrame(font, textComponents, tooltipComponent, x, y);
      //?} else {
      /*this.raw.renderTooltip(font, textComponents, tooltipComponent, x, y);
      *///?}
   }
}
