package buildcraft.transport.item;

import buildcraft.api.transport.pipe.IItemPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.energy.BCEnergyConfig;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportItems;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
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
      Component baseName = (Component)(this.isFePipe() ? Component.translatable(BCEnergyConfig.rfFeKey(this.getDescriptionId())) : super.getName(stack));
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

   @Override
   @SuppressWarnings("deprecation")
   public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
      PipeDefinition def = this.getDefinition();
      String id = def.identifier;
      int colon = id.indexOf(58);
      String path = colon >= 0 ? id.substring(colon + 1) : id;
      String tipKey = "tip.pipe." + path;
      if (I18n.exists(tipKey)) {
         tooltip.accept(Component.literal(I18n.get(tipKey, new Object[0])).withStyle(ChatFormatting.GRAY));
      }

      if (def.flowType == PipeApi.flowFluids) {
         PipeApi.FluidTransferInfo fti = PipeApi.getFluidTransferInfo(def);
         tooltip.accept(Component.literal(LocaleUtil.localizeFluidFlow(fti.transferPerTick)).withStyle(ChatFormatting.GRAY));
      } else if (def.flowType == PipeApi.flowPower) {
         PipeApi.PowerTransferInfo pti = PipeApi.getPowerTransferInfo(def);
         tooltip.accept(Component.literal(LocaleUtil.localizeMjFlow(pti.transferPerTick) + " per face").withStyle(ChatFormatting.GRAY));
      } else if (def.flowType == PipeApi.flowRf) {
         PipeApi.RedstoneFluxTransferInfo rti = PipeApi.getRfTransferInfo(def);
         tooltip.accept(Component.literal(LocaleUtil.localizeRfFlow(rti.transferPerTick) + " per face").withStyle(ChatFormatting.GRAY));
      }
   }
}
