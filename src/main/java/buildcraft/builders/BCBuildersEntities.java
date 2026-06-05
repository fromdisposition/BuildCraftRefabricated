package buildcraft.builders;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import buildcraft.builders.entity.EntityQuarryRig;
import buildcraft.fabric.BCRegistries;

public final class BCBuildersEntities {
    public static EntityType<EntityQuarryRig> QUARRY_RIG;

    private BCBuildersEntities() {}

    public static void register() {
        QUARRY_RIG = BCRegistries.registerEntityType(BCBuilders.MODID, 
                "quarry_rig",
                EntityType.Builder.<EntityQuarryRig>of(EntityQuarryRig::new, MobCategory.MISC)
                        .sized(1.0F, 1.0F)
                        .clientTrackingRange(4)
                        .updateInterval(1));
    }
}
