package dev.ducky.utilities.client;

import dev.ducky.utilities.client.config.PerformanceConfig;
import dev.ducky.utilities.client.gui.VideoSettingsButton;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DuckyUtilitiesClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ducky_utilities");

    @Override
    public void onInitializeClient() {
        PerformanceConfig.load();
        VideoSettingsButton.register();
        NothingHappensFeature.register();
        LOGGER.info("Ducky's MC Utilities client features loaded.");
    }
}
