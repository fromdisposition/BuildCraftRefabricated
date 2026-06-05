/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.energy.BCEnergyConfig;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportItems;

@SuppressWarnings("deprecation")
public class ItemPipeHolder extends BlockItem implements IItemPipe {
    public final PipeDefinition definition;

    public ItemPipeHolder(Block block, PipeDefinition definition, Item.Properties props) {
        super(block, props);
        this.definition = definition;
    }

    @Override
    public PipeDefinition getDefinition() {
        return definition;
    }

    public ItemPipeHolder registerWithPipeApi() {
        PipeApi.pipeRegistry.setItemForPipe(definition, this);
        return this;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = isFePipe()
                ? Component.translatable(BCEnergyConfig.rfFeKey(getDescriptionId()))
                : super.getName(stack);
        DyeColor col = stack.get(BCTransportItems.PIPE_COLOUR.get());
        if (col != null) {
            Component colourName = Component.literal(ColourUtil.getTextFullTooltip(col));
            return Component.literal("").append(colourName).append(" ").append(baseName);
        }
        return baseName;
    }

    private boolean isFePipe() {
        return definition.identifier != null && definition.identifier.endsWith("_rf");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {

        String id = definition.identifier;
        int colon = id.indexOf(':');
        String path = colon >= 0 ? id.substring(colon + 1) : id;
        String tipKey = "tip.pipe." + path;
        if (I18n.exists(tipKey)) {
            tooltip.accept(Component.literal(I18n.get(tipKey)).withStyle(ChatFormatting.GRAY));
        }

        if (definition.flowType == PipeApi.flowFluids) {
            PipeApi.FluidTransferInfo fti = PipeApi.getFluidTransferInfo(definition);
            tooltip.accept(Component.literal(LocaleUtil.localizeFluidFlow(fti.transferPerTick))
                    .withStyle(ChatFormatting.GRAY));
        } else if (definition.flowType == PipeApi.flowPower) {
            PipeApi.PowerTransferInfo pti = PipeApi.getPowerTransferInfo(definition);
            tooltip.accept(Component.literal(LocaleUtil.localizeMjFlow(pti.transferPerTick) + " per face")
                    .withStyle(ChatFormatting.GRAY));
        } else if (definition.flowType == PipeApi.flowRf) {
            PipeApi.RedstoneFluxTransferInfo rti = PipeApi.getRfTransferInfo(definition);
            tooltip.accept(Component.literal(LocaleUtil.localizeRfFlow(rti.transferPerTick) + " per face")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
