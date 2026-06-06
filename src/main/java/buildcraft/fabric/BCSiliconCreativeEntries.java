package buildcraft.fabric;

import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.gate.EnumGateLogic;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.silicon.item.ItemPluggableLens;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab.Output;

public final class BCSiliconCreativeEntries {
   private BCSiliconCreativeEntries() {
   }

   public static void addMainTabItems(Output output) {
      accept(output, BCSiliconItems.LASER);
      accept(output, BCSiliconItems.ASSEMBLY_TABLE);
      accept(output, BCSiliconItems.ADVANCED_CRAFTING_TABLE);
      accept(output, BCSiliconItems.INTEGRATION_TABLE);
      accept(output, BCSiliconItems.CHIPSET_REDSTONE);
      accept(output, BCSiliconItems.CHIPSET_IRON);
      accept(output, BCSiliconItems.CHIPSET_GOLD);
      accept(output, BCSiliconItems.CHIPSET_QUARTZ);
      accept(output, BCSiliconItems.CHIPSET_DIAMOND);
      accept(output, BCSiliconItems.GATE_COPIER);
   }

   private static void accept(Output output, Item item) {
      if (item != null) {
         output.accept(item);
      }
   }

   public static void addSiliconPlugItems(Output output) {
      ItemPluggableGate gateItem = BCSiliconItems.PLUG_GATE;

      for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
         if (!material.canBeModified) {
            output.accept(gateItem.getStack(new GateVariant(EnumGateLogic.AND, material, EnumGateModifier.NO_MODIFIER)));
         } else {
            for (EnumGateLogic logic : EnumGateLogic.VALUES) {
               for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                  output.accept(gateItem.getStack(new GateVariant(logic, material, modifier)));
               }
            }
         }
      }

      ItemPluggableLens lensItem = BCSiliconItems.PLUG_LENS;
      DyeColor[] legacyOrder = new DyeColor[]{
         DyeColor.BLACK,
         DyeColor.RED,
         DyeColor.GREEN,
         DyeColor.BROWN,
         DyeColor.BLUE,
         DyeColor.PURPLE,
         DyeColor.CYAN,
         DyeColor.LIGHT_GRAY,
         DyeColor.GRAY,
         DyeColor.PINK,
         DyeColor.LIME,
         DyeColor.YELLOW,
         DyeColor.LIGHT_BLUE,
         DyeColor.MAGENTA,
         DyeColor.ORANGE,
         DyeColor.WHITE
      };

      for (DyeColor color : legacyOrder) {
         output.accept(lensItem.getStack(color, false));
      }

      for (DyeColor color : legacyOrder) {
         output.accept(lensItem.getStack(color, true));
      }

      output.accept(lensItem.getStack(null, false));
      output.accept(lensItem.getStack(null, true));
      output.accept(new ItemStack(BCSiliconItems.PLUG_PULSAR));
      output.accept(new ItemStack(BCSiliconItems.PLUG_LIGHT_SENSOR));
      output.accept(new ItemStack(BCSiliconItems.PLUG_TIMER));
   }
}
