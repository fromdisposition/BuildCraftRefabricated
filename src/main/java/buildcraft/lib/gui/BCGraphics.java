/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
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
import org.joml.Matrix3x2fStack;

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

   public void fill(RenderPipeline pipeline, int x1, int y1, int x2, int y2, int color) {
      this.raw.fill(pipeline, x1, y1, x2, y2, color);
   }

   public void enableScissor(int x1, int y1, int x2, int y2) {
      this.raw.enableScissor(x1, y1, x2, y2);
   }

   public void disableScissor() {
      this.raw.disableScissor();
   }

   public void nextStratum() {
      this.raw.nextStratum();
   }

   public Matrix3x2fStack pose() {
      return this.raw.pose();
   }

   public void blitSprite(RenderPipeline pipeline, TextureAtlasSprite sprite, int x, int y, int width, int height) {
      this.raw.blitSprite(pipeline, sprite, x, y, width, height);
   }

   public void blitSprite(RenderPipeline pipeline, Identifier texture, int x, int y, int width, int height) {
      this.raw.blitSprite(pipeline, texture, x, y, width, height);
   }

   public void blitSprite(RenderPipeline pipeline, TextureAtlasSprite sprite, int x, int y, int width, int height, int color) {
      this.raw.blitSprite(pipeline, sprite, x, y, width, height, color);
   }

   public void blit(RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
      this.raw.blit(pipeline, texture, x, y, u, v, width, height, textureWidth, textureHeight);
   }

   public void blit(
      RenderPipeline pipeline, Identifier texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight, int textureSize
   ) {
      this.raw.blit(pipeline, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureSize);
   }

   public void blit(
      RenderPipeline pipeline,
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
      int textureHeight
   ) {
      this.raw.blit(pipeline, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight);
   }

   public void blit(
      RenderPipeline pipeline,
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
      this.raw.blit(pipeline, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, color);
   }

   public void setTooltipForNextFrame(Component text, int x, int y) {
      this.raw.setTooltipForNextFrame(text, x, y);
   }

   public void setTooltipForNextFrame(Font font, Component text, int x, int y) {
      this.raw.setTooltipForNextFrame(font, text, x, y);
   }

   public void setTooltipForNextFrame(Font font, ItemStack stack, int x, int y) {
      this.raw.setTooltipForNextFrame(font, stack, x, y);
   }

   public void setTooltipForNextFrame(List<FormattedCharSequence> lines, int x, int y) {
      this.raw.setTooltipForNextFrame(lines, x, y);
   }

   public void setTooltipForNextFrame(Font font, List<FormattedCharSequence> lines, int x, int y) {
      this.raw.setTooltipForNextFrame(font, lines, x, y);
   }

   public void setTooltipForNextFrame(Font font, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, int x, int y) {
      this.raw.setTooltipForNextFrame(font, textComponents, tooltipComponent, x, y);
   }
}
