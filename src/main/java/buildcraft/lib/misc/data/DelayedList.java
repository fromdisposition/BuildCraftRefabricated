/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

public class DelayedList<E> {
    protected final List<List<E>> elements;
    private final Supplier<List<E>> innerListSupplier;

    public DelayedList() {
        this(new ArrayList<>(), ArrayList::new);
    }

    public static <E> DelayedList<E> createConcurrent() {
        return new DelayedList<E>(
            Collections.synchronizedList(new ArrayList<>()),
            () -> Collections.synchronizedList(new ArrayList<>())
        ) {
            @Override
            public List<E> advance() {
                synchronized (this.elements) {
                    return super.advance();
                }
            }
        };
    }

    private DelayedList(List<List<E>> actualList, Supplier<List<E>> innerList) {
        elements = actualList;
        innerListSupplier = innerList;
    }

    public int getMaxDelay() {
        return elements.size();
    }

    public List<E> advance() {
        if (elements.isEmpty()) {
            return ImmutableList.of();
        }
        return elements.remove(0);
    }

    public void add(int delay, E element) {
        if (delay < 0) {
            delay = 0;
        }
        while (elements.size() < delay + 1) {
            elements.add(innerListSupplier.get());
        }
        elements.get(delay).add(element);
    }

    public List<List<E>> getAllElements() {
        return elements;
    }

    public void clear() {
        elements.clear();
    }
}
