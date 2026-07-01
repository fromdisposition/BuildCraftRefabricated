/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.lib.nbt.BcNbt;
import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.facades.FacadeAPI;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.facades.IFacadeRegistry;
import buildcraft.api.facades.IFacadeState;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.net.BcPayloadBuffers;
import buildcraft.lib.net.PacketBufferBC;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public enum FacadeStateManager implements IFacadeRegistry {
   INSTANCE;

   public static final boolean DEBUG = BCDebugging.shouldDebugLog("silicon.facade");
   public static volatile SortedMap<BlockState, FacadeBlockStateInfo> validFacadeStates = Collections.unmodifiableSortedMap(
      new TreeMap<>(blockStateComparator())
   );
   public static volatile Map<ItemStackKey, List<FacadeBlockStateInfo>> stackFacades = Map.of();
   public static volatile Map<ItemStackKey, List<FacadeBlockStateInfo>> stackRedirects = Map.of();
   public static FacadeBlockStateInfo defaultState;
   public static FacadeBlockStateInfo previewState;
   private static volatile boolean initialized = false;
   private static final Map<Block, String> disabledBlocks = new HashMap<>();
   private static final Map<BlockState, ItemStack> customBlocks = new HashMap<>();

   public static boolean isInitialized() {
      return initialized;
   }

   public static void ensureInitialized() {
      if (!initialized) {
         init();
      }
   }

   @Override
   public void disableBlock(Block block, String source) {
      disabledBlocks.put(block, source);
      if (initialized) {
         SortedMap<BlockState, FacadeBlockStateInfo> nextValid = new TreeMap<>(validFacadeStates);
         nextValid.keySet().removeIf(state -> state.is(block));
         validFacadeStates = Collections.unmodifiableSortedMap(nextValid);
      }
   }

   @Override
   public void mapStateToStack(BlockState state, ItemStack stack) {
      customBlocks.put(state, stack.copy());
      if (initialized) {
         SortedMap<BlockState, FacadeBlockStateInfo> nextValid = new TreeMap<>(validFacadeStates);
         Map<ItemStackKey, List<FacadeBlockStateInfo>> nextStackFacades = new HashMap<>();

         for (Entry<ItemStackKey, List<FacadeBlockStateInfo>> entry : stackFacades.entrySet()) {
            List<FacadeBlockStateInfo> filtered = entry.getValue().stream().filter(info -> !info.state.is(state.getBlock())).toList();
            if (!filtered.isEmpty()) {
               nextStackFacades.put(entry.getKey(), new ArrayList<>(filtered));
            }
         }

         nextValid.keySet().removeIf(s -> s.is(state.getBlock()));
         scanBlock(state.getBlock(), nextValid, nextStackFacades);
         validFacadeStates = Collections.unmodifiableSortedMap(nextValid);
         Map<ItemStackKey, List<FacadeBlockStateInfo>> published = new HashMap<>();

         for (Entry<ItemStackKey, List<FacadeBlockStateInfo>> entry : nextStackFacades.entrySet()) {
            published.put(entry.getKey(), List.copyOf(entry.getValue()));
         }

         stackFacades = Map.copyOf(published);
      }
   }

   private static Comparator<BlockState> blockStateComparator() {
      return Comparator.comparingInt(state -> Block.BLOCK_STATE_REGISTRY.getId(state));
   }

   public static FacadeBlockStateInfo getInfoForBlock(Block block) {
      return getInfoForState(block.defaultBlockState());
   }

   private static FacadeBlockStateInfo getInfoForState(BlockState state) {
      return validFacadeStates.get(state);
   }

   private static String isValidFacadeBlock(Block block) {
      String disablingMod = disabledBlocks.get(block);
      if (disablingMod != null) {
         return "it has been disabled by " + disablingMod;
      } else if (block instanceof LiquidBlock) {
         return "it is a fluid block";
      } else {
         return !(block instanceof TransparentBlock) && !(block instanceof HalfTransparentBlock) ? "pass" : "ok";
      }
   }

   private static String isValidFacadeState(BlockState state) {
      if (state.hasBlockEntity()) {
         return "it has a block entity";
      } else if (state.getRenderShape() != RenderShape.MODEL) {
         return "it doesn't have a normal model";
      } else {
         return !state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) ? "it isn't a full cube" : "ok";
      }
   }

   private static ItemStack getRequiredStack(BlockState state) {
      ItemStack custom = customBlocks.get(state);
      if (custom != null) {
         return custom;
      }

      Block block = state.getBlock();
      Item item = block.asItem();
      return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
   }

   public static void init() {
      if (!initialized) {
         defaultState = new FacadeBlockStateInfo(Blocks.AIR.defaultBlockState(), ItemStack.EMPTY, ImmutableSet.of());
         if (FacadeAPI.facadeItem == null) {
            previewState = defaultState;
         } else {
            SortedMap<BlockState, FacadeBlockStateInfo> nextValid = new TreeMap<>(blockStateComparator());
            Map<ItemStackKey, List<FacadeBlockStateInfo>> nextStackFacades = new HashMap<>();

            for (Block block : BuiltInRegistries.BLOCK) {
               scanBlock(block, nextValid, nextStackFacades);
            }

            previewState = nextValid.get(Blocks.BRICKS.defaultBlockState());
            if (previewState == null) {
               previewState = defaultState;
            }

            Map<ItemStackKey, List<FacadeBlockStateInfo>> publishedStackFacades = new HashMap<>(nextStackFacades.size());

            for (Entry<ItemStackKey, List<FacadeBlockStateInfo>> e : nextStackFacades.entrySet()) {
               publishedStackFacades.put(e.getKey(), List.copyOf(e.getValue()));
            }

            validFacadeStates = Collections.unmodifiableSortedMap(nextValid);
            stackFacades = Map.copyOf(publishedStackFacades);
            initialized = true;
            BCLog.logger.info("[silicon.facade] Total valid facade states: " + validFacadeStates.size());
         }
      }
   }

   private static void scanBlock(
      Block block, SortedMap<BlockState, FacadeBlockStateInfo> outValidStates, Map<ItemStackKey, List<FacadeBlockStateInfo>> outStackFacades
   ) {
      try {
         String blockResult = isValidFacadeBlock(block);
         if (!"ok".equals(blockResult) && !"pass".equals(blockResult)) {
            if (DEBUG) {
               BCLog.logger.info("[silicon.facade] Disallowed block " + BuiltInRegistries.BLOCK.getKey(block) + " because " + blockResult);
            }

            return;
         }

         if (DEBUG && "ok".equals(blockResult)) {
            BCLog.logger.info("[silicon.facade] Allowed block " + BuiltInRegistries.BLOCK.getKey(block));
         }

         Map<BlockState, ItemStack> usedStates = new HashMap<>();
         Map<ItemStackKey, Map<Property<?>, Comparable<?>>> varyingProperties = new HashMap<>();
         UnmodifiableIterator testingBuffer = block.getStateDefinition().getPossibleStates().iterator();

         while (testingBuffer.hasNext()) {
            BlockState state = (BlockState)testingBuffer.next();
            if (!"ok".equals(blockResult)) {
               String stateResult = isValidFacadeState(state);
               if (!"ok".equals(stateResult)) {
                  if (DEBUG) {
                     BCLog.logger.info("[silicon.facade] Disallowed state " + state + " because " + stateResult);
                  }
                  continue;
               }

               if (DEBUG) {
                  BCLog.logger.info("[silicon.facade] Allowed state " + state);
               }
            }

            ItemStack requiredStack;
            try {
               requiredStack = getRequiredStack(state);
            } catch (RuntimeException e) {
               BCLog.logger.warn("[silicon.facade] Disallowed state " + state + " after getRequiredStack(state) threw an exception!", e);
               continue;
            }

            if (!requiredStack.isEmpty()) {
               usedStates.put(state, requiredStack);
               ItemStackKey stackKey = new ItemStackKey(requiredStack);
               Map<Property<?>, Comparable<?>> vars = varyingProperties.get(stackKey);
               if (vars == null) {
                  Map<Property<?>, Comparable<?>> newVars = new HashMap<>();
                  //? if >= 26.1 {
                  state.getValues().forEach(pv -> newVars.put(pv.property(), pv.value()));
                  //?} else {
                  /*state.getValues().forEach(newVars::put);
                  *///?}
                  varyingProperties.put(stackKey, newVars);
               } else {
                  Map<Property<?>, Comparable<?>> finalVars = vars;
                  //? if >= 26.1 {
                  state.getValues().forEach(pv -> {
                     Property<?> prop = pv.property();
                     Comparable<?> value = pv.value();
                     if (finalVars.get(prop) != value) {
                        finalVars.put(prop, null);
                     }
                  });
                  //?} else {
                  /*state.getValues().forEach((prop, value) -> {
                     if (finalVars.get(prop) != value) {
                        finalVars.put(prop, null);
                     }
                  });
                  *///?}
               }
            }
         }

         varyingProperties.forEach((key, varsx) -> varsx.values().removeIf(Objects::nonNull));
         PacketBufferBC testingBufferx = BcPayloadBuffers.create();

         for (Entry<BlockState, ItemStack> entry : usedStates.entrySet()) {
            BlockState state = entry.getKey();
            ItemStack stack = entry.getValue();
            Map<Property<?>, Comparable<?>> vars = varyingProperties.get(new ItemStackKey(stack));

            try {
               ImmutableSet<Property<?>> varSet = ImmutableSet.copyOf(vars.keySet());
               FacadeBlockStateInfo info = new FacadeBlockStateInfo(state, stack, varSet);
               outValidStates.put(state, info);
               if (!info.requiredStack.isEmpty()) {
                  ItemStackKey stackKey = new ItemStackKey(info.requiredStack);
                  outStackFacades.computeIfAbsent(stackKey, k -> new ArrayList<>()).add(info);
               }

               FacadePhasedState phasedState = info.createPhased(null);
               CompoundTag nbt = phasedState.writeToNbt();
               BlockState nbtReadState = NbtUtils.readBlockState(buildcraft.lib.fabric.BcRegistryUtil.blockLookup(), BcNbt.getCompound(nbt, "state"));
               if (nbtReadState != info.state) {
                  throw new IllegalStateException(
                     "Read (from NBT) state was different! (\n\t" + nbtReadState + "\n !=\n\t" + info.state + "\n\tNBT = " + nbt + "\n)"
                  );
               }

               phasedState.writeToBuffer(testingBufferx);
               BlockState bufReadState = (BlockState)Block.BLOCK_STATE_REGISTRY.byId(testingBufferx.readVarInt());
               if (bufReadState != info.state) {
                  throw new IllegalStateException("Read (from buffer) state was different! (\n\t" + bufReadState + "\n !=\n\t" + info.state + "\n)");
               }

               testingBufferx.clear();
               if (DEBUG) {
                  BCLog.logger.info("[silicon.facade]   Added " + info);
               }
            } catch (Throwable t) {
               String msg = "Scanning facade states";
               msg = msg + "\n\tState = " + state;
               msg = msg + "\n\tBlock = " + BuiltInRegistries.BLOCK.getKey(state.getBlock());
               msg = msg + "\n\tStack = " + stack;
               msg = msg + "\n\tvarying-properties: {";

               for (Entry<Property<?>, Comparable<?>> varEntry : vars.entrySet()) {
                  msg = msg + "\n\t\t" + varEntry.getKey() + " = " + varEntry.getValue();
               }

               msg = msg + "\n\t}";
               BCLog.logger.error("[silicon.facade] " + msg.replace("\t", "    "), t);
            }
         }

         testingBufferx.release();
      } catch (RuntimeException e) {
         BCLog.logger.warn("[silicon.facade] Skipping " + block + " as something about it threw an exception! ", e);
      }
   }

   @Override
   public Collection<? extends IFacadeState> getValidFacades() {
      return validFacadeStates.values();
   }

   @Override
   public IFacadePhasedState createPhasedState(IFacadeState state, DyeColor activeColor) {
      return new FacadePhasedState((FacadeBlockStateInfo)state, activeColor);
   }

   @Override
   public IFacade createPhasedFacade(IFacadePhasedState[] states, boolean isHollow) {
      FacadePhasedState[] realStates = new FacadePhasedState[states.length];

      for (int i = 0; i < states.length; i++) {
         realStates[i] = (FacadePhasedState)states[i];
      }

      return new FacadeInstance(realStates, isHollow);
   }
}
