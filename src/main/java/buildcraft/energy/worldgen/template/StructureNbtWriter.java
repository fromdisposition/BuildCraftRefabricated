package buildcraft.energy.worldgen.template;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;

/** Writes gzip-compressed structure template NBT files for jigsaw pool elements. */
public final class StructureNbtWriter {
   public record BlockEntry(int x, int y, int z, String blockId) {}

   private StructureNbtWriter() {
   }

   public static void write(Path path, int sizeX, int sizeY, int sizeZ, List<BlockEntry> blocks) throws IOException {
      Map<String, Integer> paletteIndex = new LinkedHashMap<>();
      List<BlockEntry> normalized = new ArrayList<>(blocks.size());
      for (BlockEntry block : blocks) {
         paletteIndex.putIfAbsent(block.blockId, paletteIndex.size());
         normalized.add(block);
      }

      ListTag palette = new ListTag();
      for (String blockId : paletteIndex.keySet()) {
         CompoundTag state = new CompoundTag();
         state.putString("Name", blockId);
         palette.add(state);
      }

      ListTag blockList = new ListTag();
      for (BlockEntry block : normalized) {
         CompoundTag entry = new CompoundTag();
         entry.put("pos", toIntList(block.x, block.y, block.z));
         entry.putInt("state", paletteIndex.get(block.blockId));
         blockList.add(entry);
      }

      CompoundTag root = new CompoundTag();
      root.put("size", toIntList(sizeX, sizeY, sizeZ));
      root.put("palette", palette);
      root.put("blocks", blockList);
      root.put("entities", new ListTag());

      Files.createDirectories(path.getParent());
      try (OutputStream output = Files.newOutputStream(path)) {
         NbtIo.writeCompressed(NbtUtils.addCurrentDataVersion(root), output);
      }
   }

   public static CompoundTag read(Path path) throws IOException {
      return NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
   }

   public static int computeSizeY(List<BlockEntry> blocks, int fallback) {
      int minY = blocks.stream().mapToInt(BlockEntry::y).min().orElse(0);
      int maxY = blocks.stream().mapToInt(BlockEntry::y).max().orElse(0);
      return Math.max(fallback, maxY - minY + 1);
   }

   private static ListTag toIntList(int x, int y, int z) {
      ListTag list = new ListTag();
      list.add(net.minecraft.nbt.IntTag.valueOf(x));
      list.add(net.minecraft.nbt.IntTag.valueOf(y));
      list.add(net.minecraft.nbt.IntTag.valueOf(z));
      return list;
   }
}
