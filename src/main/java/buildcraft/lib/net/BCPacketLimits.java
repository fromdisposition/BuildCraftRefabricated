/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.net;

public final class BCPacketLimits {
   public static final int MAX_PAYLOAD_BYTES = 65536;
   public static final int MAX_CHUNK_BYTES = 32768;
   public static final int MAX_ASSEMBLED_BYTES = 4194304;
   public static final long MAX_COMPRESSED_NBT_BYTES = 2097152L;
   public static final int MAX_MARKER_POSITIONS = 8192;
   public static final int MAX_DEBUG_STRINGS = 256;
   public static final int MAX_DEBUG_STRING_LENGTH = 4096;
   public static final int MAX_WIRE_SYSTEMS = 2048;
   public static final int MAX_WIRE_ELEMENTS_PER_SYSTEM = 4096;
   public static final int MAX_WIRE_POWERED_ENTRIES = 8192;
   public static final int MAX_VOLUME_BOXES = 512;
   public static final int MAX_PIPE_ITEM_BLOCKS = 4000;
   public static final int MAX_PIPE_ITEMS_PER_BLOCK = 10;
   public static final int MAX_PIPE_PAYLOAD_ENTRIES = 4000;
   public static final int MAX_TRANSPORT_BATCH_PACKET_BYTES = 524288;
   public static final int MAX_PIPE_ITEM_PACKET_BYTES = 524288;
   public static final int MAX_PIPE_PAYLOAD_PACKET_BYTES = 524288;
   public static final int MAX_WIRE_SYNC_PACKET_BYTES = 524288;
   public static final int MAX_ARCHITECT_SCAN_POSITIONS = 500000;

   private BCPacketLimits() {
   }

   public static int estimatePipePayloadEntryBytes(byte[] payload) {
      return 12 + (payload != null ? payload.length : 0);
   }

   public static int validateCount(int count, int max, String field) {
      if (count >= 0 && count <= max) {
         return count;
      } else {
         throw new IllegalArgumentException("Invalid " + field + " count: " + count + " (max " + max + ")");
      }
   }

   public static void validateChunkSize(int length) {
      if (length < 0 || length > 32768) {
         throw new IllegalArgumentException("Invalid chunk size: " + length);
      }
   }

   public static void validateAssembledSize(int total) {
      if (total < 0 || total > 4194304) {
         throw new IllegalArgumentException("Assembled payload too large: " + total);
      }
   }
}
