package buildcraft.energy.worldgen.integration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
//? if >= 26.1.3 {
/*import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;*/
//?} else {
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
//?}
import net.minecraft.server.level.ServerPlayer;

public final class OilDesignBiomeNearbyTrigger extends SimpleCriterionTrigger<OilDesignBiomeNearbyTrigger.TriggerInstance> {
   @Override
   public Codec<TriggerInstance> codec() {
      return TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer player) {
      this.trigger(player, instance -> true);
   }

   //? if >= 26.1.3 {
/*   public record TriggerInstance(Optional<ContextAwarePredicate> player)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)
      ).apply(instance, TriggerInstance::new));
   }*/
   //?} else {
   public record TriggerInstance(Optional<net.minecraft.advancements.criterion.ContextAwarePredicate> player)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
         EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player)
      ).apply(instance, TriggerInstance::new));
   }
   //?}
}
