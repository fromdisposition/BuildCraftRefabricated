/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import buildcraft.api.core.BCLog;

import buildcraft.lib.expression.info.ContextInfo;
import buildcraft.lib.expression.info.VariableInfo;

public class AdvModelCache {
    private static final int MODEL_INDEX_INCORRECT = -1;
    private static final int MODEL_INDEX_NO_CACHE = -2;

    public final ModelHolderVariable model;
    public final ContextInfo modelCtxInfo;

    final List<VariableInfo<?>> variables = new ArrayList<>();
    private @Nullable CacheBase cache = null;

    public AdvModelCache(ModelHolderVariable model, ContextInfo modelCtxInfo) {
        this.model = model;
        this.modelCtxInfo = modelCtxInfo;
    }

    public void clear() {
        CacheBase base = cache;
        if (base != null) {
            base.clear();
        }
    }

    public void reset() {
        clear();
        variables.clear();
        cache = null;
    }

    public MutableQuad[] getCutoutQuads() {
        return getCurrentValue().cutout;
    }

    public MutableQuad[] getTranslucentQuads() {
        return getCurrentValue().translucent;
    }

    CacheValue computeFullModel() {
        return new CacheValue(model.getCutoutQuads(), model.getTranslucentQuads());
    }

    CacheValue getCurrentValue() {
        CacheBase c = cache;
        if (c == null) {
            c = cache = createNewCache();
        }
        return c.getCurrentValue();
    }

    CacheBase createNewCache() {
        variables.clear();
        variables.addAll(modelCtxInfo.variables.values());

        int[] multipliers = new int[variables.size()];
        List<VariableInfo<?>> missKeys = new ArrayList<>();
        int m = 1;
        for (int i = 0; i < variables.size(); i++) {
            multipliers[i] = m;
            VariableInfo<?> info = variables.get(i);
            m *= info.getPossibleValues().size();
            if (!info.setIsComplete) {
                missKeys.add(info);
            }
        }
        CacheIndexed indexedCache = new CacheIndexed(multipliers, m);

        if (!missKeys.isEmpty()) {
            BCLog.logger.warn(
                "[lib.model.adv_cache] Creating an indexed cache despite knowing that there will be cache misses!");
            for (VariableInfo<?> info : missKeys) {
                BCLog.logger.warn("[lib.model.adv_cache]  - " + info.node + " (" + info.cacheType + ", "
                    + info.getPossibleValues() + ")");
            }
        }

        return indexedCache;
    }

    abstract class CacheBase {
        abstract CacheValue getCurrentValue();

        abstract void clear();
    }

    class CacheIndexed extends CacheBase {
        final int[] multipliers;
        final CacheValue[] values;

        private CacheIndexed(int[] multipliers, int possible) {
            this.multipliers = multipliers;
            values = new CacheValue[possible];
        }

        @Override
        CacheValue getCurrentValue() {
            int index = computeIndex();
            if (index < 0 || index >= values.length) {
                if (index == MODEL_INDEX_INCORRECT) {
                    BCLog.logger.warn(
                        "[lib.model.adv_cache] Cache miss for indexed cache - this should be impossible! (index = "
                            + index + ", length = " + values.length + ")");
                    for (VariableInfo<?> var : variables) {
                        BCLog.logger.warn("            - " + var);
                    }
                }
                return computeFullModel();
            }
            CacheValue val = values[index];
            if (val == null) {
                val = computeFullModel();
                values[index] = val;
            }
            return val;
        }

        private int computeIndex() {
            int index = 0;
            for (int i = 0; i < variables.size(); i++) {
                VariableInfo<?> info = variables.get(i);
                if (!info.shouldCacheCurrentValue()) {
                    return MODEL_INDEX_NO_CACHE;
                }
                int ord = info.getCurrentOrdinal();
                if (ord < 0) {
                    return MODEL_INDEX_INCORRECT;
                }
                index += ord * multipliers[i];
            }
            return index;
        }

        @Override
        void clear() {
            Arrays.fill(values, null);
        }
    }

    static class CacheValue {
        final MutableQuad[] cutout, translucent;

        CacheValue(MutableQuad[] cutout, MutableQuad[] translucent) {
            this.cutout = cutout;
            this.translucent = translucent;
        }
    }
}
