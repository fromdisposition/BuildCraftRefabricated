/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

public abstract class PipeEventTileState extends PipeEvent {
   PipeEventTileState(IPipeHolder holder) {
      super(holder);
   }

   public static class ChunkUnload extends PipeEventTileState {
      public ChunkUnload(IPipeHolder holder) {
         super(holder);
      }
   }

   public static class Invalidate extends PipeEventTileState {
      public Invalidate(IPipeHolder holder) {
         super(holder);
      }
   }

   public static class Validate extends PipeEventTileState {
      public Validate(IPipeHolder holder) {
         super(holder);
      }
   }
}
