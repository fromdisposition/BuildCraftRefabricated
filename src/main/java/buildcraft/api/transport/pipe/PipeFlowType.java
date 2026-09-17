/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import net.minecraft.nbt.CompoundTag;

public final class PipeFlowType {
   public final PipeFlowType.IFlowCreator creator;
   public final PipeFlowType.IFlowLoader loader;
   public EnumPipeColourType fallbackColourType;

   public PipeFlowType(PipeFlowType.IFlowCreator creator, PipeFlowType.IFlowLoader loader) {
      this(creator, loader, null);
   }

   public PipeFlowType(PipeFlowType.IFlowCreator creator, PipeFlowType.IFlowLoader loader, EnumPipeColourType colourType) {
      this.creator = creator;
      this.loader = loader;
      this.fallbackColourType = colourType;
   }

   @FunctionalInterface
   public interface IFlowCreator {
      PipeFlow createFlow(IPipe var1);
   }

   @FunctionalInterface
   public interface IFlowLoader {
      PipeFlow loadFlow(IPipe var1, CompoundTag var2);
   }
}
