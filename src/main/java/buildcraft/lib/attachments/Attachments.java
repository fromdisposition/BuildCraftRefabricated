/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package buildcraft.lib.attachments;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.access.ItemAccess;
import buildcraft.lib.transfer.energy.EnergyHandler;
import buildcraft.lib.transfer.fluid.FluidResource;
import buildcraft.lib.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public final class Attachments {
    public static final class Energy {
        public static final BlockAttachment<EnergyHandler, @Nullable Direction> BLOCK = BlockAttachment.createSided(create("energy_handler"), EnergyHandler.class);
        public static final EntityAttachment<EnergyHandler, @Nullable Direction> ENTITY = EntityAttachment.createSided(create("energy_handler"), EnergyHandler.class);
        public static final ItemAttachment<EnergyHandler, ItemAccess> ITEM = ItemAttachment.create(create("energy_handler"), EnergyHandler.class, ItemAccess.class);

        private Energy() {}
    }

    public static final class Fluid {
        public static final BlockAttachment<ResourceHandler<FluidResource>, @Nullable Direction> BLOCK = BlockAttachment.createSided(create("fluid_handler"), ResourceHandler.asClass());
        public static final EntityAttachment<ResourceHandler<FluidResource>, @Nullable Direction> ENTITY = EntityAttachment.createSided(create("fluid_handler"), ResourceHandler.asClass());
        public static final ItemAttachment<ResourceHandler<FluidResource>, ItemAccess> ITEM = ItemAttachment.create(create("fluid_handler"), ResourceHandler.asClass(), ItemAccess.class);

        private Fluid() {}
    }

    public static final class Item {
        public static final BlockAttachment<ResourceHandler<ItemResource>, @Nullable Direction> BLOCK = BlockAttachment.createSided(create("item_handler"), ResourceHandler.asClass());

        public static final EntityAttachment<ResourceHandler<ItemResource>, @Nullable Void> ENTITY = EntityAttachment.createVoid(create("item_handler"), ResourceHandler.asClass());

        public static final EntityAttachment<ResourceHandler<ItemResource>, @Nullable Direction> ENTITY_AUTOMATION = EntityAttachment.createSided(create("item_handler_automation"), ResourceHandler.asClass());
        public static final ItemAttachment<ResourceHandler<ItemResource>, ItemAccess> ITEM = ItemAttachment.create(create("item_handler"), ResourceHandler.asClass(), ItemAccess.class);

        private Item() {}
    }

    private static Identifier create(String path) {
        return Identifier.fromNamespaceAndPath("buildcraftlib", path);
    }

    private Attachments() {}
}
