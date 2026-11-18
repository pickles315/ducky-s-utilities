package dev.ducky.utilities;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DuckyUtilities implements ModInitializer {
    public static final String MOD_ID = "ducky_utilities";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Ducky's MC Utilities initialized.");
    }
}
