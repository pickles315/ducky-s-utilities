package dev.ducky.utilities.client;

import dev.ducky.utilities.client.config.PerformanceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;

/**
 * Applies client-side rendering options that reduce the cost of rebuilding
 * and displaying world sections. Vanilla gameplay and server-side simulation
 * remain untouched.
 */
public final class WorldPerformanceOptimizations {
    private static PrioritizeChunkUpdates originalChunkPriority;
    private static Double originalChunkFade;
    private static Boolean originalAmbientOcclusion;

    private WorldPerformanceOptimizations() {}

    public static void apply(Minecraft client) {
        if (client.options == null) return;

        var priority = client.options.prioritizeChunkUpdates();
        if (PerformanceConfig.chunkUpdateOptimization) {
            if (originalChunkPriority == null) originalChunkPriority = priority.get();
            priority.set(PrioritizeChunkUpdates.NONE);
        } else if (originalChunkPriority != null) {
            priority.set(originalChunkPriority);
            originalChunkPriority = null;
        }

        var fade = client.options.chunkSectionFadeInTime();
        if (PerformanceConfig.chunkLoadingOptimization) {
            if (originalChunkFade == null) originalChunkFade = fade.get();
            fade.set(0.0);
        } else if (originalChunkFade != null) {
            fade.set(originalChunkFade);
            originalChunkFade = null;
        }

        var ambientOcclusion = client.options.ambientOcclusion();
        if (PerformanceConfig.lightingOptimization) {
            if (originalAmbientOcclusion == null) originalAmbientOcclusion = ambientOcclusion.get();
            ambientOcclusion.set(false);
        } else if (originalAmbientOcclusion != null) {
            ambientOcclusion.set(originalAmbientOcclusion);
            originalAmbientOcclusion = null;
        }
    }
}
