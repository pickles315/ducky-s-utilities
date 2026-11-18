package dev.ducky.utilities.client.mixin;

import dev.ducky.utilities.client.config.PerformanceConfig;
import net.minecraft.client.renderer.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class BlockUpdateOptimizationMixin {
    @Inject(
            method = "scheduleBlockRerenderIfNeeded",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ducky$skipUnchangedBlockRender(
            BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        if (PerformanceConfig.blockUpdateOptimization && oldState == newState) {
            ci.cancel();
        }
    }
}
