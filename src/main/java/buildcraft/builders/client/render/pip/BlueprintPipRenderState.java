/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.client.render.pip;

import buildcraft.builders.snapshot.Snapshot;
import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
//?}

//? if >= 1.21.10 {
public record BlueprintPipRenderState(
   Snapshot snapshot, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
   public BlueprintPipRenderState(Snapshot snapshot, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea) {
      this(snapshot, x0, y0, x1, y1, scale, scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
   }
}
//?} else {
/*// 1.21.1: PictureInPictureRenderState does not exist; this preview record is unused (cosmetic loss).
public record BlueprintPipRenderState(
   Snapshot snapshot, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds
) {
   public BlueprintPipRenderState(Snapshot snapshot, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea) {
      this(snapshot, x0, y0, x1, y1, scale, scissorArea, null);
   }
}
*///?}
