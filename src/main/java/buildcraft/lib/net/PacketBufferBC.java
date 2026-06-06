package buildcraft.lib.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import net.minecraft.network.FriendlyByteBuf;

public class PacketBufferBC extends FriendlyByteBuf {
   private int readPartialOffset = 8;
   private int readPartialCache;
   private int writePartialIndex = -1;
   private int writePartialOffset;
   private int writePartialCache;

   public PacketBufferBC(ByteBuf wrapped) {
      super(wrapped);
   }

   /** @deprecated Use {@link BcPayloadBuffers#ensure(ByteBuf)} */
   @Deprecated
   public static PacketBufferBC asPacketBufferBc(ByteBuf buf) {
      return BcPayloadBuffers.ensure(buf);
   }

   public static PacketBufferBC write(IPayloadWriter writer) {
      PacketBufferBC buffer = BcPayloadBuffers.create();
      writer.write(buffer);
      return buffer;
   }

   public PacketBufferBC clear() {
      super.clear();
      this.readPartialOffset = 8;
      this.readPartialCache = 0;
      this.writePartialIndex = -1;
      this.writePartialOffset = 0;
      this.writePartialCache = 0;
      return this;
   }

   void writePartialBitsBegin() {
      if (this.writePartialIndex == -1 || this.writePartialOffset == 8) {
         this.writePartialIndex = this.writerIndex();
         this.writePartialOffset = 0;
         this.writePartialCache = 0;
         this.writeByte(0);
      }
   }

   void readPartialBitsBegin() {
      if (this.readPartialOffset == 8) {
         this.readPartialOffset = 0;
         this.readPartialCache = this.readUnsignedByte();
      }
   }

   public PacketBufferBC writeBoolean(boolean flag) {
      this.writePartialBitsBegin();
      int toWrite = (flag ? 1 : 0) << this.writePartialOffset;
      this.writePartialCache |= toWrite;
      this.writePartialOffset++;
      this.setByte(this.writePartialIndex, this.writePartialCache);
      return this;
   }

   public boolean readBoolean() {
      this.readPartialBitsBegin();
      int offset = 1 << this.readPartialOffset++;
      return (this.readPartialCache & offset) == offset;
   }

   public PacketBufferBC writeFixedBits(int value, int length) throws IllegalArgumentException {
      if (length <= 0) {
         throw new IllegalArgumentException("Tried to write too few bits! (" + length + ")");
      }

      if (length > 32) {
         throw new IllegalArgumentException("Tried to write more bits than are in an integer! (" + length + ")");
      }

      this.writePartialBitsBegin();
      if (this.writePartialOffset > 0) {
         int availableBits = 8 - this.writePartialOffset;
         if (availableBits >= length) {
            int mask = (1 << length) - 1;
            int bitsToWrite = value & mask;
            this.writePartialCache = this.writePartialCache | bitsToWrite << this.writePartialOffset;
            this.setByte(this.writePartialIndex, this.writePartialCache);
            this.writePartialOffset += length;
            return this;
         }

         int mask = (1 << availableBits) - 1;
         int shift = length - availableBits;
         int bitsToWrite = value >>> shift & mask;
         this.writePartialCache = this.writePartialCache | bitsToWrite << this.writePartialOffset;
         this.setByte(this.writePartialIndex, this.writePartialCache);
         this.writePartialCache = 0;
         this.writePartialOffset = 8;
         length -= availableBits;
      }

      while (length >= 8) {
         this.writePartialBitsBegin();
         int byteToWrite = value >>> length - 8 & 0xFF;
         this.setByte(this.writePartialIndex, byteToWrite);
         this.writePartialCache = 0;
         this.writePartialOffset = 8;
         length -= 8;
      }

      if (length > 0) {
         this.writePartialBitsBegin();
         int mask = (1 << length) - 1;
         this.writePartialCache = value & mask;
         this.setByte(this.writePartialIndex, this.writePartialCache);
         this.writePartialOffset = length;
      }

      return this;
   }

   public int readFixedBits(int length) throws IllegalArgumentException {
      if (length <= 0) {
         throw new IllegalArgumentException("Tried to read too few bits! (" + length + ")");
      }

      if (length > 32) {
         throw new IllegalArgumentException("Tried to read more bits than are in an integer! (" + length + ")");
      }

      this.readPartialBitsBegin();
      int value = 0;
      if (this.readPartialOffset > 0) {
         int availableBits = 8 - this.readPartialOffset;
         if (availableBits >= length) {
            int mask = (1 << length) - 1;
            value = this.readPartialCache >>> this.readPartialOffset & mask;
            this.readPartialOffset += length;
            return value;
         }

         int bitsRead = this.readPartialCache >>> this.readPartialOffset;
         value = bitsRead;
         this.readPartialCache = 0;
         this.readPartialOffset = 8;
         length -= availableBits;
      }

      while (length >= 8) {
         this.readPartialBitsBegin();
         length -= 8;
         value <<= 8;
         value |= this.readPartialCache;
         this.readPartialOffset = 8;
      }

      if (length > 0) {
         this.readPartialBitsBegin();
         int mask = (1 << length) - 1;
         value <<= length;
         value |= this.readPartialCache & mask;
         this.readPartialOffset = length;
      }

      return value;
   }

   public PacketBufferBC writeEnumValue(Enum<?> value) {
      Enum<?>[] possible = (Enum<?>[])value.getDeclaringClass().getEnumConstants();
      if (possible == null) {
         throw new IllegalArgumentException("Not an enum " + value.getClass());
      }

      if (possible.length == 0) {
         throw new IllegalArgumentException("Tried to write an enum value without any values!");
      }

      if (possible.length == 1) {
         return this;
      }

      int bits = 32 - Integer.numberOfLeadingZeros(possible.length - 1);
      if (bits < 1) {
         bits = 1;
      }

      this.writeFixedBits(value.ordinal(), bits);
      return this;
   }

   public <E extends Enum<E>> E readEnumValue(Class<E> enumClass) {
      E[] enums = (E[])enumClass.getEnumConstants();
      if (enums == null) {
         throw new IllegalArgumentException("Not an enum " + enumClass);
      }

      if (enums.length == 0) {
         throw new IllegalArgumentException("Tried to read an enum value without any values!");
      }

      if (enums.length == 1) {
         return enums[0];
      }

      int bits = 32 - Integer.numberOfLeadingZeros(enums.length - 1);
      if (bits < 1) {
         bits = 1;
      }

      int index = this.readFixedBits(bits);
      return enums[index];
   }

   public String readString() {
      int length = this.readVarInt();
      byte[] array = new byte[length];

      for (int i = 0; i < length; i++) {
         array[i] = this.readByte();
      }

      return new String(array, StandardCharsets.UTF_8);
   }
}
