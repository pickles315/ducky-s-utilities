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
        int buttonWidth = Math.min(260, width - 32);
        int x = (width - buttonWidth) / 2;
        int buttonHeight = 22;
        int gap = 8;
        int totalHeight = buttonHeight * 4 + gap * 3;
        int startY = Math.max(76, (height - totalHeight) / 2 + 18);

        addRenderableWidget(Button.builder(toolText(), button -> {
            PerformanceConfig.adaptiveToolSwitching = !PerformanceConfig.adaptiveToolSwitching;
            PerformanceConfig.save();
            button.setMessage(toolText());
        }).bounds(x, startY, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.literal("Automatically select a matching tool from your hotbar when looking at a block or living entity. Only activates while holding a supported tool.")))
                .build());

        addRenderableWidget(Button.builder(sortText(), button -> {
            PerformanceConfig.sortingEnabled = !PerformanceConfig.sortingEnabled;
            PerformanceConfig.save();
            button.setMessage(sortText());
        }).bounds(x, startY + buttonHeight + gap, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.literal("Show or hide Ducky's Sort button in supported containers.")))
                .build());

        addRenderableWidget(Button.builder(Component.literal("Ducky's Optimizations"),
                button -> minecraft.gui.setScreen(new OptimizationsScreen(this)))
                .bounds(x, startY + (buttonHeight + gap) * 2, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.literal("Done"),
                button -> minecraft.gui.setScreen(parent))
                .bounds(x, startY + (buttonHeight + gap) * 3, buttonWidth, buttonHeight).build());
    }

    private static Component toolText() {
        return Component.literal("Adaptive tool switching: " + (PerformanceConfig.adaptiveToolSwitching ? "ON" : "OFF"));
    }

    private static Component sortText() {
        return Component.literal("Container sorting: " + (PerformanceConfig.sortingEnabled ? "ON" : "OFF"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelWidth = Math.max(1, Math.min(440, width - 16));
        int panelHeight = Math.min(height - 12, 230);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xE9101010);
        graphics.fill(left, top, left + panelWidth, top + 3, 0xFFFFD52A);
        graphics.centeredText(font, title, width / 2, top + 16, 0xFFFFD52A);
        graphics.centeredText(font, Component.literal("Client-side gameplay and performance controls"),
                width / 2, top + 34, 0xFFCCCCCC);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
}