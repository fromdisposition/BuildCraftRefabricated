plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.2" /* [SC] DO NOT EDIT */

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    replacements {
        // BakedQuad moved packages in 26.1
        string(current.parsed < "26.1") {
            replace(
                "net.minecraft.client.resources.model.geometry.BakedQuad",
                "net.minecraft.client.renderer.block.model.BakedQuad"
            )
            // Render-state package reorg in 26.1: 1.21.x keeps the level render states flat
            // under client.renderer.state.* and all GUI render states (incl. picture-in-picture)
            // under client.gui.render.state.*
            replace(
                "net.minecraft.client.renderer.state.level.",
                "net.minecraft.client.renderer.state."
            )
            replace(
                "net.minecraft.client.renderer.state.gui.",
                "net.minecraft.client.gui.render.state."
            )
            // Block model dispatch + model geometry/sprite were flattened in 1.21.x:
            //   block.dispatch.BlockStateModel -> block.model.BlockStateModel
            //   resources.model.geometry.QuadCollection -> resources.model.QuadCollection
            //   resources.model.sprite.Material -> resources.model.Material
            replace(
                "net.minecraft.client.renderer.block.dispatch.BlockStateModel",
                "net.minecraft.client.renderer.block.model.BlockStateModel"
            )
            replace(
                "net.minecraft.client.resources.model.geometry.QuadCollection",
                "net.minecraft.client.resources.model.QuadCollection"
            )
            // BlockAndTintGetter moved from world.level to client.renderer.block in 26.1
            replace(
                "net.minecraft.client.renderer.block.BlockAndTintGetter",
                "net.minecraft.world.level.BlockAndTintGetter"
            )
            // Fabric renamed the extended screen-handler API to "menu" in the 26.x line; 1.21.x
            // still uses screenhandler.v1.ExtendedScreenHandlerType / ExtendedScreenHandlerFactory
            // (structurally identical). The "<" guards keep the BC class ExtendedMenuTypes intact.
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
            // Fabric datagen + tooltip-callback renames (same packages, class renamed in 26.x):
            //   FabricPackOutput -> FabricDataOutput, FabricTagsProvider -> FabricTagProvider,
            //   ClientTooltipComponentCallback -> TooltipComponentCallback
            replace("FabricPackOutput", "FabricDataOutput")
            replace("FabricTagsProvider", "FabricTagProvider")
            replace("ClientTooltipComponentCallback", "TooltipComponentCallback")
            // 26.1 added net.minecraft.client.color.block.BlockTintSource (per-index colour source).
            // 1.21.x has no such type; redirect imports to BuildCraft's stand-in (the trailing ";"
            // keeps the plural BlockTintSources, used by the fluid model, untouched).
            replace(
                "net.minecraft.client.color.block.BlockTintSource;",
                "buildcraft.lib.compat.BlockTintSource;"
            )
            // 26.1 renamed LightTexture's helper to util.LightCoordsUtil; 1.21.x keeps LightTexture
            // (FQN first so the import's package is corrected before the bare-name pass).
            replace("net.minecraft.util.LightCoordsUtil", "net.minecraft.client.renderer.LightTexture")
            replace("LightCoordsUtil", "LightTexture")
            // 26.1 replaced the ClickType arg of AbstractContainerMenu.clicked with ContainerInput.
            replace("ContainerInput", "ClickType")
            // Fabric PayloadTypeRegistry: 26.x renamed playS2C/playC2S to clientboundPlay/serverboundPlay.
            replace("clientboundPlay()", "playS2C()")
            replace("serverboundPlay()", "playC2S()")
            // Model-set sprite access (26.x Context.sprites()/SpriteId vs 1.21.x Context.materials()/
            // Material) is handled with per-file conditionals — a global replace of "new Material("
            // would be reverse-applied onto legitimate 26.x new Material(...) calls and corrupt them.
            // 26.1 renamed BlockModelPart -> BlockStateModelPart; handled per-file with conditionals
            // (a global bare replace fights stonecutter's in-place reverse of the RHS token).
            // SimpleModelWrapper moved resources.model -> renderer.block.model in 26.1 (FQN-only, so
            // it rewrites imports and is reverse-safe: the 1.21.x path is absent from 26.x source).
            replace(
                "net.minecraft.client.resources.model.SimpleModelWrapper",
                "net.minecraft.client.renderer.block.model.SimpleModelWrapper"
            )
            replace(
                "net.minecraft.client.resources.model.sprite.Material",
                "net.minecraft.client.resources.model.Material"
            )
            // 26.1 split the GUI render-state recording side out of GuiGraphics into a separate
            // GuiGraphicsExtractor; 1.21.x records directly on GuiGraphics (public scissorStack /
            // guiRenderState fields). Rename the type back to GuiGraphics. Ordered so the BC
            // mixin accessor class name (GuiGraphicsExtractorAccessor) is never touched.
            replace(
                "net.minecraft.client.gui.GuiGraphicsExtractor",
                "net.minecraft.client.gui.GuiGraphics"
            )
            replace("GuiGraphicsExtractor.class", "GuiGraphics.class")
            replace("GuiGraphicsExtractor ", "GuiGraphics ")
            // 26.1 shortened several GuiGraphics drawing method names; restore the 1.21.x names
            // inside the BCGraphics wrapper (this.raw.* calls are unique to that class).
            replace("this.raw.text(", "this.raw.drawString(")
            replace("this.raw.item(", "this.raw.renderItem(")
            replace("this.raw.fakeItem(", "this.raw.renderFakeItem(")
            replace("this.raw.itemDecorations(", "this.raw.renderItemDecorations(")
        }
        // advancements.criterion split into predicates.* / triggers.* in 26.2
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
        // net.minecraft.resources.ResourceLocation was renamed to Identifier in 1.21.11 (and 26.x);
        // 1.21.10 and older still use ResourceLocation. The codebase is written against the new name,
        // so rename Identifier -> ResourceLocation only on < 1.21.11. These are delimiter-bounded so they
        // never touch Character.isJavaIdentifierStart/Part (Identifier followed by a letter). Reverse-safe:
        // "ResourceLocation" never appears natively in 1.21.11/26.x BC sources.
        string(current.parsed < "1.21.11") {
            // command argument class was renamed too (ResourceLocationArgument -> IdentifierArgument)
            replace("IdentifierArgument", "ResourceLocationArgument")
            replace("Identifier ", "ResourceLocation ")
            replace("Identifier.", "ResourceLocation.")
            replace("Identifier;", "ResourceLocation;")
            replace("Identifier>", "ResourceLocation>")
            replace("Identifier,", "ResourceLocation,")
            replace("Identifier)", "ResourceLocation)")
            replace("Identifier(", "ResourceLocation(")
            replace("Identifier[", "ResourceLocation[")
            replace("Identifier:", "ResourceLocation:")
            // RenderType moved out of the renderer.rendertype.* package (new in 1.21.11) back to
            // renderer.* on 1.21.10. Trailing ';' so it only rewrites the RenderType import, never
            // RenderTypes (which is a separate helper class handled per-file). Reverse-safe: the bare
            // renderer.RenderType import path is absent from 1.21.11/26.x.
            replace(
                "net.minecraft.client.renderer.rendertype.RenderType;",
                "net.minecraft.client.renderer.RenderType;"
            )
            // advancements.criterion (1.21.11/26.1) was spelled advancements.critereon on 1.21.10.
            // Use the same explicit FQNs as the >= 26.2 block (a bare prefix replace silently loses to
            // the more specific entries those declare for the same classes).
            replace("net.minecraft.advancements.criterion.EntityPredicate", "net.minecraft.advancements.critereon.EntityPredicate")
            replace("net.minecraft.advancements.criterion.ContextAwarePredicate", "net.minecraft.advancements.critereon.ContextAwarePredicate")
            replace("net.minecraft.advancements.criterion.SimpleCriterionTrigger", "net.minecraft.advancements.critereon.SimpleCriterionTrigger")
            // AbstractMinecart moved into vehicle.minecart.* in 1.21.11.
            replace(
                "net.minecraft.world.entity.vehicle.minecart.AbstractMinecart",
                "net.minecraft.world.entity.vehicle.AbstractMinecart"
            )
            // Util moved from net.minecraft.Util to net.minecraft.util.Util in 1.21.11.
            replace("net.minecraft.util.Util", "net.minecraft.Util")
            // Camera.getEntity() was renamed to Camera.entity() in 1.21.11 (exact-literal, reverse-safe).
            replace("camera.entity()", "camera.getEntity()")
            // DebugScreenEntryList.isF3Visible() (1.21.10) was renamed isOverlayVisible() in 1.21.11.
            replace(".isOverlayVisible()", ".isF3Visible()")
            // ARGB.black(alpha) helper is 1.21.11-only; on 1.21.10 build it via color(a, 0, 0, 0).
            replace("ARGB.black(102)", "ARGB.color(102, 0, 0, 0)")
            // Camera.getBlockPosition() was renamed to blockPosition() in 1.21.11 (exact-literal, reverse-safe).
            replace("camera.blockPosition()", "camera.getBlockPosition()")
        }
        // 1.21.1 ONLY (< 1.21.10): vanilla accessors renamed AT 1.21.2. Canonical source uses the modern
        // short names; on 1.21.1 route them back to the long forms. Each is a method-call literal that no
        // BuildCraft class defines, so it is unambiguous and reverse-safe (the long form is absent from the
        // 1.21.10/1.21.11/26.x sources). 1.21.10+ already have the short names, so this block is a no-op there.
        string(current.parsed < "1.21.10") {
            // LevelHeightAccessor.getMaxY()/getMinY() were getMaxBuildHeight()/getMinBuildHeight() on 1.21.1.
            replace(".getMaxY()", ".getMaxBuildHeight()")
            replace(".getMinY()", ".getMinBuildHeight()")
            // Minecraft.getDeltaTracker() (DeltaTracker accessor) was getTimer() on 1.21.1.
            replace(".getDeltaTracker()", ".getTimer()")
            // Direction.getUnitVec3i() (returns Vec3i) was getNormal() before 1.21.5. Both the call form
            // and the method-reference form occur; the name is Direction-unique so this is reverse-safe.
            replace("getUnitVec3i(", "getNormal(")
            replace("::getUnitVec3i", "::getNormal")
            // Camera.getPosition() (Vec3) was exposed as the record-style position() accessor at 1.21.2;
            // on 1.21.1 it is getPosition(). Var-name-bound (camera.) so it never touches entity/player position().
            replace("camera.position()", "camera.getPosition()")
            // Block.useItemOn returned ItemInteractionResult before 1.21.5 (unified into InteractionResult in
            // 1.21.5). BC blocks keep neutral logic in bcUseItemOn(...)->InteractionResult and a thin useItemOn
            // wrapper; only the wrapper's return type flips here. FQN avoids an import; reverse-safe (the
            // ItemInteractionResult form is absent from 1.21.10+/26.x sources, and bcUseItemOn never matches).
            replace("protected InteractionResult useItemOn(", "protected net.minecraft.world.ItemInteractionResult useItemOn(")
            // JigsawStructure.MaxDistance (a record wrapping an int + clamp) is 1.21.5+; on 1.21.1 the
            // jigsaw max-distance was a plain int (Codec.intRange(1,128)) and JigsawPlacement.addPieces took
            // an int. Both BC structures pass the field straight through, so only the codec field and the
            // type token differ. The .CODEC.fieldOf form is replaced first (more specific), then the bare
            // type token (matches the field decl, ctor param, and the getter's return type). Both literals
            // occur only in OilDepositStructure/WaterSpringStructure, so they are unambiguous and reverse-safe.
            replace(
                "JigsawStructure.MaxDistance.CODEC.fieldOf(\"max_distance_from_center\")",
                "Codec.intRange(1, 128).fieldOf(\"max_distance_from_center\")"
            )
            replace("JigsawStructure.MaxDistance maxDistanceFromCenter", "int maxDistanceFromCenter")
            // Fabric FabricTagProvider.builder(TagKey) was getOrCreateTagBuilder(TagKey) before the 26.x-era API.
            // Scoped to the BC tag-provider call sites (BCEnergyBiomeTags is unique to that file), reverse-safe.
            replace("builder(BCEnergyBiomeTags.", "getOrCreateTagBuilder(BCEnergyBiomeTags.")
            // JEI's recipe-type holder was the concrete class mezz.jei.api.recipe.RecipeType in JEI 19
            // (1.21.1); it became the interface mezz.jei.api.recipe.types.IRecipeType in newer JEI (26.x+).
            // Both expose the same static create(namespace, path, class) factory, so the BC jei integration is
            // written against the modern FQN and routed back here. The jei files fully-qualify every use (no
            // import), so this single FQN replace covers them all; reverse-safe (the RHS FQN is absent from
            // 26.x sources, and vanilla net.minecraft...RecipeType is never matched by this package prefix).
            replace("mezz.jei.api.recipe.types.IRecipeType", "mezz.jei.api.recipe.RecipeType")
            // JEI's recipe-catalyst registration was IRecipeCatalystRegistration.addCraftingStation(RecipeType,
            // ItemLike...) in newer JEI (26.x+); JEI 19 (1.21.1) spells the same thing addRecipeCatalysts(RecipeType,
            // ItemLike...). The BC plugins call addCraftingStation; route it back here. Reverse-safe: addRecipeCatalysts
            // is absent from 26.x BC sources (they use addCraftingStation exclusively).
            replace("addCraftingStation(", "addRecipeCatalysts(")
        }
    }
}

