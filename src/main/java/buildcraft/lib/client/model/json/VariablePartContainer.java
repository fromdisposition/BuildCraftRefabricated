/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model.json;

import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.model.ResourceLoaderContext;
import buildcraft.lib.expression.FunctionContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.List;

public class VariablePartContainer extends JsonVariableModelPart {
   public final JsonVariableModel model;

   public VariablePartContainer(JsonObject obj, FunctionContext fnCtx, ResourceLoaderContext ctx) {
      if (obj.has("textures")) {
         throw new JsonSyntaxException("Contained variable parts must not have 'textures'");
      }

      if (obj.has("variables")) {
         throw new JsonSyntaxException("Contained variable parts must not have 'variables'");
      }

      if (obj.has("translucent")) {
         throw new JsonSyntaxException("Contained variable parts must not have 'translucent'");
      }

      this.model = new JsonVariableModel(obj, fnCtx, ctx);
   }

   @Override
   public void addQuads(List<MutableQuad> to, JsonVariableModel.ITextureGetter spriteLookup) {
      Collections.addAll(to, this.model.bakePart(this.model.cutoutElements, spriteLookup));
   }
}
