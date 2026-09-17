/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.transport.pipe;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.world.item.Item;

public interface IPipeRegistry {
   PipeDefinition getDefinition(String var1);

   void registerPipe(PipeDefinition var1);

   void setItemForPipe(PipeDefinition var1, @Nullable IItemPipe var2);

   IItemPipe getItemForPipe(PipeDefinition var1);

   IItemPipe createItemForPipe(PipeDefinition var1);

   IItemPipe createUnnamedItemForPipe(PipeDefinition var1, Consumer<Item> var2);

   Iterable<PipeDefinition> getAllRegisteredPipes();
}
