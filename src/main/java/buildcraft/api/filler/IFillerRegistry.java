package buildcraft.api.filler;

import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public interface IFillerRegistry {
   void addPattern(IFillerPattern var1);

   @Nullable
   IFillerPattern getPattern(String var1);

   Collection<IFillerPattern> getPatterns();

   IFilledTemplate createFilledTemplate(BlockPos var1, BlockPos var2);
}
