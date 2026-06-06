package buildcraft.core.item;

import buildcraft.api.blocks.CustomRotationHelper;
import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.tools.IToolWrench;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ItemWrench_Neptune extends Item implements IToolWrench {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftcore:wrenched");

   public ItemWrench_Neptune(Properties properties) {
      super(properties);
   }

   @Override
   public boolean canWrench(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace) {
      return true;
   }

   @Override
   public void wrenchUsed(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace) {
      AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
      player.swing(hand);
   }

   public InteractionResult trySneakRotate(ItemStack stack, UseOnContext context) {
      Player player = context.getPlayer();
      if (player != null && player.isShiftKeyDown()) {
         Level world = context.getLevel();
         BlockPos pos = context.getClickedPos();
         BlockState state = world.getBlockState(pos);
         Block block = state.getBlock();
         if (block instanceof ICustomRotationHandler) {
            return InteractionResult.PASS;
         }

         Direction side = context.getClickedFace();
         InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);
         if (result == InteractionResult.PASS) {
            return InteractionResult.PASS;
         }

         if (result == InteractionResult.SUCCESS) {
            BlockHitResult hit = new BlockHitResult(context.getClickLocation(), side, pos, context.isInside());
            this.wrenchUsed(player, context.getHand(), stack, hit);
         }

         SoundUtil.playSlideSound(world, pos, state, result);
         return result;
      } else {
         return InteractionResult.PASS;
      }
   }

   public InteractionResult useOn(UseOnContext context) {
      if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
         InteractionResult sneak = this.trySneakRotate(context.getItemInHand(), context);
         if (sneak != InteractionResult.PASS) {
            return sneak;
         }
      }

      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Player player = context.getPlayer();
      InteractionHand hand = context.getHand();
      Direction side = context.getClickedFace();
      BlockState state = world.getBlockState(pos);
      InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state, side);
      if (result == InteractionResult.SUCCESS && player != null) {
         BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), side, pos, context.isInside());
         this.wrenchUsed(player, hand, context.getItemInHand(), hitResult);
      }

      SoundUtil.playSlideSound(world, pos, state, result);
      return result;
   }
}
