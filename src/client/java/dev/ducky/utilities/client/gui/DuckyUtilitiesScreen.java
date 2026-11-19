package dev.ducky.utilities.client.gui;

import dev.ducky.utilities.client.config.PerformanceConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
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
        int buttonWidth = 220;
        int x = (this.width - buttonWidth) / 2;
        int y = this.height / 2 - 38;
        addRenderableWidget(Button.builder(Component.literal("Ducky's Optimizations"),
                button -> minecraft.gui.setScreen(new OptimizationsScreen(this)))
                .bounds(x, y, buttonWidth, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),
                button -> minecraft.setScreen(parent))
                .bounds(x, y + 34, buttonWidth, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int panelWidth = Math.min(420, width - 24);
        int left = (width - panelWidth) / 2;
        int top = height / 2 - 92;
        graphics.fill(left, top, left + panelWidth, top + 184, 0xE9101010);
        graphics.fill(left, top, left + panelWidth, top + 3, 0xFFFFD52A);
        graphics.centeredText(font, title, width / 2, top + 18, 0xFFFFD52A);
        graphics.centeredText(font, Component.literal("Client-side gameplay and performance controls"),
                width / 2, top + 38, 0xFFCCCCCC);
        graphics.centeredText(font, Component.literal("Sorting: " + (PerformanceConfig.sortingEnabled ? "ON" : "OFF")
                        + "   |   Adaptive tools: " + (PerformanceConfig.adaptiveToolSwitching ? "ON" : "OFF")),
                width / 2, top + 76, 0xFFFFFFFF);
        
    }
}
