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
    private int page = 0;
    private static final int ENTRIES_PER_PAGE = 4;

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
        int panelWidth = Math.max(1, Math.min(660, width - 16));
        int left = (width - panelWidth) / 2;
        int toggleWidth = Math.min(72, Math.max(54, panelWidth / 4));
        int toggleX = left + panelWidth - toggleWidth - 12;
        int labelWidth = Math.max(40, toggleX - (left + 14) - 12);
        int availableHeight = Math.max(100, height - 100);
        int rowHeight = Math.max(30, Math.min(42, availableHeight / ENTRIES_PER_PAGE));
        int top = 66;
        int first = page * ENTRIES_PER_PAGE;
        int last = Math.min(entries.size(), first + ENTRIES_PER_PAGE);

        for (int i = first; i < last; i++) {
            Entry entry = entries.get(i);
            int y = top + (i - first) * rowHeight;
            Button toggle = Button.builder(toggleText(entry), button -> {
                entry.set().accept(!entry.get().get());
                PerformanceConfig.save();
                button.setMessage(toggleText(entry));
            }).bounds(toggleX, y, toggleWidth, 22)
                    .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(
                            entry.description() + "  Performance impact: " + entry.impact())))
                    .build();
            addRenderableWidget(toggle);
        }

        int navY = height - 34;
        addRenderableWidget(Button.builder(Component.literal("Previous"), button -> {
            page = Math.max(0, page - 1);
            rebuildWidgets();
        }).bounds(left + 12, navY, 82, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next"), button -> {
            page = Math.min((entries.size() - 1) / ENTRIES_PER_PAGE, page + 1);
            rebuildWidgets();
        }).bounds(left + 102, navY, 82, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> minecraft.gui.setScreen(parent))
                .bounds(width - Math.min(112, panelWidth / 3) - 12, navY, Math.min(112, panelWidth / 3), 20).build());
    }

    private static Component toggleText(Entry entry) {
        return Component.literal(entry.get().get() ? "ON" : "OFF");
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.max(1, Math.min(660, width - 16));
        int left = (width - panelWidth) / 2;
        int right = left + panelWidth;
        int top = 8;
        int bottom = height - 8;
        graphics.fill(left, top, right, bottom, 0xE90B0B0B);
        graphics.fill(left, top, right, top + 3, 0xFFFFD52A);
        graphics.text(font, Component.literal("DUCKY'S OPTIMIZATIONS"), left + 14, 18, 0xFFFFD52A);
        graphics.text(font, Component.literal("Performance controls"), left + 14, 38, 0xFFAAAAAA);

        int rowHeight = Math.max(30, Math.min(42, Math.max(100, height - 100) / ENTRIES_PER_PAGE));
        int rowY = 66;
        int first = page * ENTRIES_PER_PAGE;
        int last = Math.min(entries.size(), first + ENTRIES_PER_PAGE);
        for (int i = first; i < last; i++) {
            Entry entry = entries.get(i);
            graphics.text(font, Component.literal(entry.name()), left + 14, rowY + 2, 0xFFFFFFFF);
            int impactColor = switch (entry.impact()) {
                case "HIGH" -> 0xFFFF6B45;
                case "MEDIUM" -> 0xFFFFD52A;
                default -> 0xFFB8B8B8;
            };
            graphics.text(font, Component.literal("Impact: " + entry.impact()),
                    left + 14, rowY + 14, impactColor);
            rowY += rowHeight;
        }
        graphics.centeredText(font,
                Component.literal("Page " + (page + 1) + " / " + ((entries.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE)),
                width / 2, height - 58, 0xFFAAAAAA);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        PerformanceConfig.save();
        minecraft.gui.setScreen(parent);
    }
}