/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import buildcraft.fabric.BCRegistries;
import buildcraft.silicon.client.BCSiliconClient;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadeStateManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.CreativeModeTab.Row;

public final class BCSiliconCreativeTabs {
   public static CreativeModeTab FACADE_TAB;
   //? if >= 1.21.10 {
   public static final ResourceKey<CreativeModeTab> FACADE_TAB_KEY = BCRegistries.creativeTabKey("buildcraftsilicon", "facades");
   //?} else {
   /*// Fabric 1.21.1 orders modded creative tabs alphabetically by registry id (it ignores the Row/column
   // BC sets), which would place "buildcraftsilicon:facades" before the transport tabs. Register under an
   // id that sorts after "buildcrafttransport:plugs" so the tab stays last, matching 26.x. The id is never
   // referenced elsewhere and the title comes from the translation key, so this is invisible otherwise.
   public static final ResourceKey<CreativeModeTab> FACADE_TAB_KEY = BCRegistries.creativeTabKey("buildcrafttransport", "zz_facades");
   *///?}

   private BCSiliconCreativeTabs() {
   }

   public static void register() {
      FACADE_TAB = BCRegistries.registerCreativeTab(
         //? if >= 1.21.10 {
         "buildcraftsilicon",
         "facades",
         //?} else {
         /*// see FACADE_TAB_KEY: id chosen so Fabric 1.21.1's alphabetical modded-tab sort keeps facades last.
         "buildcrafttransport",
         "zz_facades",
         *///?}
         CreativeModeTab.builder(Row.TOP, 4)
            .title(Component.translatable("itemGroup.buildcraft.facades"))
            .icon(
               () -> {
                  FacadeBlockStateInfo preview = FacadeStateManager.previewState;
                  return preview != null && preview != FacadeStateManager.defaultState
                     ? BCSiliconItems.PLUG_FACADE.createItemStack(FacadeInstance.createSingle(preview, false))
                     : BCSiliconItems.PLUG_FACADE.getDefaultInstance();
               }
            )
            .displayItems((parameters, output) -> addFacadeItems(output))
            .build()
      );
   }

   private static void addFacadeItems(Output output) {
      FacadeStateManager.ensureInitialized();
      BCSiliconClient.runDeferredDedup();

      for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
         if (info.isVisible) {
            output.accept(BCSiliconItems.PLUG_FACADE.createItemStack(FacadeInstance.createSingle(info, false)));
            output.accept(BCSiliconItems.PLUG_FACADE.createItemStack(FacadeInstance.createSingle(info, true)));
         }
      }
   }
}
