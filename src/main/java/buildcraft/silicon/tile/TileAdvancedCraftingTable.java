package buildcraft.silicon.tile;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.tile.ItemHandlerManager;
import buildcraft.lib.tile.ItemHandlerSimple;
import buildcraft.lib.tile.craft.WorkbenchCrafting;
import buildcraft.silicon.BCSiliconBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileAdvancedCraftingTable extends TileLaserTableBase {
   private static final long POWER_REQ = 500L * MjAPI.MJ;
   public final ItemHandlerSimple invBlueprint;
   public final ItemHandlerSimple invMaterials;
   public final ItemHandlerSimple invResults;
   private final WorkbenchCrafting crafting;
   public ItemStack resultClient = ItemStack.EMPTY;

   public TileAdvancedCraftingTable(BlockPos pos, BlockState state) {
      super(BCSiliconBlockEntities.ADVANCED_CRAFTING_TABLE, pos, state);
      this.invBlueprint = this.itemManager.addInvHandler("blueprint", 9, ItemHandlerManager.EnumAccess.PHANTOM);
      this.invMaterials = this.itemManager.addInvHandler("materials", 15, ItemHandlerManager.EnumAccess.INSERT, EnumPipePart.VALUES);
      this.invResults = this.itemManager.addInvHandler("result", 9, ItemHandlerManager.EnumAccess.EXTRACT, EnumPipePart.VALUES);
      this.crafting = new WorkbenchCrafting(3, 3, this, this.invBlueprint, this.invMaterials, this.invResults);
      this.invBlueprint.setCallback((handler, slot, before, after) -> {
         this.setChanged();
         this.crafting.onInventoryChange(this.invBlueprint);
      });
      this.invMaterials.setCallback((handler, slot, before, after) -> {
         this.setChanged();
         this.crafting.onInventoryChange(this.invMaterials);
      });
   }

   @Override
   public long getTarget() {
      if (this.level == null) {
         return 0L;
      } else {
         return !this.resultClient.isEmpty() ? POWER_REQ : 0L;
      }
   }

   @Override
   public void serverTick() {
      super.serverTick();
      ItemStack prevResult = this.resultClient;
      boolean didChange = this.crafting.tick();
      if (didChange) {
         this.resultClient = this.crafting.getAssumedResult().copy();
      }

      if (this.crafting.canCraft() && this.power >= POWER_REQ && this.crafting.craft()) {
         this.power = this.power - POWER_REQ;
      }

      if (!ItemStack.matches(prevResult, this.resultClient)) {
         this.setChanged();
         if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
         }
      }
   }

   public ItemStack getCurrentRecipeOutput() {
      return this.crafting.getAssumedResult();
   }

   public ItemHandlerSimple getInvBlueprint() {
      return this.invBlueprint;
   }

   @Override
   protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      if (!this.resultClient.isEmpty()) {
         output.store("resultClient", ItemStack.CODEC, this.resultClient);
      }
   }

   @Override
   public void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.resultClient = input.read("resultClient", ItemStack.CODEC).orElse(ItemStack.EMPTY);
   }

   @Override
   public CompoundTag getUpdateTag(Provider registries) {
      return this.saveCustomOnly(registries);
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }
}
