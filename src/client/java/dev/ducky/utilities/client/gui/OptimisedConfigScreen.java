package dev.ducky.utilities.client.gui;

import dev.ducky.utilities.client.config.PerformanceConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class OptimisedConfigScreen {
    private OptimisedConfigScreen() {}

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Ducky's MC Utilities"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Entity Rendering"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Entity culling"))
                                .description(OptionDescription.of(Component.literal("Skip rendering entities beyond the selected distance.")))
                                .binding(true, () -> PerformanceConfig.entityCulling, v -> { PerformanceConfig.entityCulling = v; PerformanceConfig.save(); })
                                .controller(BooleanControllerBuilder::create).build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Entity render distance"))
                                .description(OptionDescription.of(Component.literal("Maximum entity render distance in blocks.")))
                                .binding(96.0, () -> PerformanceConfig.maxEntityDistance, v -> { PerformanceConfig.maxEntityDistance = v; PerformanceConfig.save(); })
                                .controller(o -> DoubleSliderControllerBuilder.create(o).range(16.0, 512.0).step(8.0)
                                        .valueFormatter(v -> Component.literal(Math.round(v) + " blocks"))).build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Block Entity Rendering"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Block entity culling"))
                                .description(OptionDescription.of(Component.literal("Skip distant block entities; globally rendered and off-screen renderers are preserved.")))
                                .binding(true, () -> PerformanceConfig.blockEntityCulling, v -> { PerformanceConfig.blockEntityCulling = v; PerformanceConfig.save(); })
                                .controller(BooleanControllerBuilder::create).build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Block entity render distance"))
                                .description(OptionDescription.of(Component.literal("Maximum block entity render distance in blocks.")))
                                .binding(64.0, () -> PerformanceConfig.maxBlockEntityDistance, v -> { PerformanceConfig.maxBlockEntityDistance = v; PerformanceConfig.save(); })
                                .controller(o -> DoubleSliderControllerBuilder.create(o).range(16.0, 256.0).step(8.0)
                                        .valueFormatter(v -> Component.literal(Math.round(v) + " blocks"))).build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Dropped Items"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Dropped item culling"))
                                .description(OptionDescription.of(Component.literal("Skip rendering dropped items beyond the selected distance.")))
                                .binding(true, () -> PerformanceConfig.itemEntityCulling, v -> { PerformanceConfig.itemEntityCulling = v; PerformanceConfig.save(); })
                                .controller(BooleanControllerBuilder::create).build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.literal("Dropped item render distance"))
                                .description(OptionDescription.of(Component.literal("Maximum dropped-item render distance in blocks.")))
                                .binding(48.0, () -> PerformanceConfig.maxItemEntityDistance, v -> { PerformanceConfig.maxItemEntityDistance = v; PerformanceConfig.save(); })
                                .controller(o -> DoubleSliderControllerBuilder.create(o).range(16.0, 256.0).step(8.0)
                                        .valueFormatter(v -> Component.literal(Math.round(v) + " blocks"))).build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("World Performance"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Chunk update optimization"))
                                .description(OptionDescription.of(Component.literal("Use vanilla's non-prioritized chunk rebuild mode to avoid forcing synchronous rebuilds for nearby sections.")))
                                .binding(true, () -> PerformanceConfig.chunkUpdateOptimization, v -> { PerformanceConfig.chunkUpdateOptimization = v; PerformanceConfig.save(); })
                                .controller(BooleanControllerBuilder::create).build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Chunk loading optimization"))
                                .description(OptionDescription.of(Component.literal("Disable chunk section fade-in animations to reduce extra rendering work when chunks appear.")))
                                .binding(true, () -> PerformanceConfig.chunkLoadingOptimization, v -> { PerformanceConfig.chunkLoadingOptimization = v; PerformanceConfig.save(); })
                                .controller(BooleanControllerBuilder::create).build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Block update optimization"))
                                .description(OptionDescription.of(Component.literal("Skip chunk rerender checks when a block's old and new states are identical.")))
                                .binding(true, () -> PerformanceConfig.blockUpdateOptimization, v -> { PerformanceConfig.blockUpdateOptimization = v; PerformanceConfig.save(); })
                                .controller(BooleanControllerBuilder::create).build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Lighting optimization"))
                                .description(OptionDescription.of(Component.literal("Disable ambient occlusion to reduce block shading work. This changes visual shading, not world light levels.")))
                                .binding(true, () -> PerformanceConfig.lightingOptimization, v -> { PerformanceConfig.lightingOptimization = v; PerformanceConfig.save(); })
                                .controller(BooleanControllerBuilder::create).build())
                        .build())
                .build().generateScreen(parent);
    }
}
