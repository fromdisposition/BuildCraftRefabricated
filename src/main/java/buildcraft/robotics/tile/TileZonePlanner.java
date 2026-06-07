package buildcraft.robotics.tile;

import buildcraft.core.BCCore;
import buildcraft.core.item.ItemMapLocation;
import buildcraft.core.item.ItemPaintbrush_BC8;
import buildcraft.lib.fabric.menu.BlockEntityExtendedMenu;
import buildcraft.lib.tile.BcBlockEntity;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.robotics.BCRoboticsBlockEntities;
import buildcraft.robotics.container.ContainerZonePlanner;
import buildcraft.robotics.zone.ZonePlan;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileZonePlanner extends BcBlockEntity implements MenuProvider, BlockEntityExtendedMenu {
   public final ItemHandlerSimple invPaintbrushes = new ItemHandlerSimple(16, null);
   public final ItemHandlerSimple invInputPaintbrush = new ItemHandlerSimple(1, null);
   public final ItemHandlerSimple invInputMapLocation = new ItemHandlerSimple(1, null);
   public final ItemHandlerSimple invInputResult = new ItemHandlerSimple(1, null);
   public final ItemHandlerSimple invOutputPaintbrush = new ItemHandlerSimple(1, null);
   public final ItemHandlerSimple invOutputMapLocation = new ItemHandlerSimple(1, null);
   public final ItemHandlerSimple invOutputResult = new ItemHandlerSimple(1, null);
   private static final int PROGRESS_TARGET = 200;
   private static final String TAG_MAP_TYPE = "mapType";
   private static final String MAP_TYPE_CLEAN = "CLEAN";
   private static final String MAP_TYPE_ZONE = "ZONE";
   private int progressInput = -1;
   private int progressOutput = -1;
   public ZonePlan[] layers = new ZonePlan[16];

   public TileZonePlanner(BlockPos pos, BlockState state) {
      super(BCRoboticsBlockEntities.ZONE_PLANNER, pos, state);

      for (int i = 0; i < this.layers.length; i++) {
         this.layers[i] = new ZonePlan();
      }
   }

   public void serverTick() {
      if (this.level != null && !this.level.isClientSide()) {
         this.tickInput();
         this.tickOutput();
      }
   }

   private void tickInput() {
      ItemStack brush = this.invInputPaintbrush.getStackInSlot(0);
      ItemStack map = this.invInputMapLocation.getStackInSlot(0);
      boolean canRun = isPaintbrush(brush) && isZoneMap(map) && this.invInputResult.getStackInSlot(0).isEmpty();
      if (!canRun) {
         if (this.progressInput != -1) {
            this.progressInput = -1;
            this.setChanged();
         }
      } else {
         if (this.progressInput < 0) {
            this.progressInput = 0;
         }

         this.progressInput++;
         if (this.progressInput >= 200) {
            this.progressInput = -1;
            int layer = layerFor(brush);
            if (layer >= 0) {
               ZonePlan plan = new ZonePlan();
               readZoneFromMap(map, plan);
               this.layers[layer] = plan;
            }

            this.invInputResult.setStackInSlot(0, map.copy());
            this.invInputMapLocation.setStackInSlot(0, ItemStack.EMPTY);
         }

         this.setChanged();
      }
   }

   private void tickOutput() {
      ItemStack brush = this.invOutputPaintbrush.getStackInSlot(0);
      ItemStack map = this.invOutputMapLocation.getStackInSlot(0);
      boolean canRun = isPaintbrush(brush) && isCleanMap(map) && this.invOutputResult.getStackInSlot(0).isEmpty();
      if (!canRun) {
         if (this.progressOutput != -1) {
            this.progressOutput = -1;
            this.setChanged();
         }
      } else {
         if (this.progressOutput < 0) {
            this.progressOutput = 0;
         }

         this.progressOutput++;
         if (this.progressOutput >= 200) {
            this.progressOutput = -1;
            int layer = layerFor(brush);
            ItemStack result = map.copy();
            result.setCount(1);
            if (layer >= 0) {
               writeZoneToMap(result, this.layers[layer]);
            }

            this.invOutputResult.setStackInSlot(0, result);
            map.shrink(1);
            this.invOutputMapLocation.setStackInSlot(0, map.isEmpty() ? ItemStack.EMPTY : map);
         }

         this.setChanged();
      }
   }

   public int getProgressInput() {
      return this.progressInput;
   }

   public int getProgressOutput() {
      return this.progressOutput;
   }

   public void setProgressInput(int progress) {
      this.progressInput = progress;
   }

   public void setProgressOutput(int progress) {
      this.progressOutput = progress;
   }

   public void applyPaint(int layer, int x, int z, boolean set) {
      if (layer >= 0 && layer < this.layers.length) {
         if (this.layers[layer] == null) {
            this.layers[layer] = new ZonePlan();
         }

         this.layers[layer].set(x, z, set);
         this.setChanged();
      }
   }

   private static boolean isPaintbrush(ItemStack stack) {
      return !stack.isEmpty() && stack.getItem() instanceof ItemPaintbrush_BC8;
   }

   private static int layerFor(ItemStack brush) {
      DyeColor colour = (DyeColor)brush.get(BCCore.BRUSH_COLOR);
      return colour == null ? -1 : colour.getId();
   }

   private static String mapTypeOf(ItemStack stack) {
      CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
      return data == null ? "CLEAN" : data.copyTag().getString("mapType").orElse("CLEAN");
   }

   private static boolean isZoneMap(ItemStack stack) {
      return stack.getItem() instanceof ItemMapLocation && "ZONE".equals(mapTypeOf(stack));
   }

   private static boolean isCleanMap(ItemStack stack) {
      if (!(stack.getItem() instanceof ItemMapLocation)) {
         return false;
      }

      String type = mapTypeOf(stack);
      return type.isEmpty() || "CLEAN".equals(type);
   }

   private static void readZoneFromMap(ItemStack map, ZonePlan plan) {
      CustomData data = (CustomData)map.get(DataComponents.CUSTOM_DATA);
      if (data != null) {
         plan.readFromNBT(data.copyTag());
      }
   }

   private static void writeZoneToMap(ItemStack map, ZonePlan plan) {
      CustomData existing = (CustomData)map.get(DataComponents.CUSTOM_DATA);
      CompoundTag tag = existing == null ? new CompoundTag() : existing.copyTag();
      plan.writeToNBT(tag);
      tag.putString("mapType", "ZONE");
      map.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      map.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(4.0F), List.of(), List.of(), List.of()));
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);

      for (int i = 0; i < this.layers.length; i++) {
         CompoundTag layerTag = new CompoundTag();
         this.layers[i].writeToNBT(layerTag);
         output.store("layer_" + i, CompoundTag.CODEC, layerTag);
      }

      output.store("invPaintbrushes", CompoundTag.CODEC, this.invPaintbrushes.serializeNBT());
      output.store("invInputPaintbrush", CompoundTag.CODEC, this.invInputPaintbrush.serializeNBT());
      output.store("invInputMapLocation", CompoundTag.CODEC, this.invInputMapLocation.serializeNBT());
      output.store("invInputResult", CompoundTag.CODEC, this.invInputResult.serializeNBT());
      output.store("invOutputPaintbrush", CompoundTag.CODEC, this.invOutputPaintbrush.serializeNBT());
      output.store("invOutputMapLocation", CompoundTag.CODEC, this.invOutputMapLocation.serializeNBT());
      output.store("invOutputResult", CompoundTag.CODEC, this.invOutputResult.serializeNBT());
      output.putInt("progressInput", this.progressInput);
      output.putInt("progressOutput", this.progressOutput);
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);

      for (int i = 0; i < this.layers.length; i++) {
         int idx = i;
         input.read("layer_" + i, CompoundTag.CODEC).ifPresent(tag -> {
            this.layers[idx] = new ZonePlan();
            this.layers[idx].readFromNBT(tag);
         });
      }

      input.read("invPaintbrushes", CompoundTag.CODEC).ifPresent(this.invPaintbrushes::deserializeNBT);
      input.read("invInputPaintbrush", CompoundTag.CODEC).ifPresent(this.invInputPaintbrush::deserializeNBT);
      input.read("invInputMapLocation", CompoundTag.CODEC).ifPresent(this.invInputMapLocation::deserializeNBT);
      input.read("invInputResult", CompoundTag.CODEC).ifPresent(this.invInputResult::deserializeNBT);
      input.read("invOutputPaintbrush", CompoundTag.CODEC).ifPresent(this.invOutputPaintbrush::deserializeNBT);
      input.read("invOutputMapLocation", CompoundTag.CODEC).ifPresent(this.invOutputMapLocation::deserializeNBT);
      input.read("invOutputResult", CompoundTag.CODEC).ifPresent(this.invOutputResult::deserializeNBT);
      this.progressInput = input.getIntOr("progressInput", -1);
      this.progressOutput = input.getIntOr("progressOutput", -1);
   }

   public Component getDisplayName() {
      return Component.translatable("block.buildcraftrobotics.zone_planner");
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
      return new ContainerZonePlanner(containerId, playerInv, this);
   }

   public void getDebugInfo(List<String> left, List<String> right, Direction side) {
      left.add("progress_input = " + this.progressInput);
      left.add("progress_output = " + this.progressOutput);
   }
}
