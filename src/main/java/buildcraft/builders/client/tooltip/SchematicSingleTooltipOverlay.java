/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.tooltip;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.client.render.BlueprintRenderer;
import buildcraft.builders.snapshot.Blueprint;
import buildcraft.builders.tooltip.SchematicPreviewTooltipComponent;
import buildcraft.lib.gui.BCGraphics;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class SchematicSingleTooltipOverlay implements ClientTooltipComponent {
   public static final int PREVIEW_SIZE = 100;
   @Nullable
   private static ISchematicBlock cachedSchematic;
   @Nullable
   private static Blueprint cachedSynthetic;
   private final ISchematicBlock schematic;

   public SchematicSingleTooltipOverlay(SchematicPreviewTooltipComponent component) {
      this.schematic = component.schematic();
   }

   @Override
   public int getHeight(Font font) {
      return PREVIEW_SIZE;
   }

   @Override
   public int getWidth(Font font) {
      return PREVIEW_SIZE;
   }

   @Override
   public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
      Blueprint synthetic = getOrBuildSynthetic(this.schematic);
      BlueprintRenderer.renderSnapshotForTooltip(new BCGraphics(graphics), synthetic, x, y, PREVIEW_SIZE, PREVIEW_SIZE);
   }

   private static Blueprint getOrBuildSynthetic(ISchematicBlock schematic) {
      Blueprint cached = cachedSynthetic;
      if (cached != null && schematic.equals(cachedSchematic)) {
         return cached;
      }

      Blueprint synthetic = new Blueprint();
      synthetic.size = new BlockPos(1, 1, 1);
      synthetic.offset = BlockPos.ZERO;
      synthetic.facing = Direction.NORTH;
      synthetic.data = new int[]{0};
      synthetic.palette.add(schematic);
      cachedSchematic = schematic;
      cachedSynthetic = synthetic;
      return synthetic;
   }
}
