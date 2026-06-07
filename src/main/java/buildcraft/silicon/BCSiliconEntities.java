package buildcraft.silicon;

import buildcraft.fabric.BCRegistries;
import buildcraft.silicon.entity.EntityPackage;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public final class BCSiliconEntities {
   public static EntityType<EntityPackage> PACKAGE;

   private BCSiliconEntities() {
   }

   public static void register() {
      PACKAGE = BCRegistries.registerEntityType(
         "buildcraftsilicon", "package", Builder.of(EntityPackage::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
      );
   }
}
