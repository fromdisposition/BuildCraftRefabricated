/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics.statement;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatementParameter;

public class ActionStationRobot extends ActionStation {
   public ActionStationRobot(String tag, String descriptionKey, ISprite sprite, int maxParameters) {
      super(tag, descriptionKey, sprite, maxParameters, false);
   }

   public ActionStationRobot(String[] tags, String descriptionKey, ISprite sprite, int maxParameters) {
      super(tags, descriptionKey, sprite, maxParameters, false);
   }

   @Override
   public IStatementParameter createParameter(int index) {
      return new StatementParameterRobot();
   }
}
