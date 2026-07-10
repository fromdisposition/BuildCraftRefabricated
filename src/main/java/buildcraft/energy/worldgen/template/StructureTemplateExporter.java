package buildcraft.energy.worldgen.template;

import com.google.common.hash.Hashing;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.CachedOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** Writes compressed structure template NBT files for the oil worldgen jigsaw pools. */
public final class StructureTemplateExporter {
   public record BlockEntry(int x, int y, int z, BlockState state) {}

   private StructureTemplateExporter() {
   }

   // Hashing.sha1() is deprecated in Guava but is exactly what Minecraft's CachedOutput/DataProvider use to key the
   // datagen cache, so match it (using a different algorithm would just churn every entry's hash).
   @SuppressWarnings("deprecation")
   public static void write(
      final CachedOutput cache,
      final Path path,
      final HolderGetter<Block> blocks,
      final int sizeX,
      final int sizeZ,
      final int yOffset,
      final int sizeYFallback,
      final List<BlockEntry> entries
   ) throws IOException {
      Map<BlockState, Integer> paletteIndex = new LinkedHashMap<>();
      List<BlockEntry> normalized = new ArrayList<>(entries.size());
      for (BlockEntry entry : entries) {
         paletteIndex.putIfAbsent(entry.state(), paletteIndex.size());
         normalized.add(entry);
      }

      // yOffset lifts every block into non-negative space; sizeY then spans 0..maxStoredY inclusive. A position that
      // is still negative would be an invalid template that Minecraft's tooling and jigsaw placer reject, so guard it.
      int maxStoredY = 0;
      for (BlockEntry entry : normalized) {
         int storedY = entry.y() + yOffset;
         if (storedY < 0) {
            throw new IllegalStateException("Negative block Y " + storedY + " (" + entry.y() + " + " + yOffset + ") for " + path);
         }
         maxStoredY = Math.max(maxStoredY, storedY);
      }
      int sizeY = Math.max(sizeYFallback, maxStoredY + 1);

      ListTag palette = new ListTag();
      for (BlockState state : paletteIndex.keySet()) {
         palette.add(NbtUtils.writeBlockState(state));
      }

      ListTag blockList = new ListTag();
      for (BlockEntry entry : normalized) {
         CompoundTag blockTag = new CompoundTag();
         blockTag.put("pos", toIntList(entry.x(), entry.y() + yOffset, entry.z()));
         blockTag.putInt("state", paletteIndex.get(entry.state()));
         blockList.add(blockTag);
      }

      CompoundTag tag = new CompoundTag();
      tag.put("size", toIntList(sizeX, sizeY, sizeZ));
      tag.put("palette", palette);
      tag.put("blocks", blockList);
      tag.put("entities", new ListTag());

      StructureTemplate template = new StructureTemplate();
      template.load(blocks, tag);
      // Write through the datagen CachedOutput so the file is registered in its manifest. Writing the NBT straight to
      // disk (NbtIo/StructureTemplateManager.save) does create it, but the CachedOutput then prunes it as an untracked
      // "stale" file at the end of the run -- which is why the structure directory kept ending up empty.
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      NbtIo.writeCompressed(template.save(new CompoundTag()), out);
      byte[] data = out.toByteArray();
      cache.writeIfNeeded(path, data, Hashing.sha1().hashBytes(data));
   }

   private static ListTag toIntList(final int x, final int y, final int z) {
      ListTag list = new ListTag();
      list.add(net.minecraft.nbt.IntTag.valueOf(x));
      list.add(net.minecraft.nbt.IntTag.valueOf(y));
      list.add(net.minecraft.nbt.IntTag.valueOf(z));
      return list;
   }
}
