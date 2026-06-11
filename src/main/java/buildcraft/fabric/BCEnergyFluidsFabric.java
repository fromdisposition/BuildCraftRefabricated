package buildcraft.fabric;

import buildcraft.energy.BCEnergyConfig;
import buildcraft.fabric.fluid.BcFluidBlock;
import buildcraft.fabric.fluid.BcFluidWorldProperties;
import buildcraft.fabric.fluid.BcOilFluid;
import buildcraft.lib.fluids.FluidTypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class BCEnergyFluidsFabric {
   public static final List<String> BASE_NAMES = List.of(BcFluidWorldProperties.FLUID_NAMES);
   private static final List<BcOilFluid.Holder> HOLDERS = new ArrayList<>();
   private static final List<BCEnergyFluidsFabric.FluidEntry> ENTRIES = new ArrayList<>();
   private static final Map<Fluid, BCEnergyFluidsFabric.FluidEntry> BY_FLUID = new IdentityHashMap<>();
   private static final Map<String, Fluid> BY_NAME = new HashMap<>();
   public static BCEnergyFluidsFabric.FluidEntry OIL_COOL;
   public static final List<BCEnergyFluidsFabric.FluidEntry> ALL = Collections.unmodifiableList(ENTRIES);

   private BCEnergyFluidsFabric() {
   }

   public static void register() {
      HOLDERS.clear();
      ENTRIES.clear();
      BY_FLUID.clear();
      BY_NAME.clear();

      for (int i = 0; i < BcFluidWorldProperties.FLUID_DATA.length; i++) {
         int[] data = BcFluidWorldProperties.FLUID_DATA[i];
         String baseName = BcFluidWorldProperties.FLUID_NAMES[i];

         for (int heat = 0; heat < 3; heat++) {
            BCEnergyFluidsFabric.FluidEntry entry = registerVariant(baseName, heat, data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
            ENTRIES.add(entry);
            BY_FLUID.put(entry.still(), entry);
            BY_FLUID.put(entry.flowing(), entry);
            BY_NAME.put(entry.name(), entry.still());
            if (i == 0 && heat == 0) {
               OIL_COOL = entry;
            }
         }
      }
   }

   private static BCEnergyFluidsFabric.FluidEntry registerVariant(
      String baseName,
      int heat,
      int baseDensity,
      int baseViscosity,
      int boilPoint,
      int baseSpread,
      int texLight,
      int texDark,
      int stickyFlag,
      int flammableFlag
   ) {
      BcFluidWorldProperties props = BcFluidWorldProperties.compute(
         baseName,
         heat,
         baseDensity,
         baseViscosity,
         boilPoint,
         baseSpread,
         texLight,
         texDark,
         BCEnergyConfig.oilIsSticky.get(),
         stickyFlag,
         BCEnergyConfig.enableOilBurn.get(),
         flammableFlag
      );
      int tintColor = -1;
      String regName = BcFluidWorldProperties.regName(baseName, heat);
      Identifier id = Identifier.fromNamespaceAndPath("buildcraftenergy", regName);
      boolean gaseous = props.gaseous();
      BcOilFluid.Holder holder = new BcOilFluid.Holder(
         props, baseName, heat, baseDensity, baseViscosity, boilPoint, baseSpread, texLight, texDark, stickyFlag, flammableFlag
      );
      holder.still = BCRegistries.registerFluid("buildcraftenergy", regName, new BcOilFluid.Source(holder));
      holder.flowing = BCRegistries.registerFluid("buildcraftenergy", regName + "_flowing", new BcOilFluid.Flowing(holder));
      ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
      holder.block = (Block)Registry.register(
         BuiltInRegistries.BLOCK,
         id,
         new BcFluidBlock(
            (FlowingFluid)holder.still,
            Properties.of()
               .setId(blockKey)
               .mapColor(gaseous ? MapColor.NONE : MapColor.COLOR_BLACK)
               .replaceable()
               .strength(100.0F)
               .pushReaction(PushReaction.DESTROY)
               .noLootTable()
               .liquid()
               .sound(SoundType.HONEY_BLOCK)
         )
      );
      if (props.flammable()) {
         FlammableBlockRegistry.getDefaultInstance().add(holder.block, 200, 200);
      }

      holder.bucket = BCRegistries.registerItem(
         "buildcraftenergy", regName + "_bucket", itemProps -> new BucketItem(holder.still, itemProps.craftRemainder(Items.BUCKET).stacksTo(1))
      );
      holder.seal();
      HOLDERS.add(holder);
      FluidTypes.register(holder.still, props.viscosity(), props.density());
      FluidTypes.register(holder.flowing, props.viscosity(), props.density());
      BcFluidAttributesFabric.register(holder.still, holder.flowing, gaseous);
      return new BCEnergyFluidsFabric.FluidEntry(
         regName, baseName, heat, texLight, texDark, tintColor, holder, holder.still, holder.flowing, holder.block, holder.bucket, gaseous
      );
   }

   public static void reapplyConfigProperties() {
      boolean stickyEnabled = BCEnergyConfig.oilIsSticky.get();
      boolean flammableEnabled = BCEnergyConfig.enableOilBurn.get();

      for (BcOilFluid.Holder holder : HOLDERS) {
         holder.reapplyConfig(stickyEnabled, flammableEnabled);
      }
   }

   @Nullable
   public static BCEnergyFluidsFabric.FluidEntry findEntry(Fluid fluid) {
      return fluid == null ? null : BY_FLUID.get(fluid);
   }

   public static Fluid findFluid(String baseName, int heat) {
      String regName = baseName + (heat == 0 ? "" : "_heat_" + heat);
      return BY_NAME.get(regName);
   }

   public static int getHeat(Fluid fluid) {
      if (fluid == null) {
         return -1;
      }

      BCEnergyFluidsFabric.FluidEntry entry = BY_FLUID.get(fluid);
      return entry == null ? -1 : entry.heat();
   }

   public static BlockState sourceBlockState(BCEnergyFluidsFabric.FluidEntry entry) {
      return entry.still().defaultFluidState().createLegacyBlock();
   }

   public static BlockState oilSourceBlockStateForLevel(Level level) {
      return OIL_COOL != null ? sourceBlockState(OIL_COOL) : null;
   }

   public static String getBaseName(Fluid fluid) {
      if (fluid == null) {
         return null;
      }

      BCEnergyFluidsFabric.FluidEntry entry = BY_FLUID.get(fluid);
      return entry == null ? null : entry.baseName();
   }

   public record FluidEntry(
      String name,
      String baseName,
      int heat,
      int texLight,
      int texDark,
      int tintColor,
      BcOilFluid.Holder holder,
      Fluid still,
      Fluid flowing,
      Block block,
      Item bucket,
      boolean gaseous
   ) {
   }
}
