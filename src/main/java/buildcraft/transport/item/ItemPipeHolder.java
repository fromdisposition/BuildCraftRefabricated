/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.item;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;

public class ItemPipeHolder extends BlockItem implements IItemPipe {
   private final Supplier<PipeDefinition> definitionSupplier;

   public ItemPipeHolder(Block block, Supplier<PipeDefinition> definitionSupplier, Properties props) {
      super(block, props);
      this.definitionSupplier = definitionSupplier;
   }

   @Override
   public PipeDefinition getDefinition() {
      return this.definitionSupplier.get();
   }

   public ItemPipeHolder registerWithPipeApi() {
      PipeApi.pipeRegistry.setItemForPipe(this.getDefinition(), this);
      return this;
   }

   public Component getName(ItemStack stack) {
      Component baseName = (Component)(this.isFePipe() ? Component.translatable(this.getDescriptionId()) : super.getName(stack));
      DyeColor col = (DyeColor)stack.get(BCTransportItems.PIPE_COLOUR);
      if (col != null) {
         Component colourName = Component.literal(ColourUtil.getTextFullTooltip(col));
         return Component.literal("").append(colourName).append(" ").append(baseName);
      } else {
         return baseName;
      }
   }

   private boolean isFePipe() {
      PipeDefinition def = this.getDefinition();
      return def.identifier != null && def.identifier.endsWith("_rf");
   }

   public static void appendTooltipLines(ItemPipeHolder item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      PipeDefinition def = item.getDefinition();
      String id = def.identifier;
      int colon = id.indexOf(58);
      String path = colon >= 0 ? id.substring(colon + 1) : id;
      String tipKey = "tip.pipe." + path;
      //? if >= 26.2 {
      if (!I18n.get(tipKey).equals(tipKey)) {
      //?} else {
      /*if (I18n.exists(tipKey)) {
      *///?}
         tooltip.add(Component.literal(I18n.get(tipKey, new Object[0])).withStyle(ChatFormatting.GRAY));
      }

      if (def.flowType == PipeApi.flowFluids) {
         PipeApi.FluidTransferInfo fti = PipeApi.getFluidTransferInfo(def);
         String flow = LocaleUtil.localizeFluidFlow(fti.transferPerTick);
         if (!flow.isEmpty()) {
            tooltip.add(Component.literal(flow).withStyle(ChatFormatting.GRAY));
         }
      } else if (def.flowType == PipeApi.flowPower) {
         PipeApi.PowerTransferInfo pti = PipeApi.getPowerTransferInfo(def);
         String flow = LocaleUtil.localizeMjFlow(pti.transferPerTick);
         if (!flow.isEmpty()) {
            tooltip.add(Component.literal(flow + " per face").withStyle(ChatFormatting.GRAY));
         }
      } else if (def.flowType == PipeApi.flowRf) {
         PipeApi.RedstoneFluxTransferInfo rti = PipeApi.getRfTransferInfo(def);
         String flow = LocaleUtil.localizeRfFlow(rti.transferPerTick);
         if (!flow.isEmpty()) {
            tooltip.add(Component.literal(flow + " per face").withStyle(ChatFormatting.GRAY));
         }
      }
   }
}
