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
import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli;

public class ActionExtractionPreset extends BCStatement implements IActionInternal {
   public final PipeBehaviourEmzuli.SlotIndex index;

   public ActionExtractionPreset(PipeBehaviourEmzuli.SlotIndex index) {
      super("buildcraft:extraction.preset." + index.colour.getName(), "buildcraft.extraction.preset." + index.colour.getName());
      this.index = index;
   }

   @Override
   public String getDescription() {
      return String.format(LocaleUtil.localize("gate.action.extraction"), ColourUtil.getTextFullTooltip(this.index.colour));
   }

   @Override
   public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
   }

   @Override
   public IStatement[] getPossible() {
      return BCTransportStatements.ACTION_EXTRACTION_PRESET;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCTransportSprites.ACTION_EXTRACTION_PRESET.get(this.index);
   }
}
