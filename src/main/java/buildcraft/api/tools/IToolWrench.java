package buildcraft.api.tools;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public interface IToolWrench {
   boolean canWrench(Player var1, InteractionHand var2, ItemStack var3, HitResult var4);

   void wrenchUsed(Player var1, InteractionHand var2, ItemStack var3, HitResult var4);
}
