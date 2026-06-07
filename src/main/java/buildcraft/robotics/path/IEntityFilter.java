package buildcraft.robotics.path;

import net.minecraft.world.entity.Entity;

/** Predicate over a world entity, used by robot entity-search AIs. */
public interface IEntityFilter {
   boolean matches(Entity entity);
}
