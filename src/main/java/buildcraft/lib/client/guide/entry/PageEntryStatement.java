/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.client.guide.parts.contents.PageLinkStatement;
import buildcraft.lib.client.guide.ref.GuideGroupManager;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.statement.GuiElementStatementSource;
import buildcraft.lib.misc.LocaleUtil;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfilerFiller;

public class PageEntryStatement extends PageValueType<IStatement> {
   public static final PageEntryStatement INSTANCE = new PageEntryStatement();
   private static final JsonTypeTags TRIGGER_TAGS = new JsonTypeTags("buildcraft.guide.contents.triggers");
   private static final JsonTypeTags ACTION_TAGS = new JsonTypeTags("buildcraft.guide.contents.actions");

   /**
    * Statement family (matched by uniqueTag prefix) -> a lang key describing what that whole family does. This
    * only fills the auto-generated statement pages that have no written .md of their own (paint colours, pipe-wire
    * signals, power limits, Emzuli presets, robot/station actions). Families whose every variant already has a
    * written page never reach here. Longer prefixes MUST precede the shorter ones they contain (work_filter_tool
    * before work_filter).
    */
   private static final Map<String, String> FAMILY_DESC = new LinkedHashMap<>();

   static {
      FAMILY_DESC.put("buildcraft:pipe.color.", "buildcraft.guide.statement.desc.pipe_paint");
      FAMILY_DESC.put("buildcraft:pipe.wire.output.", "buildcraft.guide.statement.desc.wire_emit");
      FAMILY_DESC.put("buildcraft:pipe.wire.input.", "buildcraft.guide.statement.desc.wire_read");
      FAMILY_DESC.put("buildcraft:pipe.power_limit.", "buildcraft.guide.statement.desc.power_limit");
      FAMILY_DESC.put("buildcraft:extraction.preset.", "buildcraft.guide.statement.desc.emzuli_preset");
      FAMILY_DESC.put("buildcraft:robot.goto_station", "buildcraft.guide.statement.desc.robot_goto_station");
      FAMILY_DESC.put("buildcraft:robot.wakeup", "buildcraft.guide.statement.desc.robot_wakeup");
      FAMILY_DESC.put("buildcraft:robot.work_filter_tool", "buildcraft.guide.statement.desc.robot_filter_tool");
      FAMILY_DESC.put("buildcraft:robot.work_filter", "buildcraft.guide.statement.desc.robot_filter");
      FAMILY_DESC.put("buildcraft:robot.work_in_area", "buildcraft.guide.statement.desc.robot_work_area");
      FAMILY_DESC.put("buildcraft:robot.load_unload_area", "buildcraft.guide.statement.desc.robot_load_unload");
      FAMILY_DESC.put("buildcraft:robot.sleep", "buildcraft.guide.statement.desc.robot_sleep");
      FAMILY_DESC.put("buildcraft:robot.in.station", "buildcraft.guide.statement.desc.robot_in_station");
      FAMILY_DESC.put("buildcraft:robot.reserved", "buildcraft.guide.statement.desc.robot_linked");
      FAMILY_DESC.put("buildcraft:robot.linked", "buildcraft.guide.statement.desc.robot_linked");
      FAMILY_DESC.put("buildcraft:station.provide_items", "buildcraft.guide.statement.desc.station_provide_items");
      FAMILY_DESC.put("buildcraft:station.request_items", "buildcraft.guide.statement.desc.station_request_items");
      FAMILY_DESC.put("buildcraft:station.accept_items", "buildcraft.guide.statement.desc.station_accept_items");
      FAMILY_DESC.put("buildcraft:station.drop_in_pipe", "buildcraft.guide.statement.desc.station_accept_items");
      FAMILY_DESC.put("buildcraft:station.provide_fluids", "buildcraft.guide.statement.desc.station_provide_fluids");
      FAMILY_DESC.put("buildcraft:station.accept_fluids", "buildcraft.guide.statement.desc.station_accept_fluids");
      FAMILY_DESC.put("buildcraft:station.machine_request_items", "buildcraft.guide.statement.desc.station_machine_request");
      FAMILY_DESC.put("buildcraft:station.forbid_robot", "buildcraft.guide.statement.desc.station_forbid");
      FAMILY_DESC.put("buildcraft:station.force_robot", "buildcraft.guide.statement.desc.station_forbid");
   }

   /** The family description for a statement, falling back to a generic trigger/action note if the family is unknown. */
   private static String familyDescription(IStatement value) {
      String tag = value.getUniqueTag();
      if (tag != null) {
         for (Map.Entry<String, String> family : FAMILY_DESC.entrySet()) {
            if (tag.startsWith(family.getKey())) {
               return LocaleUtil.localize(family.getValue());
            }
         }
      }

      String kind = value instanceof ITrigger ? "trigger" : "action";
      return LocaleUtil.localize("buildcraft.guide.statement." + kind + ".desc");
   }

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
      // A statement carries a name but no description of its own, and an auto-generated statement page is built
      // with an empty body — so without this the page would be blank. When there is no written .md (parts holds
      // only the auto-added title chapter), fill the page: the statement's own extra tooltip lines plus a real
      // description of its family (familyDescription). Pages that already have a written .md are left as they
      // were (their body + the usage links below).
      boolean hasWrittenBody = parts.size() > 1;

      if (!hasWrittenBody) {
         List<String> tooltip = value.getTooltip();
         for (int i = 1; i < tooltip.size(); i++) {
            String line = tooltip.get(i);
            if (line != null && !line.trim().isEmpty()) {
               parts.add(new GuideText(gui, line));
            }
         }

         parts.add(new GuideText(gui, familyDescription(value)));
      }

      GuideGroupManager.appendLinkedChapters(INSTANCE.wrap(value), gui, parts);
   }
}
