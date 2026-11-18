package dev.ducky.utilities.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

import java.util.concurrent.ThreadLocalRandom;

public final class NothingHappensFeature {
    private static final long TICKS_PER_HOUR = 20L * 60L * 60L;
    private static final long ONE_QUINTILLION = 1_000_000_000_000_000_000L;

    private static long activeTicks;

    private NothingHappensFeature() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(NothingHappensFeature::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null || client.isPaused()) {
            return;
        }

        activeTicks++;
        if (activeTicks >= TICKS_PER_HOUR) {
            activeTicks = 0;

            if (ThreadLocalRandom.current().nextLong(ONE_QUINTILLION) == 0L) {
                SystemToast.add(
                    client,
                    SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                    Component.literal("One in a Quintillion"),
                    Component.literal("Very lucky. Nothing happened.")
                );
            }
        }
    }
}
