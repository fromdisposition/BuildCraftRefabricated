/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc.data;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import java.util.ArrayList;
import java.util.List;

public class IdAllocator {
   public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.id_alloc");
   private final IdAllocator parent;
   private final String name;
   private final List<String> idNameMap = new ArrayList<>();
   private boolean hasChildren;
   private int nextId = 0;

   private IdAllocator(IdAllocator parent, String name) {
      this.parent = parent;
      this.name = parent == null ? name : parent.name + "." + name;
      if (parent != null) {
         this.idNameMap.addAll(parent.idNameMap);
      }

      this.nextId = parent == null ? 0 : parent.nextId;
   }

   public IdAllocator() {
      this(null, "unknown");
   }

   public IdAllocator(String name) {
      this(null, name);
   }

   public IdAllocator makeChild(String childName) {
      this.hasChildren = true;
      return new IdAllocator(this, childName);
   }

   public String getNameFor(int id) {
      if (id < 0) {
         return "NEGATIVE ID " + id;
      } else {
         return id >= this.idNameMap.size() ? "UNKNOWN_CHILD " + id : this.idNameMap.get(id);
      }
   }

   public int allocId(String allocName) {
      if (this.hasChildren) {
         throw new IllegalStateException("A child of this object has already allocated ID's! You have probably set the calling class up wrong!");
      }

      if (DEBUG) {
         BCLog.logger.info("[lib.id_alloc] " + this.name + " allocated " + allocName + " as " + this.nextId);
      }

      this.idNameMap.add(allocName);
      return this.nextId++;
   }
}
