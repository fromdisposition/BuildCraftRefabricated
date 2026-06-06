package buildcraft.builders.snapshot;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class JsonRule {
   public List<JsonSelector> selectors = null;
   public List<RequiredExtractor> requiredExtractors = null;
   public List<BlockPos> requiredBlockOffsets = null;
   public List<String> ignoredProperties = null;
   public List<BlockPos> updateBlockOffsets = null;
   public String placeBlock = null;
   public List<String> canBeReplacedWithBlocks = null;
   public CompoundTag replaceNbt = null;
   public boolean ignore = false;
   public boolean capture = false;
}
