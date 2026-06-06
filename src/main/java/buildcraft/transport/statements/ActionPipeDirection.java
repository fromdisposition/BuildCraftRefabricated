package buildcraft.transport.statements;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.transport.BCTransportSprites;
import buildcraft.transport.BCTransportStatements;
import java.util.Locale;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class ActionPipeDirection extends BCStatement implements IActionInternal {
   public final Direction direction;

   public ActionPipeDirection(Direction direction) {
      super("buildcraft:pipe.dir." + direction.name().toLowerCase(Locale.ROOT), "buildcraft.pipe.dir." + direction.name().toLowerCase(Locale.ROOT));
      this.direction = direction;
   }

   @Override
   public String getDescription() {
      return String.format(LocaleUtil.localize("gate.action.pipe.direction"), ColourUtil.getTextFullTooltip(this.direction));
   }

   @Override
   public IStatement rotateLeft() {
      Direction face = this.direction.getAxis() == Axis.Y ? this.direction : this.direction.getClockWise();
      return BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()];
   }

   @Override
   public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
   }

   @Override
   public String toString() {
      return "ActionPipeDirection[" + this.direction + "]";
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCTransportSprites.getPipeDirection(this.direction);
   }

   @Override
   public IStatement[] getPossible() {
      return BCTransportStatements.ACTION_PIPE_DIRECTION;
   }
}
