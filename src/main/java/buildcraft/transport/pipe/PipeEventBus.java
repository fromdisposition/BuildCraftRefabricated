package buildcraft.transport.pipe;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.transport.pipe.PipeEvent;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventPriority;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipeEventBus {
   public static final boolean DEBUG = BCDebugging.shouldDebugLog("transport.pipe.event_bus");
   private static final Map<Class<?>, List<PipeEventBus.Handler>> allHandlers = new HashMap<>();
   private static final List<Object> globalHandlers = new ArrayList<>();
   private final List<PipeEventBus.LocalHandler> currentHandlers = new ArrayList<>();

   public PipeEventBus() {
      for (Object handler : globalHandlers) {
         this.registerHandler(handler);
      }
   }

   public static void registerGlobalHandler(Object handler) {
      if (handler != null && !globalHandlers.contains(handler)) {
         globalHandlers.add(handler);
      }
   }

   private static List<PipeEventBus.LocalHandler> getAndBindHandlers(Object obj) {
      Class<?> cls = obj instanceof Class ? (Class)obj : obj.getClass();
      List<PipeEventBus.Handler> handlerList = getHandlers(cls);
      List<PipeEventBus.LocalHandler> list = new ArrayList<>();

      for (PipeEventBus.Handler handler : handlerList) {
         PipeEventBus.LocalHandler bound = handler.bindTo(obj);
         if (bound != null) {
            list.add(bound);
         }
      }

      return list;
   }

   private static List<PipeEventBus.Handler> getHandlers(Class<?> cls) {
      if (!allHandlers.containsKey(cls)) {
         List<PipeEventBus.Handler> list = new ArrayList<>();
         Class<?> superCls = cls.getSuperclass();
         if (superCls != null) {
            list.addAll(getHandlers(superCls));
         }

         for (Method m : cls.getDeclaredMethods()) {
            PipeEventHandler annot = m.getAnnotation(PipeEventHandler.class);
            if (annot != null) {
               Parameter[] params = m.getParameters();
               if (params.length != 1) {
                  throw new IllegalStateException(
                     "Cannot annotate " + m + " with @PipeEventHandler as it had an incorrect number of parameters (" + Arrays.toString(params) + ")"
                  );
               }

               Parameter p = params[0];
               if (!PipeEvent.class.isAssignableFrom(p.getType())) {
                  throw new IllegalStateException("Cannot annotate " + m + " with @PipeEventHandler as it did not take a pipe event! (" + p.getType() + ")");
               }

               MethodHandle mh;
               try {
                  mh = MethodHandles.publicLookup().unreflect(m);
               } catch (IllegalAccessException e) {
                  throw new IllegalStateException("Cannot annotate " + m + " with @PipeEventHandler as there was a problem with it!", e);
               }

               boolean isStatic = Modifier.isStatic(m.getModifiers());
               String methodName = m.toString();
               list.add(new PipeEventBus.Handler(annot.priority(), annot.receiveCancelled(), isStatic, methodName, mh, p.getType()));
            }
         }

         allHandlers.put(cls, list);
         return list;
      } else {
         return allHandlers.get(cls);
      }
   }

   public void registerHandler(Object obj) {
      if (obj != null) {
         this.currentHandlers.addAll(getAndBindHandlers(obj));
         Collections.sort(this.currentHandlers);
      }
   }

   public void unregisterHandler(Object obj) {
      if (obj != null) {
         this.currentHandlers.removeIf(next -> next.target == obj);
      }
   }

   public boolean fireEvent(PipeEvent event) {
      boolean handled = false;
      if (DEBUG) {
         String error = event.checkStateForErrors();
         if (error != null) {
            throw new IllegalArgumentException(
               "The event " + event.getClass() + " was in an invalid state when firing! This is DEFINITELY a bug!\n(error = " + error + ")"
            );
         }
      }

      for (PipeEventBus.LocalHandler handler : this.currentHandlers) {
         handled |= handler.handleEvent(event);
         if (DEBUG) {
            String error = event.checkStateForErrors();
            if (error != null) {
               throw new IllegalStateException(
                  "The event " + event.getClass() + " was in an invalid state after being handled by " + handler.methodName + " (error = " + error + ")"
               );
            }
         }
      }

      return handled;
   }

   public static class Handler {
      final PipeEventPriority priority;
      final boolean receiveCanceled;
      final boolean isStatic;
      final String methodName;
      final MethodHandle handle;
      final Class<?> eventClassHandled;

      public Handler(PipeEventPriority priority, boolean receiveCanceled, boolean isStatic, String methodName, MethodHandle handle, Class<?> eventClassHandled) {
         this.priority = priority;
         this.receiveCanceled = receiveCanceled;
         this.isStatic = isStatic;
         this.methodName = methodName;
         this.handle = handle;
         this.eventClassHandled = eventClassHandled;
      }

      public PipeEventBus.LocalHandler bindTo(Object obj) {
         if (!this.isStatic && obj instanceof Class) {
            return null;
         }

         MethodHandle bound = this.isStatic ? this.handle : this.handle.bindTo(obj);
         return new PipeEventBus.LocalHandler(this.priority, this.receiveCanceled, obj, this.methodName, this.eventClassHandled, bound);
      }
   }

   public static class LocalHandler implements Comparable<PipeEventBus.LocalHandler> {
      final PipeEventPriority priority;
      final boolean receiveCanceled;
      final Object target;
      final String methodName;
      final Class<?> classHandled;
      final MethodHandle handle;

      public LocalHandler(PipeEventPriority priority, boolean receiveCanceled, Object target, String methodName, Class<?> classHandled, MethodHandle handle) {
         this.priority = priority;
         this.receiveCanceled = receiveCanceled;
         this.target = target;
         this.methodName = methodName;
         this.classHandled = classHandled;
         this.handle = handle;
      }

      public boolean handleEvent(PipeEvent event) {
         if (!this.receiveCanceled && event.isCanceled()) {
            return false;
         }

         if (this.classHandled.isAssignableFrom(event.getClass())) {
            try {
               this.handle.invoke((PipeEvent)event);
               return true;
            } catch (Throwable e) {
               throw new IllegalStateException(e);
            }
         } else {
            return false;
         }
      }

      public int compareTo(PipeEventBus.LocalHandler o) {
         return this.priority.compareTo(o.priority);
      }
   }
}
