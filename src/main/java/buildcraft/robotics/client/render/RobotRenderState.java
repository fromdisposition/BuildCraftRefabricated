package buildcraft.robotics.client.render;

import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;

public class RobotRenderState extends EntityRenderState {
   public Identifier texture = EntityRobot.DEFAULT_TEXTURE;
   public float energy;
   public float aimYaw;
   public final ItemStackRenderState heldItemState = new ItemStackRenderState();
}
