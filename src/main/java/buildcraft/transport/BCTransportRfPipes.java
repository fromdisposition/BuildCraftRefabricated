package buildcraft.transport;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeFlowType;
import buildcraft.transport.pipe.behaviour.PipeBehaviourCobble;
import buildcraft.transport.pipe.behaviour.PipeBehaviourGold;
import buildcraft.transport.pipe.behaviour.PipeBehaviourLimiter;
import buildcraft.transport.pipe.behaviour.PipeBehaviourQuartz;
import buildcraft.transport.pipe.behaviour.PipeBehaviourSandstone;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStone;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraft.transport.pipe.behaviour.PipeBehaviourWoodPower;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;

public final class BCTransportRfPipes {
   private BCTransportRfPipes() {}

   /** Called only when team_reborn_energy is loaded. Registers flowRf and all RF pipe definitions. */
   public static void preInit() {
      PipeApi.flowRf = new PipeFlowType(PipeFlowRedstoneFlux::new, PipeFlowRedstoneFlux::new);
      BCTransportPipes.DefinitionBuilder builder = new BCTransportPipes.DefinitionBuilder();

      // wood_rf — same logic/tex state as woodPower
      builder.logic(PipeBehaviourWoodPower::new, PipeBehaviourWoodPower::new).texSuffixes("_clear", "_filled");
      builder.builder.itemTex(0, 0, 1);
      BCTransportPipes.woodRf = builder.idTexPrefix("wood_rf").flowRf().define();

      // simple variants — idTex resets texSuffixes to {""}
      builder.builder.itemTex(0);
      builder.logic(PipeBehaviourStone::new, PipeBehaviourStone::new);
      BCTransportPipes.stoneRf = builder.idTex("stone_rf").flowRf().define();

      builder.logic(PipeBehaviourCobble::new, PipeBehaviourCobble::new);
      BCTransportPipes.cobbleRf = builder.idTex("cobblestone_rf").flowRf().define();

      builder.logic(PipeBehaviourQuartz::new, PipeBehaviourQuartz::new);
      BCTransportPipes.quartzRf = builder.idTex("quartz_rf").flowRf().define();

      builder.logic(PipeBehaviourGold::new, PipeBehaviourGold::new);
      BCTransportPipes.goldRf = builder.idTex("gold_rf").flowRf().define();

      builder.logic(PipeBehaviourSandstone::new, PipeBehaviourSandstone::new);
      BCTransportPipes.sandstoneRf = builder.idTex("sandstone_rf").flowRf().define();

      // iron_rf / diamond_rf — Limiter logic, multi-texture, itemTex(6)
      builder.logic(PipeBehaviourLimiter::new, PipeBehaviourLimiter::new).flowRf();
      builder.texSuffixes("_m0", "_m4", "_m8", "_m16", "_m32", "_m64", "_m128");
      builder.builder.itemTex(6);
      BCTransportPipes.ironRf = builder.idTexPrefix("iron_rf").define();
      BCTransportPipes.diamondRf = builder.idTexPrefix("diamond_rf").define();

      // diamond_wood_rf — WoodPower logic, "_clear"/"_filled" tex
      builder.builder.itemTex(0);
      builder.logic(PipeBehaviourWoodDiamond::new, PipeBehaviourWoodDiamond::new).texSuffixes("_clear", "_filled");
      builder.builder.itemTex(0, 0, 1);
      builder.logic(PipeBehaviourWoodPower::new, PipeBehaviourWoodPower::new);
      BCTransportPipes.diaWoodRf = builder.idTexPrefix("diamond_wood_rf").flowRf().define();

      registerItems();
   }

   private static void registerItems() {
      BCTransportItems.PIPE_WOOD_RF = BCTransportItems.registerPipeItem("pipe_wood_rf", () -> BCTransportPipes.woodRf);
      BCTransportItems.PIPE_COBBLE_RF = BCTransportItems.registerPipeItem("pipe_cobble_rf", () -> BCTransportPipes.cobbleRf);
      BCTransportItems.PIPE_STONE_RF = BCTransportItems.registerPipeItem("pipe_stone_rf", () -> BCTransportPipes.stoneRf);
      BCTransportItems.PIPE_QUARTZ_RF = BCTransportItems.registerPipeItem("pipe_quartz_rf", () -> BCTransportPipes.quartzRf);
      BCTransportItems.PIPE_IRON_RF = BCTransportItems.registerPipeItem("pipe_iron_rf", () -> BCTransportPipes.ironRf);
      BCTransportItems.PIPE_GOLD_RF = BCTransportItems.registerPipeItem("pipe_gold_rf", () -> BCTransportPipes.goldRf);
      BCTransportItems.PIPE_SANDSTONE_RF = BCTransportItems.registerPipeItem("pipe_sandstone_rf", () -> BCTransportPipes.sandstoneRf);
      BCTransportItems.PIPE_DIAMOND_RF = BCTransportItems.registerPipeItem("pipe_diamond_rf", () -> BCTransportPipes.diamondRf);
      BCTransportItems.PIPE_DIAMOND_WOOD_RF = BCTransportItems.registerPipeItem("pipe_diamond_wood_rf", () -> BCTransportPipes.diaWoodRf);
   }
}
