package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.lib.misc.AdvancementUtil;
import buildcraft.lib.misc.InventoryUtil;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.silicon.BCSiliconBlockEntities;
import buildcraft.silicon.EnumAssemblyRecipeState;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileAssemblyTable extends TileLaserTableBase {
   private static final Identifier ADVANCEMENT = Identifier.parse("buildcraftsilicon:precision_crafting");
   private static final int RESCAN_INTERVAL = 100;
   public final ItemHandlerSimple inv = this.itemManager.addInvHandler("inv", 12, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
   public SortedMap<TileAssemblyTable.AssemblyInstruction, EnumAssemblyRecipeState> recipesStates = new TreeMap<>();
   private long lastInvSignature = Long.MIN_VALUE;
   private int rescanCooldown;

   public TileAssemblyTable(BlockPos pos, BlockState state) {
      super(BCSiliconBlockEntities.ASSEMBLY_TABLE, pos, state);
   }

   private long inventorySignature() {
      long sig = 1L;

      for (int i = 0; i < this.inv.getSlots(); i++) {
         ItemStack stack = this.inv.getStackInSlot(i);
         sig = sig * 31L + (stack.isEmpty() ? 0L : ItemStack.hashItemAndComponents(stack) * 31L + stack.getCount());
      }

      return sig;
   }

   private void updateRecipes() {
      for (AssemblyRecipe recipe : AssemblyRecipeRegistry.REGISTRY.values()) {
         for (ItemStack out : recipe.getOutputs(this.inv.stacks)) {
            TileAssemblyTable.AssemblyInstruction instruction = new TileAssemblyTable.AssemblyInstruction(recipe, out);
            if (!this.recipesStates.containsKey(instruction)) {
               this.recipesStates.put(instruction, EnumAssemblyRecipeState.POSSIBLE);
            }
         }
      }

      boolean findActive = false;
      Iterator<Entry<TileAssemblyTable.AssemblyInstruction, EnumAssemblyRecipeState>> iterator = this.recipesStates.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<TileAssemblyTable.AssemblyInstruction, EnumAssemblyRecipeState> entry = iterator.next();
         TileAssemblyTable.AssemblyInstruction instruction = entry.getKey();
         EnumAssemblyRecipeState state = entry.getValue();
         boolean enough = this.extract(this.inv, instruction.recipe.getInputsFor(instruction.output), true, false);
         if (state == EnumAssemblyRecipeState.POSSIBLE) {
            if (!enough) {
               iterator.remove();
            }
         } else if (state != EnumAssemblyRecipeState.PAUSED) {
            if (enough) {
               if (state == EnumAssemblyRecipeState.SAVED) {
                  state = EnumAssemblyRecipeState.SAVED_ENOUGH;
               }
            } else if (state != EnumAssemblyRecipeState.SAVED) {
               state = EnumAssemblyRecipeState.SAVED;
            }
         }

         if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
            findActive = true;
         }

         entry.setValue(state);
      }

      if (!findActive) {
         for (Entry<TileAssemblyTable.AssemblyInstruction, EnumAssemblyRecipeState> entry : this.recipesStates.entrySet()) {
            EnumAssemblyRecipeState state = entry.getValue();
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
               entry.setValue(EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE);
               break;
            }
         }
      }
   }

   @Nullable
   private TileAssemblyTable.AssemblyInstruction getActiveRecipe() {
      return this.recipesStates
         .entrySet()
         .stream()
         .filter(entry -> entry.getValue() == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE)
         .map(Entry::getKey)
         .findFirst()
         .orElse(null);
   }

   private void activateNextRecipe() {
      TileAssemblyTable.AssemblyInstruction activeRecipe = this.getActiveRecipe();
      if (activeRecipe != null) {
         int index = 0;
         int activeIndex = 0;
         boolean isActiveLast = false;
         long enoughCount = this.recipesStates
            .values()
            .stream()
            .filter(statex -> statex == EnumAssemblyRecipeState.SAVED_ENOUGH || statex == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE)
            .count();
         if (enoughCount <= 1L) {
            return;
         }

         for (Entry<TileAssemblyTable.AssemblyInstruction, EnumAssemblyRecipeState> entry : this.recipesStates.entrySet()) {
            EnumAssemblyRecipeState state = entry.getValue();
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH) {
               isActiveLast = false;
            }

            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE) {
               entry.setValue(EnumAssemblyRecipeState.SAVED_ENOUGH);
               activeIndex = index;
               isActiveLast = true;
            }

            index++;
         }

         index = 0;

         for (Entry<TileAssemblyTable.AssemblyInstruction, EnumAssemblyRecipeState> entry : this.recipesStates.entrySet()) {
            EnumAssemblyRecipeState state = entry.getValue();
            if (state == EnumAssemblyRecipeState.SAVED_ENOUGH && entry.getKey().recipe != activeRecipe.recipe && (index > activeIndex || isActiveLast)) {
               entry.setValue(EnumAssemblyRecipeState.SAVED_ENOUGH_ACTIVE);
               break;
            }

            index++;
         }
      }
   }

   @Override
   public long getTarget() {
      return Optional.ofNullable(this.getActiveRecipe()).map(instruction -> instruction.recipe.getRequiredMicroJoulesFor(instruction.output)).orElse(0L);
   }

   @Override
   public void serverTick() {
      super.serverTick();
      long signature = this.inventorySignature();
      if (signature != this.lastInvSignature || --this.rescanCooldown <= 0) {
         this.lastInvSignature = signature;
         this.rescanCooldown = RESCAN_INTERVAL;
         int prevSize = this.recipesStates.size();
         int prevHash = this.recipesStates.hashCode();
         this.updateRecipes();
         if (this.recipesStates.size() != prevSize || this.recipesStates.hashCode() != prevHash) {
            this.setChanged();
            if (this.getLevel() != null) {
               this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            }
         }
      }

      if (this.getTarget() > 0L) {
         if (this.getOwner() != null) {
            AdvancementUtil.unlockAdvancement(this.getOwner().id(), this.getLevel(), ADVANCEMENT);
         }

         if (this.power >= this.getTarget()) {
            TileAssemblyTable.AssemblyInstruction instruction = this.getActiveRecipe();
            if (instruction != null) {
               long target = this.getTarget();
               if (this.extract(this.inv, instruction.recipe.getInputsFor(instruction.output), false, false)) {
                  InventoryUtil.addToBestAcceptor(this.getLevel(), this.getBlockPos(), null, instruction.output.copy());
                  this.power -= target;
                  this.setChanged();
                  this.activateNextRecipe();
               }
            }
         }
      }
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      CompoundTag wrapper = new CompoundTag();
      ListTag recipesStatesTag = new ListTag();
      this.recipesStates.forEach((instruction, state) -> {
         CompoundTag entryTag = new CompoundTag();
         entryTag.putString("recipe", instruction.recipe.getRegistryName());
         entryTag.putInt("state", state.ordinal());
         CompoundTag outputTag = NBTUtilBC.itemStackToNBT(instruction.output);
         CompoundTag customData = NBTUtilBC.getItemData(instruction.output);
         if (!customData.isEmpty()) {
            outputTag.put("customData", customData);
         }

         entryTag.put("output", outputTag);
         recipesStatesTag.add(entryTag);
      });
      wrapper.put("entries", recipesStatesTag);
      output.store("recipes_states", CompoundTag.CODEC, wrapper);
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.recipesStates.clear();
      input.read("recipes_states", CompoundTag.CODEC).ifPresent(wrapper -> wrapper.getList("entries").ifPresent(recipesStatesTag -> {
         for (int i = 0; i < recipesStatesTag.size(); i++) {
            recipesStatesTag.getCompound(i).ifPresent(entryTag -> entryTag.getString("recipe").ifPresent(name -> {
               AssemblyRecipe recipe = AssemblyRecipeRegistry.REGISTRY.get(name);
               if (recipe != null) {
                  int stateOrdinal = entryTag.getIntOr("state", 0);
                  EnumAssemblyRecipeState[] values = EnumAssemblyRecipeState.values();
                  if (stateOrdinal >= 0 && stateOrdinal < values.length) {
                     ItemStack outputStack = entryTag.getCompound("output").map(outputTag -> {
                        ItemStack stack = NBTUtilBC.itemStackFromNBT(outputTag);
                        outputTag.getCompound("customData").ifPresent(cd -> NBTUtilBC.setItemData(stack, cd));
                        return stack;
                     }).orElse(ItemStack.EMPTY);
                     if (outputStack.isEmpty()) {
                        Set<ItemStack> outputs = recipe.getOutputs(this.inv.stacks);
                        if (!outputs.isEmpty()) {
                           outputStack = outputs.iterator().next();
                        }
                     }

                     if (!outputStack.isEmpty()) {
                        TileAssemblyTable.AssemblyInstruction instruction = new TileAssemblyTable.AssemblyInstruction(recipe, outputStack);
                        this.recipesStates.put(instruction, values[stateOrdinal]);
                     }
                  }
               }
            }));
         }
      }));
   }

   public static class AssemblyInstruction implements Comparable<TileAssemblyTable.AssemblyInstruction> {
      public final AssemblyRecipe recipe;
      public final ItemStack output;

      public AssemblyInstruction(AssemblyRecipe recipe, ItemStack output) {
         this.recipe = recipe;
         this.output = output;
      }

      public int compareTo(TileAssemblyTable.AssemblyInstruction o) {
         int recipeCompare = this.recipe.compareTo(o.recipe);
         if (recipeCompare != 0) {
            return recipeCompare;
         }

         if (ItemStack.isSameItemSameComponents(this.output, o.output)) {
            return 0;
         }

         Identifier thisId = BuiltInRegistries.ITEM.getKey(this.output.getItem());
         Identifier otherId = BuiltInRegistries.ITEM.getKey(o.output.getItem());
         int idCompare = thisId.compareTo(otherId);
         return idCompare != 0 ? idCompare : this.output.getComponents().toString().compareTo(o.output.getComponents().toString());
      }

      @Override
      public boolean equals(Object obj) {
         return !(obj instanceof TileAssemblyTable.AssemblyInstruction instruction)
            ? false
            : this.recipe.equals(instruction.recipe) && ItemStack.isSameItemSameComponents(this.output, instruction.output);
      }

      @Override
      public int hashCode() {
         return this.recipe.hashCode() * 31 + ItemStack.hashItemAndComponents(this.output);
      }
   }
}
