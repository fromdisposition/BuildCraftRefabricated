/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.item;

import buildcraft.api.blocks.CustomPaintHelper;
import buildcraft.core.BCCore;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.SoundUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ItemPaintbrush_BC8 extends Item {
   private static final int MAX_USES = 64;

   public ItemPaintbrush_BC8(Properties properties) {
      super(properties);
   }

   @Nullable
   private static DyeColor getColour(ItemStack stack) {
      return (DyeColor)stack.get(BCCore.BRUSH_COLOR);
   }

   private static int getUsesLeft(ItemStack stack) {
      Integer uses = (Integer)stack.get(BCCore.BRUSH_USES);
      return uses != null ? uses : 0;
   }

   private static void setBrushData(ItemStack stack, @Nullable DyeColor colour, int usesLeft) {
      if (colour != null && usesLeft > 0) {
         stack.set(BCCore.BRUSH_COLOR, colour);
         stack.set(BCCore.BRUSH_USES, usesLeft);
         stack.set(DataComponents.CUSTOM_MODEL_DATA, buildcraft.lib.compat.BcModelData.index((float)(colour.ordinal() + 1)));
      } else {
         stack.remove(BCCore.BRUSH_COLOR);
         stack.remove(BCCore.BRUSH_USES);
         stack.remove(DataComponents.CUSTOM_MODEL_DATA);
      }
   }

   public static ItemStack createColoredStack(Item paintbrushItem, @Nullable DyeColor colour) {
      ItemStack stack = new ItemStack(paintbrushItem);
      if (colour != null) {
         setBrushData(stack, colour, 64);
      }

      return stack;
   }

   public Component getName(ItemStack stack) {
      DyeColor colour = getColour(stack);
      if (colour != null) {
         String colourName = ColourUtil.getTextFullTooltip(colour);
         return Component.empty().append(Component.literal(colourName + " ")).append(super.getName(stack));
      } else {
         return super.getName(stack);
      }
   }

   public static void appendTooltipLines(ItemPaintbrush_BC8 item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      if (getColour(stack) == null) {
         tooltip.add(Component.translatable("tip.item.paintbrush.clean").withStyle(ChatFormatting.GRAY));
      }
   }

   public boolean isBarVisible(ItemStack stack) {
      DyeColor colour = getColour(stack);
      return colour != null && getUsesLeft(stack) < 64;
   }

   public int getBarWidth(ItemStack stack) {
      int usesLeft = getUsesLeft(stack);
      return Math.round(usesLeft / 64.0F * 13.0F);
   }

   public int getBarColor(ItemStack stack) {
      DyeColor colour = getColour(stack);
      return colour != null ? colour.getTextureDiffuseColor() : super.getBarColor(stack);
   }

   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      Player player = context.getPlayer();
      if (player == null) {
         return InteractionResult.PASS;
      }

      ItemStack stack = context.getItemInHand();
      DyeColor colour = getColour(stack);
      int usesLeft = getUsesLeft(stack);
      if (colour != null && usesLeft <= 0) {
         return InteractionResult.FAIL;
      }

      BlockPos pos = context.getClickedPos();
      Direction side = context.getClickedFace();
      BlockState state = level.getBlockState(pos);
      Vec3 hitPos = context.getClickLocation();
      InteractionResult result = CustomPaintHelper.INSTANCE.attemptPaintBlock(level, pos, state, hitPos, side, colour);
      if (result == InteractionResult.SUCCESS) {
         if (!level.isClientSide()) {
            SoundUtil.playChangeColour(level, pos, colour);
            if (!player.isCreative()) {
               usesLeft--;
            }

            if (usesLeft <= 0) {
               colour = null;
               usesLeft = 0;
            }

            setBrushData(stack, colour, usesLeft);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.FAIL;
      }
   }

   public ItemPaintbrush_BC8.Brush getBrushFromStack(ItemStack stack) {
      return new ItemPaintbrush_BC8.Brush(getColour(stack), getUsesLeft(stack));
   }

   public static class Brush {
      @Nullable
      public final DyeColor colour;
      public final int usesLeft;

      public Brush(@Nullable DyeColor colour, int usesLeft) {
         this.colour = colour;
         this.usesLeft = usesLeft;
      }

      @Override
      public String toString() {
         return "[" + this.usesLeft + " of " + (this.colour == null ? "nothing" : this.colour.getName()) + "]";
      }
   }
}
