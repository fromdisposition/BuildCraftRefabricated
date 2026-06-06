package buildcraft.factory;

import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.fabric.BCRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.player.Player;

public final class BCFactoryAttachments {
   public static AttachmentType<BCFactoryAttachments.OilAndFuelProduction> OIL_FUEL_PRODUCTION;

   private BCFactoryAttachments() {
   }

   public static void register() {
      OIL_FUEL_PRODUCTION = AttachmentRegistry.create(
         BCRegistries.id("buildcraftfactory", "oil_fuel_production"),
         builder -> builder.initializer(BCFactoryAttachments.OilAndFuelProduction::new)
            .persistent(BCFactoryAttachments.OilAndFuelProduction.CODEC)
            .copyOnDeath()
      );
   }

   public static BCFactoryAttachments.OilAndFuelProduction get(Player player) {
      return (BCFactoryAttachments.OilAndFuelProduction)player.getAttachedOrCreate(OIL_FUEL_PRODUCTION);
   }

   public static final class OilAndFuelProduction {
      public static final int PER_FLUID_TARGET = 16000;
      static final Codec<BCFactoryAttachments.OilAndFuelProduction> CODEC = Codec.INT.listOf().comapFlatMap(list -> {
         int expected = BCEnergyFluidsFabric.BASE_NAMES.size();
         if (list.size() != expected) {
            return DataResult.error(() -> "expected " + expected + " fluid counters, got " + list.size());
         }

         BCFactoryAttachments.OilAndFuelProduction copy = new BCFactoryAttachments.OilAndFuelProduction();

         for (int i = 0; i < expected; i++) {
            copy.amounts[i] = Math.min(16000, Math.max(0, (Integer)list.get(i)));
         }

         return DataResult.success(copy);
      }, production -> Arrays.stream(production.amounts).boxed().toList());
      private final int[] amounts = new int[BCEnergyFluidsFabric.BASE_NAMES.size()];

      public String recordProduction(String baseName, int mb) {
         if (baseName != null && mb > 0) {
            int index = BCEnergyFluidsFabric.BASE_NAMES.indexOf(baseName);
            if (index < 0) {
               return null;
            }

            if (this.amounts[index] >= 16000) {
               return null;
            }

            this.amounts[index] = Math.min(16000, this.amounts[index] + mb);
            return this.amounts[index] >= 16000 ? baseName : null;
         } else {
            return null;
         }
      }

      public boolean isComplete() {
         for (int amount : this.amounts) {
            if (amount < 16000) {
               return false;
            }
         }

         return true;
      }

      public int get(String baseName) {
         int index = BCEnergyFluidsFabric.BASE_NAMES.indexOf(baseName);
         return index < 0 ? -1 : this.amounts[index];
      }
   }
}
