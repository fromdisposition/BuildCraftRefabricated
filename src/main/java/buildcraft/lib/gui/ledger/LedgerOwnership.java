/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.ledger;

import buildcraft.lib.nbt.BcAuth;
import buildcraft.lib.gui.BCGraphics;
import buildcraft.lib.gui.BuildCraftGui;
import com.mojang.authlib.GameProfile;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
//? if >= 1.21.10 {
import net.minecraft.core.ClientAsset.Texture;
//?}
import net.minecraft.resources.Identifier;
//? if >= 1.21.10 {
import net.minecraft.world.entity.player.PlayerSkin;
//?} else {
/*import net.minecraft.client.resources.PlayerSkin;
*///?}

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
         return profile != null ? BcAuth.name(profile) : "Unknown";
      }, 0);
      this.calculateMaxSize();
   }

   @Override
   protected void drawIcon(double x, double y, BCGraphics graphics) {
      Identifier skinTexture = getSkinTexture(this.ownerSupplier.get());
      //? if < 1.21.10 {
      /*// Draw the face + hat through the canonical vanilla helper (graphics.raw is GuiGraphicsExtractor on 1.21.1); it
      // sets up the player-skin layout/blend exactly like vanilla player heads, avoiding the manual-blit edge
      // cases that mis-rendered the owner head on 1.21.1.
      net.minecraft.client.gui.components.PlayerFaceRenderer.draw(graphics.raw, skinTexture, (int) x, (int) y, 16);
      *///?} else {
      // The 8x8 face region (UV 8,8) and the 8x8 hat overlay (UV 40,8) from the 64x64 skin, scaled to 16x16.
      graphics.blit(skinTexture, (int)x, (int)y, 8.0F, 8.0F, 16, 16, 8, 8, 64, 64);
      graphics.blit(skinTexture, (int)x, (int)y, 40.0F, 8.0F, 16, 16, 8, 8, 64, 64);
      //?}
   }

   private static Identifier getSkinTexture(GameProfile profile) {
      if (profile != null && BcAuth.id(profile) != null) {
         try {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
               PlayerInfo info = connection.getPlayerInfo(BcAuth.id(profile));
               if (info != null) {
                  PlayerSkin skin = info.getSkin();
                  //? if >= 1.21.10 {
                  Texture bodyTex = skin.body();
                  if (bodyTex != null) {
                     return bodyTex.id();
                  }
                  //?} else {
                  /*Identifier tex = skin.texture();
                  if (tex != null) {
                     return tex;
                  }
                  *///?}
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
