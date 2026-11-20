package dev.ducky.utilities.client.config;

import dev.ducky.utilities.client.DuckyUtilitiesClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class PerformanceConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("ducky-utilities.properties");

    public static volatile boolean sortingEnabled = true;
    public static volatile boolean inventorySortingEnabled = true;
    public static volatile boolean adaptiveToolSwitching = true;
    public static volatile boolean entityCulling = true;
    public static volatile double maxEntityDistance = 96.0;
    public static volatile boolean blockEntityCulling = true;
    public static volatile double maxBlockEntityDistance = 64.0;
    public static volatile boolean itemEntityCulling = true;
    public static volatile boolean livingEntityCulling = false;
    public static volatile double maxLivingEntityDistance = 64.0;
    public static volatile boolean projectileCulling = false;
    public static volatile double maxProjectileDistance = 48.0;
    public static volatile boolean experienceOrbCulling = false;
    public static volatile double maxExperienceOrbDistance = 32.0;
    public static volatile double maxItemEntityDistance = 48.0;
    public static volatile boolean chunkUpdateOptimization = true;
    public static volatile boolean chunkLoadingOptimization = true;
    public static volatile boolean blockUpdateOptimization = true;
    public static volatile boolean lightingOptimization = false;

    private PerformanceConfig() {}

    public static void load() {
        Properties p = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) { p.load(reader); }
            catch (IOException e) { DuckyUtilitiesClient.LOGGER.warn("Could not read Ducky Utilities config.", e); }
        }
        sortingEnabled = Boolean.parseBoolean(p.getProperty("sortingEnabled", "true"));
        inventorySortingEnabled = Boolean.parseBoolean(p.getProperty("inventorySortingEnabled", "true"));
        adaptiveToolSwitching = Boolean.parseBoolean(p.getProperty("adaptiveToolSwitching", "true"));
        entityCulling = Boolean.parseBoolean(p.getProperty("entityCulling", "true"));
        maxEntityDistance = readDistance(p, "maxEntityDistance", 96.0, 512.0);
        blockEntityCulling = Boolean.parseBoolean(p.getProperty("blockEntityCulling", "true"));
        maxBlockEntityDistance = readDistance(p, "maxBlockEntityDistance", 64.0, 256.0);
        itemEntityCulling = Boolean.parseBoolean(p.getProperty("itemEntityCulling", "true"));
        livingEntityCulling = Boolean.parseBoolean(p.getProperty("livingEntityCulling", "false"));
        maxLivingEntityDistance = readDistance(p, "maxLivingEntityDistance", 64.0, 512.0);
        projectileCulling = Boolean.parseBoolean(p.getProperty("projectileCulling", "false"));
        maxProjectileDistance = readDistance(p, "maxProjectileDistance", 48.0, 256.0);
        experienceOrbCulling = Boolean.parseBoolean(p.getProperty("experienceOrbCulling", "false"));
        maxExperienceOrbDistance = readDistance(p, "maxExperienceOrbDistance", 32.0, 256.0);
        maxItemEntityDistance = readDistance(p, "maxItemEntityDistance", 48.0, 256.0);
        chunkUpdateOptimization = Boolean.parseBoolean(p.getProperty("chunkUpdateOptimization", "true"));
        chunkLoadingOptimization = Boolean.parseBoolean(p.getProperty("chunkLoadingOptimization", "true"));
        blockUpdateOptimization = Boolean.parseBoolean(p.getProperty("blockUpdateOptimization", "true"));
        lightingOptimization = Boolean.parseBoolean(p.getProperty("lightingOptimization", "false"));
        save();
    }

    private static double readDistance(Properties p, String key, double fallback, double max) {
        try { return Math.max(16.0, Math.min(max, Double.parseDouble(p.getProperty(key, Double.toString(fallback))))); }
        catch (NumberFormatException e) { return fallback; }
    }

    public static synchronized void save() {
        Properties p = new Properties();
        p.setProperty("sortingEnabled", Boolean.toString(sortingEnabled));
        p.setProperty("inventorySortingEnabled", Boolean.toString(inventorySortingEnabled));
        p.setProperty("adaptiveToolSwitching", Boolean.toString(adaptiveToolSwitching));
        p.setProperty("entityCulling", Boolean.toString(entityCulling));
        p.setProperty("maxEntityDistance", Double.toString(maxEntityDistance));
        p.setProperty("blockEntityCulling", Boolean.toString(blockEntityCulling));
        p.setProperty("maxBlockEntityDistance", Double.toString(maxBlockEntityDistance));
        p.setProperty("itemEntityCulling", Boolean.toString(itemEntityCulling));
        p.setProperty("livingEntityCulling", Boolean.toString(livingEntityCulling));
        p.setProperty("maxLivingEntityDistance", Double.toString(maxLivingEntityDistance));
        p.setProperty("projectileCulling", Boolean.toString(projectileCulling));
        p.setProperty("maxProjectileDistance", Double.toString(maxProjectileDistance));
        p.setProperty("experienceOrbCulling", Boolean.toString(experienceOrbCulling));
        p.setProperty("maxExperienceOrbDistance", Double.toString(maxExperienceOrbDistance));
        p.setProperty("maxItemEntityDistance", Double.toString(maxItemEntityDistance));
        p.setProperty("chunkUpdateOptimization", Boolean.toString(chunkUpdateOptimization));
        p.setProperty("chunkLoadingOptimization", Boolean.toString(chunkLoadingOptimization));
        p.setProperty("blockUpdateOptimization", Boolean.toString(blockUpdateOptimization));
        p.setProperty("lightingOptimization", Boolean.toString(lightingOptimization));
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) { p.store(writer, "Ducky's MC Utilities settings"); }
        } catch (IOException e) { DuckyUtilitiesClient.LOGGER.warn("Could not save Ducky Utilities config.", e); }
    }
}
