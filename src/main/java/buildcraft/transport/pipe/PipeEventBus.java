/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.transport.pipe.IPipeEventBus;
import buildcraft.api.transport.pipe.PipeBehaviour;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeFlow;
import buildcraft.api.transport.pluggable.PipePluggable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PipeEventBus implements IPipeEventBus {
   public static final boolean DEBUG = BCDebugging.shouldDebugLog("transport.pipe.event_bus");

   private record Entry(Object source, Consumer<PipeEvent> consumer) {}

   private final Map<Class<? extends PipeEvent>, List<Entry>> handlers = new LinkedHashMap<>();

   private static final List<Consumer<PipeEventBus>> globalRegistrants = new ArrayList<>();

   public PipeEventBus() {
      for (Consumer<PipeEventBus> r : globalRegistrants) {
         r.accept(this);
      }
   }

   public static void addGlobalRegistrant(Consumer<PipeEventBus> registrant) {
      globalRegistrants.add(registrant);
   }

   @SuppressWarnings("unchecked")
   @Override
   public <E extends PipeEvent> void on(Class<E> type, Object source, Consumer<E> handler) {
      handlers.computeIfAbsent(type, k -> new ArrayList<>())
              .add(new Entry(source, e -> handler.accept((E) e)));
   }

   public void registerHandler(Object obj) {
      if (obj instanceof PipeBehaviour b) b.registerEventHandlers(this);
      else if (obj instanceof PipeFlow f) f.registerEventHandlers(this);
      else if (obj instanceof PipePluggable p) p.registerEventHandlers(this);
   }

   public void unregisterHandler(Object obj) {
      if (obj != null) {
         handlers.values().forEach(list -> list.removeIf(e -> e.source() == obj));
      }
   }

   public boolean fireEvent(PipeEvent event) {
      if (DEBUG) {
         String error = event.checkStateForErrors();
         if (error != null) {
            throw new IllegalArgumentException(
               "The event " + event.getClass() + " was in an invalid state when firing! This is DEFINITELY a bug!\n(error = " + error + ")"
            );
         }
      }

      List<Entry> list = handlers.get(event.getClass());
      if (list == null || list.isEmpty()) return false;

      for (Entry entry : list) {
         if (event.isCanceled()) break;
         entry.consumer().accept(event);
         if (DEBUG) {
            String error = event.checkStateForErrors();
            if (error != null) {
               throw new IllegalStateException(
                  "The event " + event.getClass() + " was in an invalid state after being handled (error = " + error + ")"
               );
            }
         }
      }

      return true;
   }
}
