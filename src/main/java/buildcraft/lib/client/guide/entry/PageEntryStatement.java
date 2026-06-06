package buildcraft.lib.client.guide.entry;

import buildcraft.api.registry.IScriptableRegistry;
import buildcraft.api.statements.IAction;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.ITrigger;
import buildcraft.api.statements.StatementManager;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.data.JsonTypeTags;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.contents.PageLinkStatement;
import buildcraft.lib.client.guide.ref.GuideGroupManager;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.statement.GuiElementStatementSource;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.TreeMap;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;

public class PageEntryStatement extends PageValueType<IStatement> {
   public static final PageEntryStatement INSTANCE = new PageEntryStatement();
   private static final JsonTypeTags TRIGGER_TAGS = new JsonTypeTags("buildcraft.guide.contents.triggers");
   private static final JsonTypeTags ACTION_TAGS = new JsonTypeTags("buildcraft.guide.contents.actions");

   @Override
   public Class<IStatement> getEntryClass() {
      return IStatement.class;
   }

   @Override
   public void iterateAllDefault(IEntryLinkConsumer consumer, ProfilerFiller prof) {
      for (IStatement statement : new TreeMap<>(StatementManager.statements).values()) {
         if (GuideManager.INSTANCE.objectsAdded.add(statement)) {
            JsonTypeTags parent;
            if (statement instanceof ITrigger) {
               parent = TRIGGER_TAGS;
            } else {
               if (!(statement instanceof IAction)) {
                  continue;
               }

               parent = ACTION_TAGS;
            }

            boolean hidden = GuideManager.INSTANCE.isStatementHiddenByCategory(statement);
            consumer.addChild(parent, new PageLinkStatement(!hidden, statement));
         }
      }
   }

   @Override
   public IScriptableRegistry.OptionallyDisabled<PageEntry<IStatement>> deserialize(Identifier name, JsonObject json, JsonDeserializationContext ctx) {
      if (!json.has("statement")) {
         throw new JsonSyntaxException("Missing 'statement' field in " + json);
      } else {
         String stmntName = json.get("statement").getAsString();
         IStatement stmnt = StatementManager.statements.get(stmntName);
         if (stmnt == null) {
            throw new JsonSyntaxException("Unknown statement '" + stmntName + "'");
         } else {
            return new IScriptableRegistry.OptionallyDisabled<>(new PageEntry<>(this, name, json, stmnt));
         }
      }
   }

   public List<String> getTooltip(IStatement value) {
      return value.getTooltip();
   }

   public String getTitle(IStatement value) {
      List<String> tooltip = value.getTooltip();
      return tooltip.isEmpty() ? value.getClass().toString() : tooltip.get(0);
   }

   @Nullable
   public ISimpleDrawable createDrawable(IStatement value) {
      return (x, y) -> GuiElementStatementSource.drawGuiSlot(value, x, y);
   }

   public void addPageEntries(IStatement value, GuiGuide gui, List<GuidePart> parts) {
      GuideGroupManager.appendLinkedChapters(INSTANCE.wrap(value), gui, parts);
   }
}
