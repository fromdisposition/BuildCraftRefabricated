package buildcraft.builders.snapshot;

import buildcraft.api.schematics.ISchematicBlock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PaletteIndex {
   private final List<ISchematicBlock> palette = new ArrayList<>();
   private final Map<ISchematicBlock, Integer> indexBySchematic = new HashMap<>();

   public int indexOf(ISchematicBlock schematic) {
      Integer existing = this.indexBySchematic.get(schematic);
      if (existing != null) {
         return existing;
      }

      int index = this.palette.size();
      this.palette.add(schematic);
      this.indexBySchematic.put(schematic, index);
      return index;
   }

   public List<ISchematicBlock> asList() {
      return this.palette;
   }

   public void clear() {
      this.palette.clear();
      this.indexBySchematic.clear();
   }

   public int size() {
      return this.palette.size();
   }
}
