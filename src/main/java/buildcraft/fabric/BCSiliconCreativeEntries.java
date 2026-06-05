package buildcraft.fabric;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import buildcraft.silicon.BCSiliconItems;

public final class BCSiliconCreativeEntries {
    private BCSiliconCreativeEntries() {}

    public static void addMainTabItems(CreativeModeTab.Output output) {
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

    private static void accept(CreativeModeTab.Output output, buildcraft.fabric.registry.DeferredItem<?> item) {
        if (item == null) {
            return;
        }
        try {
            output.accept(item.get());
        } catch (IllegalStateException ignored) {
        }
    }

    public static void addSiliconPlugItems(CreativeModeTab.Output output) {
        buildcraft.silicon.item.ItemPluggableGate gateItem = BCSiliconItems.PLUG_GATE.get();
        for (buildcraft.silicon.gate.EnumGateMaterial material : buildcraft.silicon.gate.EnumGateMaterial.VALUES) {
            if (!material.canBeModified) {
                output.accept(gateItem.getStack(new buildcraft.silicon.gate.GateVariant(
                        buildcraft.silicon.gate.EnumGateLogic.AND, material,
                        buildcraft.silicon.gate.EnumGateModifier.NO_MODIFIER)));
                continue;
            }
            for (buildcraft.silicon.gate.EnumGateLogic logic : buildcraft.silicon.gate.EnumGateLogic.VALUES) {
                for (buildcraft.silicon.gate.EnumGateModifier modifier :
                        buildcraft.silicon.gate.EnumGateModifier.VALUES) {
                    output.accept(gateItem.getStack(
                            new buildcraft.silicon.gate.GateVariant(logic, material, modifier)));
                }
            }
        }

        buildcraft.silicon.item.ItemPluggableLens lensItem = BCSiliconItems.PLUG_LENS.get();
        net.minecraft.world.item.DyeColor[] legacyOrder = new net.minecraft.world.item.DyeColor[] {
                net.minecraft.world.item.DyeColor.BLACK,
                net.minecraft.world.item.DyeColor.RED,
                net.minecraft.world.item.DyeColor.GREEN,
                net.minecraft.world.item.DyeColor.BROWN,
                net.minecraft.world.item.DyeColor.BLUE,
                net.minecraft.world.item.DyeColor.PURPLE,
                net.minecraft.world.item.DyeColor.CYAN,
                net.minecraft.world.item.DyeColor.LIGHT_GRAY,
                net.minecraft.world.item.DyeColor.GRAY,
                net.minecraft.world.item.DyeColor.PINK,
                net.minecraft.world.item.DyeColor.LIME,
                net.minecraft.world.item.DyeColor.YELLOW,
                net.minecraft.world.item.DyeColor.LIGHT_BLUE,
                net.minecraft.world.item.DyeColor.MAGENTA,
                net.minecraft.world.item.DyeColor.ORANGE,
                net.minecraft.world.item.DyeColor.WHITE
        };
        for (net.minecraft.world.item.DyeColor color : legacyOrder) {
            output.accept(lensItem.getStack(color, false));
        }
        for (net.minecraft.world.item.DyeColor color : legacyOrder) {
            output.accept(lensItem.getStack(color, true));
        }
        output.accept(lensItem.getStack(null, false));
        output.accept(lensItem.getStack(null, true));
        output.accept(new ItemStack(BCSiliconItems.PLUG_PULSAR.get()));
        output.accept(new ItemStack(BCSiliconItems.PLUG_LIGHT_SENSOR.get()));
        output.accept(new ItemStack(BCSiliconItems.PLUG_TIMER.get()));
    }
}
