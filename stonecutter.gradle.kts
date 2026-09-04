plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3" /* [SC] DO NOT EDIT */

stonecutter parameters {
    replacements {
        // Canonical source uses the newest node's names. Rules apply in both directions, so a rule's older name
        // must never occur natively in canonical source.
        string(current.parsed < "26.1") {
            replace("net.minecraft.util.valueproviders.IntProviders.codec(", "net.minecraft.util.valueproviders.IntProvider.codec(")
            replace("net.minecraft.client.renderer.block.dispatch.BlockStateModelPart", "net.minecraft.client.renderer.block.model.BlockModelPart")
            replace("BlockStateModelPart", "BlockModelPart")
            replace(
                "net.minecraft.client.resources.model.geometry.BakedQuad",
                "net.minecraft.client.renderer.block.model.BakedQuad"
            )
            replace(
                "net.minecraft.client.renderer.state.level.",
                "net.minecraft.client.renderer.state."
            )
            replace(
                "net.minecraft.client.renderer.state.gui.",
                "net.minecraft.client.gui.render.state."
            )
            replace(
                "net.minecraft.client.renderer.block.dispatch.BlockStateModel",
                "net.minecraft.client.renderer.block.model.BlockStateModel"
            )
            replace(
                "net.minecraft.client.resources.model.geometry.QuadCollection",
                "net.minecraft.client.resources.model.QuadCollection"
            )
            replace(
                "net.minecraft.client.renderer.block.BlockAndTintGetter",
                "net.minecraft.world.level.BlockAndTintGetter"
            )
            // The trailing "<" keeps the BC class ExtendedMenuTypes out of the bare renames.
            replace(
                "net.fabricmc.fabric.api.menu.v1.ExtendedMenuType",
                "net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType"
            )
            replace(
                "net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider",
                "net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory"
            )
            replace("ExtendedMenuType<", "ExtendedScreenHandlerType<")
            replace("ExtendedMenuProvider<", "ExtendedScreenHandlerFactory<")
            replace("FabricPackOutput", "FabricDataOutput")
            replace("FabricTagsProvider", "FabricTagProvider")
            replace("ClientTooltipComponentCallback", "TooltipComponentCallback")
            // 1.21.x has no BlockTintSource; the trailing ";" keeps the plural BlockTintSources untouched.
            replace(
                "net.minecraft.client.color.block.BlockTintSource;",
                "buildcraft.lib.compat.BlockTintSource;"
            )
            // FQN rule needed too: the bare rename alone leaves the import at net.minecraft.util.LightTexture.
            replace("net.minecraft.util.LightCoordsUtil", "net.minecraft.client.renderer.LightTexture")
            replace("LightCoordsUtil", "LightTexture")
            replace("ContainerInput", "ClickType")
            replace("clientboundPlay()", "playS2C()")
            replace("serverboundPlay()", "playC2S()")
            replace(
                "net.minecraft.client.resources.model.SimpleModelWrapper",
                "net.minecraft.client.renderer.block.model.SimpleModelWrapper"
            )
            replace(
                "net.minecraft.client.resources.model.sprite.Material",
                "net.minecraft.client.resources.model.Material"
            )
            // Delimited forms only, so the BC mixin accessor GuiGraphicsExtractorAccessor stays untouched.
            replace(
                "net.minecraft.client.gui.GuiGraphicsExtractor",
                "net.minecraft.client.gui.GuiGraphics"
            )
            replace("GuiGraphicsExtractor.class", "GuiGraphics.class")
            replace("GuiGraphicsExtractor ", "GuiGraphics ")
            // Scoped by "this.raw.", which occurs only in the BCGraphics wrapper.
            replace("this.raw.text(", "this.raw.drawString(")
            replace("this.raw.item(", "this.raw.renderItem(")
            replace("this.raw.fakeItem(", "this.raw.renderFakeItem(")
            replace("this.raw.itemDecorations(", "this.raw.renderItemDecorations(")
        }
        string(current.parsed < "26.3-pre-1") {
            replace("com.mojang.renderpearl.api.pipeline.RenderPipeline", "com.mojang.blaze3d.pipeline.RenderPipeline")
            replace("com.mojang.renderpearl.api.pipeline.DepthStencilState", "com.mojang.blaze3d.pipeline.DepthStencilState")
            replace("com.mojang.renderpearl.api.buffers.GpuBuffer", "com.mojang.blaze3d.buffers.GpuBuffer")
            replace("com.mojang.renderpearl.api.device.GpuDevice", "com.mojang.blaze3d.systems.GpuDevice")
            replace("rotate(Axis.", "mulPose(Axis.")
            replace(
                "player.swing(hand, player.getItemInHand(hand).getOrDefault(net.minecraft.core.component.DataComponents.INTERACT_ANIMATION, net.minecraft.world.item.component.SwingAnimation.DEFAULT), false);",
                "player.swing(hand);"
            )
            replace(
                "player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);",
                "player.drop(stack, false);"
            )
            replace(
                "player.drop(modified, false, net.minecraft.util.Prediction.SERVER_ONLY);",
                "player.drop(modified, false);"
            )
            replace(
                "net.minecraft.util.context.ContextMap.builder().buildAndValidate(SlotDisplayContext.CONTEXT)",
                "new Builder().create(SlotDisplayContext.CONTEXT)"
            )
            replace(
                "layer.setQuads(net.minecraft.client.resources.model.geometry.ItemQuads.split(quads));",
                "layer.prepareQuadList().addAll(quads);"
            )
            replace(
                "overlayLayer.setQuads(net.minecraft.client.resources.model.geometry.ItemQuads.split(overlayQuads));",
                "overlayLayer.prepareQuadList().addAll(overlayQuads);"
            )
            replace("PushReaction.POPPED", "PushReaction.DESTROY")
            replace(
                "net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement.FrequencyReductionMethod",
                "net.minecraft.world.level.levelgen.structure.placement.StructurePlacement.FrequencyReductionMethod"
            )
            replace(
                "emitter.shadeDirectionOverride(net.minecraft.core.Direction.UP);",
                "emitter.diffuseShade(false);"
            )
        }
        string(current.parsed >= "26.2") {
            replace(
                "net.minecraft.advancements.criterion.EntityPredicate",
                "net.minecraft.advancements.predicates.entity.EntityPredicate"
            )
            replace(
                "net.minecraft.advancements.criterion.ContextAwarePredicate",
                "net.minecraft.advancements.predicates.ContextAwarePredicate"
            )
            replace(
                "net.minecraft.advancements.criterion.SimpleCriterionTrigger",
                "net.minecraft.advancements.triggers.SimpleCriterionTrigger"
            )
        }
        string(current.parsed < "1.21.11") {
            replace("IdentifierArgument", "ResourceLocationArgument")
            // Identity rule: the longer match wins, shielding REI's CategoryIdentifier from the renames below.
            replace("CategoryIdentifier", "CategoryIdentifier")
            // Delimiter-bounded so Character.isJavaIdentifierStart/Part are never touched.
            replace("Identifier ", "ResourceLocation ")
            replace("Identifier.", "ResourceLocation.")
            replace("Identifier;", "ResourceLocation;")
            replace("Identifier>", "ResourceLocation>")
            replace("Identifier,", "ResourceLocation,")
            replace("Identifier)", "ResourceLocation)")
            replace("Identifier(", "ResourceLocation(")
            replace("Identifier[", "ResourceLocation[")
            replace("Identifier:", "ResourceLocation:")
            // Trailing ";" so the separate RenderTypes helper is never rewritten.
            replace(
                "net.minecraft.client.renderer.rendertype.RenderType;",
                "net.minecraft.client.renderer.RenderType;"
            )
            // Explicit FQNs: a bare package-prefix rule silently loses to the >= 26.2 block's more specific entries.
            replace("net.minecraft.advancements.criterion.EntityPredicate", "net.minecraft.advancements.critereon.EntityPredicate")
            replace("net.minecraft.advancements.criterion.ContextAwarePredicate", "net.minecraft.advancements.critereon.ContextAwarePredicate")
            replace("net.minecraft.advancements.criterion.SimpleCriterionTrigger", "net.minecraft.advancements.critereon.SimpleCriterionTrigger")
            replace(
                "net.minecraft.world.entity.vehicle.minecart.AbstractMinecart",
                "net.minecraft.world.entity.vehicle.AbstractMinecart"
            )
            replace("net.minecraft.util.Util", "net.minecraft.Util")
            replace("camera.entity()", "camera.getEntity()")
            replace(".isOverlayVisible()", ".isF3Visible()")
            replace("ARGB.black(102)", "ARGB.color(102, 0, 0, 0)")
            replace("camera.blockPosition()", "camera.getBlockPosition()")
        }
        // Real boundaries vary (1.21.2 to 1.21.5 vanilla, later for Fabric and JEI); 1.21.10 is only the next node.
        string(current.parsed < "1.21.10") {
            // Identity rule shields REI's Rectangle.getMaxY() from the rename below.
            replace("bounds.getMaxY()", "bounds.getMaxY()")
            replace(".getMaxY()", ".getMaxBuildHeight()")
            replace(".getMinY()", ".getMinBuildHeight()")
            replace(".getDeltaTracker()", ".getTimer()")
            replace("getUnitVec3i(", "getNormal(")
            replace("::getUnitVec3i", "::getNormal")
            replace("camera.position()", "camera.getPosition()")
            // Only the thin wrapper flips; block logic sits in bcUseItemOn and returns InteractionResult on every node.
            replace("protected InteractionResult useItemOn(", "protected net.minecraft.world.ItemInteractionResult useItemOn(")
            replace(
                "JigsawStructure.MaxDistance.CODEC.fieldOf(\"max_distance_from_center\")",
                "Codec.intRange(1, 128).fieldOf(\"max_distance_from_center\")"
            )
            replace("JigsawStructure.MaxDistance maxDistanceFromCenter", "int maxDistanceFromCenter")
            replace("builder(BCEnergyBiomeTags.", "getOrCreateTagBuilder(BCEnergyBiomeTags.")
            replace("builder(BCEnergyStructures.", "getOrCreateTagBuilder(BCEnergyStructures.")
            replace("builder(TagKey.create(", "getOrCreateTagBuilder(TagKey.create(")
            // The jei files fully-qualify every use, so this one FQN rule covers them all; an import would escape it.
            replace("mezz.jei.api.recipe.types.IRecipeType", "mezz.jei.api.recipe.RecipeType")
            replace("addCraftingStation(", "addRecipeCatalysts(")
        }
    }
}

