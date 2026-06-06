package buildcraft.silicon.item;

import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadeItem;
import buildcraft.api.transport.IItemPluggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.SoundUtil;
import buildcraft.silicon.BCSiliconPlugs;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadePhasedState;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.silicon.plug.PluggableFacade;
import com.google.common.collect.UnmodifiableIterator;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;

public class ItemPluggableFacade extends Item implements IItemPluggable, IFacadeItem {
   public ItemPluggableFacade(Properties properties) {
      super(properties);
   }

   @Nonnull
   public ItemStack createItemStack(FacadeInstance state) {
      ItemStack item = new ItemStack(this);
      CompoundTag nbt = NBTUtilBC.getItemData(item);
      nbt.put("facade", state.writeToNbt());
      NBTUtilBC.setItemData(item, nbt);
      return item;
   }

   public static FacadeInstance getStates(@Nonnull ItemStack item) {
      CompoundTag nbt = NBTUtilBC.getItemData(item);
      String strPreview = nbt.getStringOr("preview", "");
      if ("basic".equalsIgnoreCase(strPreview)) {
         return FacadeInstance.createSingle(FacadeStateManager.previewState, false);
      }

      if (!nbt.contains("facade") && nbt.contains("states")) {
         ListTag statesList = nbt.getListOrEmpty("states");
         if (!statesList.isEmpty()) {
            boolean isHollow = statesList.get(0) instanceof CompoundTag ct && ct.getBooleanOr("isHollow", false);
            CompoundTag tagFacade = new CompoundTag();
            tagFacade.putBoolean("isHollow", isHollow);
            tagFacade.put("states", statesList);
            nbt.put("facade", tagFacade);
         }
      }

      return FacadeInstance.readFromNbt(nbt.getCompoundOrEmpty("facade"));
   }

   @Nonnull
   @Override
   public ItemStack getFacadeForBlock(BlockState state) {
      FacadeBlockStateInfo info = FacadeStateManager.validFacadeStates.get(state);
      return info == null ? ItemStack.EMPTY : this.createItemStack(FacadeInstance.createSingle(info, false));
   }

   @Override
   public PipePluggable onPlace(@Nonnull ItemStack stack, IPipeHolder holder, Direction side, Player player, InteractionHand hand) {
      FacadeInstance fullState = getStates(stack);
      SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos(), fullState.phasedStates[0].stateInfo.state);
      return new PluggableFacade(BCSiliconPlugs.facade, holder, side, fullState);
   }

   @Nonnull
   @Override
   public AABB getPlacementBoundingBox(@Nonnull ItemStack stack, Direction side) {
      return PluggableFacade.boundingBoxFor(side);
   }

   public Component getName(ItemStack stack) {
      FacadeInstance fullState = getStates(stack);
      if (fullState.type == FacadeType.Basic) {
         String displayName = getFacadeStateDisplayName(fullState.phasedStates[0]);
         return (Component)(displayName.isEmpty() ? super.getName(stack) : super.getName(stack).copy().append(": " + displayName));
      } else {
         return Component.translatable("item.buildcraftsilicon.plug_facade_phased");
      }
   }

   public static String getFacadeStateDisplayName(FacadePhasedState state) {
      if (state != null && state.stateInfo != null) {
         ItemStack assumedStack = state.stateInfo.requiredStack;
         return assumedStack != null && !assumedStack.isEmpty() ? assumedStack.getHoverName().getString() : "";
      } else {
         return "";
      }
   }

   public static void appendTooltipLines(ItemPluggableFacade item, ItemStack stack, TooltipFlag flag, List<Component> tooltip) {
      FacadeInstance states = getStates(stack);
      if (states.type == FacadeType.Phased) {
         FacadePhasedState defaultState = null;

         for (FacadePhasedState state : states.phasedStates) {
            if (state.activeColour == null) {
               defaultState = state;
            } else {
               tooltip.add(
                  Component.translatable(
                     "item.buildcraftsilicon.plug_facade_phased.state",
                     new Object[]{Component.translatable("color.minecraft." + state.activeColour.getName()), getFacadeStateDisplayName(state)}
                  )
               );
            }
         }

         if (defaultState != null) {
            tooltip.add(
               Component.translatable("item.buildcraftsilicon.plug_facade_phased.state_default", new Object[]{getFacadeStateDisplayName(defaultState)})
            );
         }
      } else {
         if (flag.isAdvanced()) {
            tooltip.add(Component.literal(BuiltInRegistries.BLOCK.getKey(states.phasedStates[0].stateInfo.state.getBlock()).toString()));
         }

         FacadeBlockStateInfo info = states.phasedStates[0].stateInfo;
         BlockState state = info.state;
         UnmodifiableIterator var15 = info.varyingProperties.iterator();

         while (var15.hasNext()) {
            Property<?> prop = (Property<?>)var15.next();
            String name = prop.getName();
            String value = getPropertyValueName(state, prop);
            tooltip.add(Component.literal(name + " = " + value).withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}));
         }
      }
   }

   private static <T extends Comparable<T>> String getPropertyValueName(BlockState state, Property<T> prop) {
      return prop.getName(state.getValue(prop));
   }

   @Override
   public ItemStack createFacadeStack(IFacade facade) {
      return this.createItemStack((FacadeInstance)facade);
   }

   @Override
   public IFacade getFacade(ItemStack facade) {
      return getStates(facade);
   }
}
