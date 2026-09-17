/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.statement;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;

public class ActionStation extends BCStatement implements IActionInternal {
   private final String descriptionKey;
   private final ISprite sprite;
   private final int maxParameters;
   private final boolean itemParameters;

   public ActionStation(String tag, String descriptionKey, ISprite sprite, int maxParameters, boolean itemParameters) {
      super(tag);
      this.descriptionKey = descriptionKey;
      this.sprite = sprite;
      this.maxParameters = maxParameters;
      this.itemParameters = itemParameters;
   }

   public ActionStation(String[] tags, String descriptionKey, ISprite sprite, int maxParameters, boolean itemParameters) {
      super(tags);
      this.descriptionKey = descriptionKey;
      this.sprite = sprite;
      this.maxParameters = maxParameters;
      this.itemParameters = itemParameters;
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize(this.descriptionKey);
   }

   @Override
   public int maxParameters() {
      return this.maxParameters;
   }

   @Override
   public IStatementParameter createParameter(int index) {
      return this.itemParameters ? new StatementParameterItemStack() : null;
   }

   @Override
   public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
   }

   @Override
   public ISprite getSprite() {
      return this.sprite;
   }
}
