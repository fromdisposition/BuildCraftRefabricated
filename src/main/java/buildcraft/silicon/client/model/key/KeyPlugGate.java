/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client.model.key;

import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.silicon.gate.GateVariant;
import net.minecraft.core.Direction;

public class KeyPlugGate extends PluggableModelKey {
   public final GateVariant variant;

   public KeyPlugGate(Object layer, Direction side, GateVariant variant) {
      super(layer, side);
      this.variant = variant;
   }

   /**
    * Identity is deliberately side+variant only: the baker bakes just the static gate body, and the on/off
    * indicator is drawn by the dynamic renderer — so the active state must NOT split the model cache.
    */
   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         KeyPlugGate other = (KeyPlugGate)obj;
         return other.side == this.side && other.variant.equals(this.variant);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.side.hashCode() * 31 + this.variant.hashCode();
   }
}
