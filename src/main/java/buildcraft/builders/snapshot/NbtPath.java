package buildcraft.builders.snapshot;

import buildcraft.lib.misc.NBTUtilBC;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class NbtPath {
   private final List<String> elements;
   public static final JsonDeserializer<NbtPath> DESERIALIZER = (json, typeOfT, context) -> new NbtPath(
      context.deserialize(json, TypeToken.getParameterized(List.class, String.class).getType())
   );

   private NbtPath(List<String> elements) {
      this.elements = elements;
   }

   public Tag get(ByteTag tag) {
      return (Tag)(this.elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL);
   }

   public Tag get(ShortTag tag) {
      return (Tag)(this.elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL);
   }

   public Tag get(IntTag tag) {
      return (Tag)(this.elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL);
   }

   public Tag get(LongTag tag) {
      return (Tag)(this.elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL);
   }

   public Tag get(FloatTag tag) {
      return (Tag)(this.elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL);
   }

   public Tag get(DoubleTag tag) {
      return (Tag)(this.elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL);
   }

   public Tag get(ByteArrayTag tag) {
      if (this.elements.size() == 1) {
         int key;
         try {
            key = Integer.parseInt(this.elements.get(0));
         } catch (NumberFormatException e) {
            return NBTUtilBC.NBT_NULL;
         }

         byte[] bytes = tag.getAsByteArray();
         return (Tag)(key >= 0 && key < bytes.length ? ByteTag.valueOf(bytes[key]) : NBTUtilBC.NBT_NULL);
      } else {
         return (Tag)(this.elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL);
      }
   }

   public Tag get(StringTag tag) {
      return (Tag)(this.elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL);
   }

   public Tag get(ListTag tag) {
      if (this.elements.size() == 1) {
         int key;
         try {
            key = Integer.parseInt(this.elements.get(0));
         } catch (NumberFormatException e) {
            return NBTUtilBC.NBT_NULL;
         }

         return key >= 0 && key < tag.size() ? new NbtPath(this.elements.subList(1, this.elements.size())).get(tag.get(key)) : NBTUtilBC.NBT_NULL;
      } else {
         return (Tag)(this.elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL);
      }
   }

   public Tag get(CompoundTag tag) {
      if (!this.elements.isEmpty()) {
         String key = this.elements.get(0);
         return tag.contains(key) ? new NbtPath(this.elements.subList(1, this.elements.size())).get(tag.get(key)) : NBTUtilBC.NBT_NULL;
      } else {
         return tag;
      }
   }

   public Tag get(IntArrayTag tag) {
      if (this.elements.size() == 1) {
         int key;
         try {
            key = Integer.parseInt(this.elements.get(0));
         } catch (NumberFormatException e) {
            return NBTUtilBC.NBT_NULL;
         }

         int[] ints = tag.getAsIntArray();
         return (Tag)(key >= 0 && key < ints.length ? IntTag.valueOf(ints[key]) : NBTUtilBC.NBT_NULL);
      } else {
         return (Tag)(this.elements.isEmpty() ? tag : NBTUtilBC.NBT_NULL);
      }
   }

   @SuppressWarnings("unchecked")
   public <N extends Tag> N getTyped(Tag tag) {
      return (N)this.get(tag);
   }

   public Tag get(Tag tag) {
      if (tag == null) {
         return NBTUtilBC.NBT_NULL;
      }

      return switch (tag.getId()) {
         case 1 -> this.get((ByteTag)tag);
         case 2 -> this.get((ShortTag)tag);
         case 3 -> this.get((IntTag)tag);
         case 4 -> this.get((LongTag)tag);
         case 5 -> this.get((FloatTag)tag);
         case 6 -> this.get((DoubleTag)tag);
         case 7 -> this.get((ByteArrayTag)tag);
         case 8 -> this.get((StringTag)tag);
         case 9 -> this.get((ListTag)tag);
         case 10 -> this.get((CompoundTag)tag);
         case 11 -> this.get((IntArrayTag)tag);
         default -> NBTUtilBC.NBT_NULL;
      };
   }

   public void remove(CompoundTag root) {
      if (!this.elements.isEmpty()) {
         CompoundTag current = root;

         for (int i = 0; i < this.elements.size() - 1; i++) {
            if (!(current.get(this.elements.get(i)) instanceof CompoundTag c)) {
               return;
            }

            current = c;
         }

         current.remove(this.elements.get(this.elements.size() - 1));
      }
   }

   @Override
   public String toString() {
      return "NbtPath{" + this.elements + "}";
   }
}
