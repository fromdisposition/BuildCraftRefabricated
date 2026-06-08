/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.ledger;

import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import com.mojang.authlib.GameProfile;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.ClientAsset.Texture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

public class LedgerOwnership extends Ledger_Neptune {
   private static final int COLOUR = 14741759;
   private static final Identifier STEVE_SKIN = Identifier.parse("textures/entity/player/wide/steve.png");
   private final Supplier<GameProfile> ownerSupplier;

   public LedgerOwnership(BuildCraftGui gui, Supplier<GameProfile> ownerSupplier, boolean expandPositive) {
      super(gui, 14741759, expandPositive);
      this.ownerSupplier = ownerSupplier;
      this.title = "gui.owner";
      this.appendText(() -> {
         GameProfile profile = ownerSupplier.get();
         return profile != null ? profile.name() : "Unknown";
      }, 0);
      this.calculateMaxSize();
   }

   @Override
   protected void drawIcon(double x, double y, BCGraphics graphics) {
      Identifier skinTexture = getSkinTexture(this.ownerSupplier.get());
      graphics.blit(RenderPipelines.GUI_TEXTURED, skinTexture, (int)x, (int)y, 8.0F, 8.0F, 16, 16, 8, 8, 64, 64);
      graphics.blit(RenderPipelines.GUI_TEXTURED, skinTexture, (int)x, (int)y, 40.0F, 8.0F, 16, 16, 8, 8, 64, 64);
   }

   private static Identifier getSkinTexture(GameProfile profile) {
      if (profile != null && profile.id() != null) {
         try {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
               PlayerInfo info = connection.getPlayerInfo(profile.id());
               if (info != null) {
                  PlayerSkin skin = info.getSkin();
                  Texture bodyTex = skin.body();
                  if (bodyTex != null) {
                     return bodyTex.id();
                  }
               }
            }
         } catch (Exception var5) {
         }

         return STEVE_SKIN;
      } else {
         return STEVE_SKIN;
      }
   }
}
