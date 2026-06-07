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
