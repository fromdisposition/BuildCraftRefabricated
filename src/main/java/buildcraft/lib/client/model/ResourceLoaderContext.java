package buildcraft.lib.client.model;

import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public class ResourceLoaderContext {
   private final Set<Identifier> loaded = new HashSet<>();
   private final Deque<Identifier> loadingStack = new ArrayDeque<>();

   public InputStreamReader startLoading(Identifier location) throws IOException {
      if (!this.loaded.add(location)) {
         throw new JsonSyntaxException("Already loaded " + location + " from " + this.loadingStack.peek());
      }

      this.loadingStack.push(location);
      Resource res = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location);
      return new InputStreamReader(res.open(), StandardCharsets.UTF_8);
   }

   public void finishLoading() {
      this.loadingStack.pop();
   }
}
