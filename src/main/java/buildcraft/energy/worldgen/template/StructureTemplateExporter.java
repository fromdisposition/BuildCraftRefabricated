package buildcraft.energy.worldgen.template;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/** Writes structure template NBT via vanilla {@link StructureTemplateManager#save}. */
public final class StructureTemplateExporter {
   public record BlockEntry(int x, int y, int z, BlockState state) {}

   private StructureTemplateExporter() {
   }

   public static void write(
      final Path path,
      final HolderGetter<Block> blocks,
      final int sizeX,
      final int sizeY,
      final int sizeZ,
      final List<BlockEntry> entries
   ) throws IOException {
      Map<BlockState, Integer> paletteIndex = new LinkedHashMap<>();
      List<BlockEntry> normalized = new ArrayList<>(entries.size());
      for (BlockEntry entry : entries) {
         paletteIndex.putIfAbsent(entry.state(), paletteIndex.size());
         normalized.add(entry);
      }

      ListTag palette = new ListTag();
      for (BlockState state : paletteIndex.keySet()) {
         palette.add(NbtUtils.writeBlockState(state));
      }

      ListTag blockList = new ListTag();
      for (BlockEntry entry : normalized) {
         CompoundTag blockTag = new CompoundTag();
         blockTag.put("pos", toIntList(entry.x(), entry.y(), entry.z()));
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
      StructureTemplateManager.save(path, template, false);
   }

   public static int computeSizeY(final List<BlockEntry> blocks, final int fallback) {
      int minY = blocks.stream().mapToInt(BlockEntry::y).min().orElse(0);
      int maxY = blocks.stream().mapToInt(BlockEntry::y).max().orElse(0);
      return Math.max(fallback, maxY - minY + 1);
   }

   private static ListTag toIntList(final int x, final int y, final int z) {
      ListTag list = new ListTag();
      list.add(net.minecraft.nbt.IntTag.valueOf(x));
      list.add(net.minecraft.nbt.IntTag.valueOf(y));
      list.add(net.minecraft.nbt.IntTag.valueOf(z));
      return list;
   }
}
