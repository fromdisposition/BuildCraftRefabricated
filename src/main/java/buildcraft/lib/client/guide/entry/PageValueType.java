package buildcraft.lib.client.guide.entry;

import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.gui.ISimpleDrawable;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class PageValueType<T> {
   public abstract IScriptableRegistry.OptionallyDisabled<PageEntry<T>> deserialize(Identifier var1, JsonObject var2, JsonDeserializationContext var3);

   public abstract Class<T> getEntryClass();

   public boolean matches(T value, Object test) {
      return Objects.equals(test, value);
   }

   @Nullable
   public abstract ISimpleDrawable createDrawable(T var1);

   public Object getBasicValue(T value) {
      return value;
   }

   public abstract String getTitle(T var1);

   public abstract List<String> getTooltip(T var1);

   public abstract void iterateAllDefault(IEntryLinkConsumer var1, ProfilerFiller var2);

   public IScriptableRegistry.OptionallyDisabled<Object> createLink(String to, ProfilerFiller prof) {
      return new IScriptableRegistry.OptionallyDisabled<>(this.getClass().getSimpleName() + " doesn't support links");
   }

   @Nullable
   public final PageValue<T> wrap(Object value) {
      T typed = this.getEntryClass().cast(value);
      return this.isValid(typed) ? new PageValue<>(this, typed) : null;
   }

   protected boolean isValid(T typed) {
      return true;
   }

   public void addPageEntries(T value, GuiGuide gui, List<GuidePart> parts) {
   }
}
