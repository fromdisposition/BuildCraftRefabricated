plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.1" /* [SC] DO NOT EDIT */

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    replacements {
        // BakedQuad moved packages in 26.1
        string(current.parsed < "26.1") {
            replace(
                "net.minecraft.client.resources.model.geometry.BakedQuad",
                "net.minecraft.client.renderer.block.model.BakedQuad"
            )
        }
        // advancements.criterion split into predicates.* / triggers.* in 26.2
        string(current.parsed >= "26.1.3") {
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
    }
}

