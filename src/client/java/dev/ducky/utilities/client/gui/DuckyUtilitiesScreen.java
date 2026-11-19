package dev.ducky.utilities.client.gui;

import dev.ducky.utilities.client.config.PerformanceConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class DuckyUtilitiesScreen extends Screen {
    private final Screen parent;

    public DuckyUtilitiesScreen(Screen parent) {
        super(Component.literal("Ducky's Utilities"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();

        int panelWidth = Math.max(1, Math.min(660, width - 16));
        int left = (width - panelWidth) / 2;
        int buttonWidth = Math.min(360, panelWidth - 28);
        int buttonX = (width - buttonWidth) / 2;

        int footerY = height - 34;
        int contentTop = 72;
        int available = Math.max(66, footerY - contentTop - 12);
        int gap = Math.min(10, Math.max(4, available / 12));
        int buttonHeight = Math.max(20, Math.min(26, (available - gap * 2) / 3));

        addRenderableWidget(Button.builder(toolText(), button -> {
            PerformanceConfig.adaptiveToolSwitching = !PerformanceConfig.adaptiveToolSwitching;
            PerformanceConfig.save();
            button.setMessage(toolText());
        }).bounds(buttonX, contentTop, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.literal(
                        "Automatically select a matching tool from your hotbar when looking at a block or living entity. Only activates while holding a supported tool.")))
                .build());

        int sortY = contentTop + buttonHeight + gap;
        addRenderableWidget(Button.builder(sortText(), button -> {
            PerformanceConfig.sortingEnabled = !PerformanceConfig.sortingEnabled;
            PerformanceConfig.save();
            button.setMessage(sortText());
        }).bounds(buttonX, sortY, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.literal(
                        "Show or hide Ducky's Sort button in supported containers.")))
                .build());

        int optimizationsY = sortY + buttonHeight + gap;
        addRenderableWidget(Button.builder(Component.literal("Ducky's Optimizations"),
                button -> minecraft.gui.setScreen(new OptimizationsScreen(this)))
                .bounds(buttonX, optimizationsY, buttonWidth, buttonHeight).build());

        int doneWidth = Math.min(112, panelWidth - 24);
        addRenderableWidget(Button.builder(Component.literal("Done"),
                button -> minecraft.gui.setScreen(parent))
                .bounds(width - left - doneWidth - 12, footerY, doneWidth, 20).build());
    }

    private static Component toolText() {
        return Component.literal("Adaptive tool switching: " + (PerformanceConfig.adaptiveToolSwitching ? "ON" : "OFF"));
    }

    private static Component sortText() {
        return Component.literal("Container sorting: " + (PerformanceConfig.sortingEnabled ? "ON" : "OFF"));
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
        graphics.text(font, Component.literal("DUCKY'S UTILITIES"), left + 14, 18, 0xFFFFD52A);
        graphics.text(font, Component.literal("Client-side gameplay controls"), left + 14, 38, 0xFFAAAAAA);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        PerformanceConfig.save();
        minecraft.gui.setScreen(parent);
    }
}
