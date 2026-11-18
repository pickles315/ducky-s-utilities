package dev.ducky.utilities.client.gui;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;

public final class VideoSettingsButton {
    private VideoSettingsButton() {}

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof VideoSettingsScreen) {
                Screens.getWidgets(screen).add(Button.builder(Component.literal("Ducky's Utilities"),
                        button -> client.gui.setScreen(OptimisedConfigScreen.create(screen)))
                        .bounds(width - 140, 8, 132, 20).build());
            }
        });
    }
}
