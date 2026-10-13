/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IActionInternalSided;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerInternalSided;
import java.util.Collection;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;

public abstract class PipeEventStatement extends PipeEvent {
   public PipeEventStatement(IPipeHolder holder) {
      super(holder);
   }

   public static class AddActionInternal extends PipeEventStatement {
      public final Collection<IActionInternal> actions;

      public AddActionInternal(IPipeHolder holder, Collection<IActionInternal> actions) {
         super(holder);
         this.actions = actions;
      }
   }

   public static class AddActionInternalSided extends PipeEventStatement {
      public final Collection<IActionInternalSided> actions;
      @Nonnull
      public final Direction side;

      public AddActionInternalSided(IPipeHolder holder, Collection<IActionInternalSided> actions, @Nonnull Direction side) {
         super(holder);
         this.actions = actions;
         this.side = side;
      }
   }

   public static class AddTriggerInternal extends PipeEventStatement {
      public final Collection<ITriggerInternal> triggers;

      public AddTriggerInternal(IPipeHolder holder, Collection<ITriggerInternal> triggers) {
         super(holder);
         this.triggers = triggers;
      }
   }

   public static class AddTriggerInternalSided extends PipeEventStatement {
      public final Collection<ITriggerInternalSided> triggers;
      @Nonnull
      public final Direction side;

      public AddTriggerInternalSided(IPipeHolder holder, Collection<ITriggerInternalSided> triggers, @Nonnull Direction side) {
         super(holder);
         this.triggers = triggers;
         this.side = side;
      }
   }
}
