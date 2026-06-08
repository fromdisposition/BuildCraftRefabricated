/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import buildcraft.api.core.render.ISprite;

public class SubSprite implements ISprite {
   private final ISprite delegate;
   private final double uMin;
   private final double vMin;
   private final double uMax;
   private final double vMax;

   public SubSprite(ISprite delegate, double uMin, double vMin, double uMax, double vMax) {
      this.delegate = delegate;
      this.uMin = uMin;
      this.vMin = vMin;
      this.uMax = uMax;
      this.vMax = vMax;
   }

   @Override
   public void bindTexture() {
      this.delegate.bindTexture();
   }

   @Override
   public double getInterpU(double u) {
      double iu = this.uMin * (1.0 - u) + this.uMax * u;
      return this.delegate.getInterpU(iu);
   }

   @Override
   public double getInterpV(double v) {
      double iv = this.vMin * (1.0 - v) + this.vMax * v;
      return this.delegate.getInterpV(iv);
   }
}
