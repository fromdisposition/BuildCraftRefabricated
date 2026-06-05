package buildcraft.api.core;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class AreaProviders {
    public static final List<IAreaProviderGetter> providers = new ArrayList<>();

    public interface IAreaProviderGetter {

        List<IAreaProvider> getAreaProviders(Level world, BlockPos at);
    }

    public static List<IAreaProvider> getAreaProviders(Level world, BlockPos at) {
        List<IAreaProvider> list = new ArrayList<>();
        for (IAreaProviderGetter getter : providers) {
            list.addAll(getter.getAreaProviders(world, at));
        }
        return list;
    }
}
