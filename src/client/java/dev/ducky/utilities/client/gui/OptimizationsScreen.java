package dev.ducky.utilities.client.gui;

import dev.ducky.utilities.client.config.PerformanceConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class OptimizationsScreen extends Screen {
    private final Screen parent;
    private final List<Entry> entries = new ArrayList<>();

    private record Entry(String name, String impact, String description,
                         Supplier<Boolean> get, Consumer<Boolean> set) {}

    public OptimizationsScreen(Screen parent) {
        super(Component.literal("Ducky's Optimizations"));
        this.parent = parent;
        entries.add(new Entry("Entity culling", "HIGH", "Stops rendering entities beyond the configured distance. Distant mobs and other entities will not be visible.",
                () -> PerformanceConfig.entityCulling, v -> PerformanceConfig.entityCulling = v));
        entries.add(new Entry("Block entity culling", "MEDIUM", "Stops rendering distant block entities such as chests and signs. Global and off-screen renderers are preserved.",
                () -> PerformanceConfig.blockEntityCulling, v -> PerformanceConfig.blockEntityCulling = v));
        entries.add(new Entry("Dropped item culling", "LOW", "Stops rendering dropped item entities beyond the configured item distance.",
                () -> PerformanceConfig.itemEntityCulling, v -> PerformanceConfig.itemEntityCulling = v));
        entries.add(new Entry("Living entity culling", "MEDIUM", "Stops rendering living entities beyond a shorter distance. Distant mobs and players may disappear sooner.",
                () -> PerformanceConfig.livingEntityCulling, v -> PerformanceConfig.livingEntityCulling = v));
        entries.add(new Entry("Projectile culling", "LOW", "Stops rendering distant projectiles such as arrows. Projectiles may disappear before they reach the edge of your view.",
                () -> PerformanceConfig.projectileCulling, v -> PerformanceConfig.projectileCulling = v));
        entries.add(new Entry("Experience orb culling", "LOW", "Stops rendering distant experience orbs. This only changes client rendering.",
                () -> PerformanceConfig.experienceOrbCulling, v -> PerformanceConfig.experienceOrbCulling = v));
        entries.add(new Entry("Chunk update optimization", "MEDIUM", "Uses the non-prioritized chunk rebuild mode. Chunk updates may feel less immediate in some situations.",
                () -> PerformanceConfig.chunkUpdateOptimization, v -> PerformanceConfig.chunkUpdateOptimization = v));
        entries.add(new Entry("Chunk fade removal", "LOW", "Removes the fade-in animation when chunk sections appear. Chunks pop in instead of fading.",
                () -> PerformanceConfig.chunkLoadingOptimization, v -> PerformanceConfig.chunkLoadingOptimization = v));
        entries.add(new Entry("Ambient occlusion reduction", "LOW", "Disables ambient occlusion. Block corners and surfaces look flatter and less shaded. OFF by default.",
                () -> PerformanceConfig.lightingOptimization, v -> PerformanceConfig.lightingOptimization = v));
    }

    @Override
    protected void init() {
        clearWidgets();
        int rowHeight = 38;
        int top = 66;
        int labelX = Math.max(12, width / 2 - 300);
        int buttonX = Math.min(width - 92, width / 2 + 220);
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            int y = top + i * rowHeight;
            Button toggle = Button.builder(toggleText(entry), button -> {
                entry.set().accept(!entry.get().get());
                PerformanceConfig.save();
                button.setMessage(toggleText(entry));
            }).bounds(buttonX, y, 78, 22)
                    .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(
                            entry.description() + "  Performance impact: " + entry.impact())))
                    .build();
            addRenderableWidget(toggle);
        }
        int doneWidth = 100;
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> minecraft.gui.setScreen(parent))
                .bounds(width - doneWidth - 14, height - 32, doneWidth, 20).build());
    }

    private static Component toggleText(Entry entry) {
        return Component.literal(entry.get().get() ? "ON" : "OFF");
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int panelWidth = Math.min(660, width - 20);
        int left = (width - panelWidth) / 2;
        int right = left + panelWidth;
        int top = 8;
        int bottom = height - 40;
        graphics.fill(left, top, right, bottom, 0xE90B0B0B);
        graphics.fill(left, top, right, top + 3, 0xFFFFD52A);
        graphics.text(font, Component.literal("DUCKY'S OPTIMIZATIONS"), left + 14, 18, 0xFFFFD52A);
        graphics.drawString(font, Component.literal("Performance controls"), left + 14, 38, 0xFFAAAAAA);

        int labelX = left + 14;
        int rowY = 66;
        for (Entry entry : entries) {
            graphics.drawString(font, Component.literal(entry.name()), labelX, rowY + 2, 0xFFFFFFFF);
            int impactColor = switch (entry.impact()) {
                case "HIGH" -> 0xFFFF6B45;
                case "MEDIUM" -> 0xFFFFD52A;
                default -> 0xFFB8B8B8;
            };
            graphics.drawString(font, Component.literal("Performance impact: " + entry.impact()),
                    labelX, rowY + 15, impactColor);
            rowY += 38;
        }
        graphics.drawString(font, Component.literal("Hover over an option for its description."),
                left + 14, bottom - 18, 0xFFAAAAAA, false);
        
    }

    @Override
    public void onClose() {
        PerformanceConfig.save();
        minecraft.setScreen(parent);
    }
}
