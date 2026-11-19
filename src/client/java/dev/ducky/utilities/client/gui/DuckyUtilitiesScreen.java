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
        int buttonWidth = 240;
        int x = (width - buttonWidth) / 2;
        int y = height / 2 - 62;

        addRenderableWidget(Button.builder(toolText(), button -> {
            PerformanceConfig.adaptiveToolSwitching = !PerformanceConfig.adaptiveToolSwitching;
            PerformanceConfig.save();
            button.setMessage(toolText());
        }).bounds(x, y, buttonWidth, 22)
                .tooltip(Tooltip.create(Component.literal("Automatically select a better matching tool from your hotbar when looking at a block or living entity. Only activates while you are already holding a supported tool.")))
                .build());

        addRenderableWidget(Button.builder(sortText(), button -> {
            PerformanceConfig.sortingEnabled = !PerformanceConfig.sortingEnabled;
            PerformanceConfig.save();
            button.setMessage(sortText());
        }).bounds(x, y + 28, buttonWidth, 22)
                .tooltip(Tooltip.create(Component.literal("Show or hide Ducky's Sort button in supported containers.")))
                .build());

        addRenderableWidget(Button.builder(Component.literal("Ducky's Optimizations"),
                button -> minecraft.gui.setScreen(new OptimizationsScreen(this)))
                .bounds(x, y + 62, buttonWidth, 24).build());

        addRenderableWidget(Button.builder(Component.literal("Done"),
                button -> minecraft.gui.setScreen(parent))
                .bounds(x, y + 96, buttonWidth, 20).build());
    }

    private static Component toolText() {
        return Component.literal("Adaptive tool switching: " + (PerformanceConfig.adaptiveToolSwitching ? "ON" : "OFF"));
    }

    private static Component sortText() {
        return Component.literal("Container sorting: " + (PerformanceConfig.sortingEnabled ? "ON" : "OFF"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int panelWidth = Math.min(440, width - 24);
        int left = (width - panelWidth) / 2;
        int top = height / 2 - 104;
        graphics.fill(left, top, left + panelWidth, top + 206, 0xE9101010);
        graphics.fill(left, top, left + panelWidth, top + 3, 0xFFFFD52A);
        graphics.centeredText(font, title, width / 2, top + 18, 0xFFFFD52A);
        graphics.centeredText(font, Component.literal("Client-side gameplay and performance controls"),
                width / 2, top + 38, 0xFFCCCCCC);
    }
}
