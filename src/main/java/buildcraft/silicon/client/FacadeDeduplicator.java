/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.misc.ItemStackKey;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeStateManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class FacadeDeduplicator {
   private static final boolean DEBUG = BCDebugging.shouldDebugLog("silicon.facade");
   private static final RandomSource RANDOM = RandomSource.create(42L);
   private static volatile Map<ItemStackKey, List<FacadeBlockStateInfo>> computedRedirects = Map.of();

   public static void deduplicateVisuallyIdentical(Map<BlockState, BlockStateModel> blockStateModels) {
      SortedMap<BlockState, FacadeBlockStateInfo> currentValid = FacadeStateManager.validFacadeStates;
      Map<ItemStackKey, List<FacadeBlockStateInfo>> currentStackFacades = FacadeStateManager.stackFacades;
      BCLog.logger.info("[silicon.facade] Starting visual deduplication of " + currentValid.size() + " facade states...");
      if (!currentValid.isEmpty()) {
         Map<String, FacadeBlockStateInfo> seen = new HashMap<>();
         Map<BlockState, FacadeBlockStateInfo> toRemove = new HashMap<>();
         int dupCount = 0;
         int nullFingerprints = 0;

         for (Entry<BlockState, FacadeBlockStateInfo> entry : currentValid.entrySet()) {
            FacadeBlockStateInfo info = entry.getValue();
            if (info.isVisible) {
               BlockStateModel model = blockStateModels.get(info.state);
               if (model == null) {
                  nullFingerprints++;
               } else {
                  String fingerprint = computeTextureFingerprint(model);
                  if (fingerprint == null) {
                     nullFingerprints++;
                  } else {
                     FacadeBlockStateInfo existing = seen.get(fingerprint);
                     if (existing != null) {
                        toRemove.put(entry.getKey(), existing);
                        dupCount++;
                        if (DEBUG) {
                           BCLog.logger.info("[silicon.facade] Dedup: " + info.state + " is visually identical to " + existing.state);
                        }
                     } else {
                        seen.put(fingerprint, info);
                     }
                  }
               }
            }
         }

         BCLog.logger
            .info("[silicon.facade] Dedup scan complete: " + seen.size() + " unique, " + dupCount + " duplicates, " + nullFingerprints + " null fingerprints");
         Comparator<? super BlockState> validComparator = currentValid.comparator();
         SortedMap<BlockState, FacadeBlockStateInfo> nextValid = validComparator != null ? new TreeMap<>(validComparator) : new TreeMap<>();
         nextValid.putAll(currentValid);
         Map<ItemStackKey, List<FacadeBlockStateInfo>> nextStackFacades = new HashMap<>(currentStackFacades.size());

         for (Entry<ItemStackKey, List<FacadeBlockStateInfo>> e : currentStackFacades.entrySet()) {
            nextStackFacades.put(e.getKey(), new ArrayList<>(e.getValue()));
         }

         Map<ItemStackKey, List<FacadeBlockStateInfo>> nextStackRedirects = new HashMap<>();
         int redirectCount = 0;

         for (Entry<BlockState, FacadeBlockStateInfo> removal : toRemove.entrySet()) {
            BlockState state = removal.getKey();
            FacadeBlockStateInfo surviving = removal.getValue();
            FacadeBlockStateInfo removed = nextValid.remove(state);
            if (removed != null && !removed.requiredStack.isEmpty()) {
               ItemStackKey stackKey = new ItemStackKey(removed.requiredStack);
               List<FacadeBlockStateInfo> list = nextStackFacades.get(stackKey);
               if (list != null) {
                  list.remove(removed);
                  if (list.isEmpty()) {
                     nextStackFacades.remove(stackKey);
                  }
               }

               nextStackRedirects.computeIfAbsent(stackKey, k -> new ArrayList<>()).add(surviving);
               redirectCount++;
            }
         }

         if (dupCount > 0) {
            BCLog.logger
               .info(
                  "[silicon.facade] Removed "
                     + dupCount
                     + " visually identical facade duplicates. Remaining: "
                     + nextValid.size()
                     + " ("
                     + redirectCount
                     + " recipe redirects registered)"
               );
         } else {
            BCLog.logger.info("[silicon.facade] No visual duplicates found.");
         }

         int extraRedirects = 0;

         for (Entry<BlockState, BlockStateModel> modelEntry : blockStateModels.entrySet()) {
            BlockState state = modelEntry.getKey();
            if (!nextValid.containsKey(state)) {
               Item blockItem = state.getBlock().asItem();
               if (blockItem != Items.AIR && state == state.getBlock().defaultBlockState()) {
                  ItemStack requiredStack = new ItemStack(blockItem);
                  ItemStackKey stackKey = new ItemStackKey(requiredStack);
                  if (!nextStackFacades.containsKey(stackKey) && !nextStackRedirects.containsKey(stackKey)) {
                     BlockStateModel model = modelEntry.getValue();
                     if (model != null) {
                        String fingerprint = computeTextureFingerprint(model);
                        if (fingerprint != null) {
                           FacadeBlockStateInfo match = seen.get(fingerprint);
                           if (match != null) {
                              nextStackRedirects.computeIfAbsent(stackKey, k -> new ArrayList<>()).add(match);
                              extraRedirects++;
                              if (DEBUG) {
                                 BCLog.logger
                                    .info("[silicon.facade] Extra redirect: " + BuiltInRegistries.BLOCK.getKey(state.getBlock()) + " → " + match.state);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (extraRedirects > 0) {
            BCLog.logger
               .info(
                  "[silicon.facade] Added "
                     + extraRedirects
                     + " extra recipe redirects from non-facade blocks (total redirects: "
                     + nextStackRedirects.size()
                     + ")"
               );
         }

         Map<ItemStackKey, List<FacadeBlockStateInfo>> publishedStackFacades = new HashMap<>(nextStackFacades.size());

         for (Entry<ItemStackKey, List<FacadeBlockStateInfo>> e : nextStackFacades.entrySet()) {
            publishedStackFacades.put(e.getKey(), List.copyOf(e.getValue()));
         }

         Map<ItemStackKey, List<FacadeBlockStateInfo>> frozenRedirects = new HashMap<>(nextStackRedirects.size());

         for (Entry<ItemStackKey, List<FacadeBlockStateInfo>> e : nextStackRedirects.entrySet()) {
            frozenRedirects.put(e.getKey(), List.copyOf(e.getValue()));
         }

         FacadeStateManager.validFacadeStates = Collections.unmodifiableSortedMap(nextValid);
         FacadeStateManager.stackFacades = Map.copyOf(publishedStackFacades);
         computedRedirects = Map.copyOf(frozenRedirects);
         applyRedirectAuthority();
      }
   }

   public static void applyRedirectAuthority() {
      Minecraft mc = Minecraft.getInstance();
      boolean authoritative = mc != null && mc.hasSingleplayerServer();
      if (authoritative) {
         FacadeStateManager.stackRedirects = computedRedirects;
         if (DEBUG) {
            BCLog.logger.info("[silicon.facade] Integrated server present — published " + computedRedirects.size() + " facade recipe redirects.");
         }
      } else {
         FacadeStateManager.stackRedirects = Map.of();
         if (DEBUG) {
            BCLog.logger
               .info("[silicon.facade] No integrated server (dedicated/LAN-guest) — facade recipe redirects withheld; the server owns its own (empty) table.");
         }
      }
   }

   private static String computeTextureFingerprint(BlockStateModel model) {
      try {
         Set<String> textures = new LinkedHashSet<>();

         for (Direction dir : Direction.values()) {
            for (BakedQuad quad : getQuadsFromModel(model, dir)) {
               textures.add(dir.name() + ":" + quad.materialInfo().sprite().contents().name().toString());
            }
         }

         for (BakedQuad quad : getQuadsFromModel(model, null)) {
            textures.add("GENERAL:" + quad.materialInfo().sprite().contents().name().toString());
         }

         if (textures.isEmpty()) {
            return null;
         }

         List<String> sorted = new ArrayList<>(textures);
         sorted.sort(String::compareTo);
         return String.join("|", sorted);
      } catch (Exception e) {
         if (DEBUG) {
            BCLog.logger.warn("[silicon.facade] Failed to compute fingerprint for model", e);
         }

         return null;
      }
   }

   private static List<BakedQuad> getQuadsFromModel(BlockStateModel model, Direction side) {
      List<BlockStateModelPart> parts = new ArrayList<>();
      model.collectParts(RANDOM, parts);
      List<BakedQuad> result = new ArrayList<>();

      for (BlockStateModelPart part : parts) {
         if (part instanceof SimpleModelWrapper smw) {
            QuadCollection qc = smw.quads();
            result.addAll(qc.getQuads(side));
         }
      }

      return result;
   }
}
