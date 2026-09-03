/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.client.render.pip;

//? if >= 1.21.10 {
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import javax.annotation.Nullable;

/** The hover, selection and painted-zone layer of the zone planner map, drawn over the terrain layer with the same camera. */
public record ZoneMapOverlayPipRenderState(ZoneMapPipRenderState map) implements PictureInPictureRenderState {
   @Override
   public int x0() {
      return this.map.x0();
   }

   @Override
   public int y0() {
      return this.map.y0();
   }

   @Override
   public int x1() {
      return this.map.x1();
   }

   @Override
   public int y1() {
      return this.map.y1();
   }

   @Override
   public float scale() {
      return this.map.scale();
   }

   @Nullable
   @Override
   public ScreenRectangle scissorArea() {
      return this.map.scissorArea();
   }

   @Nullable
   @Override
   public ScreenRectangle bounds() {
      return this.map.bounds();
   }
}
//?}
