/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client.render;

public final class PipeRenderContext {
   private static final ThreadLocal<Integer> PACKED_LIGHT = ThreadLocal.withInitial(() -> 0);

   private PipeRenderContext() {
   }

   public static void setPackedLight(int packedLight) {
      PACKED_LIGHT.set(packedLight);
   }

   public static int getPackedLight() {
      return PACKED_LIGHT.get();
   }

   public static int blockLight() {
      return (getPackedLight() & 65535) >> 4;
   }

   public static int skyLight() {
      return (getPackedLight() >> 16 & 65535) >> 4;
   }
}
