package dev.ducky.utilities.client.mixin;

import dev.ducky.utilities.client.config.PerformanceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Shadow public abstract <E extends BlockEntity> BlockEntityRenderer<E, ?> getRenderer(E blockEntity);

    @Inject(method = "tryExtractRenderState", at = @At("HEAD"), cancellable = true)
    private void duckyUtilities$limitDistantBlockEntities(BlockEntity blockEntity, float partialTick,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            boolean globallyRendered, CallbackInfoReturnable<BlockEntityRenderState> cir) {
        if (!PerformanceConfig.blockEntityCulling || globallyRendered || blockEntity == null) return;
        BlockEntityRenderer<?, ?> renderer = getRenderer(blockEntity);
        if (renderer == null || renderer.shouldRenderOffScreen()) return;
        var camera = Minecraft.getInstance().gameRenderer.mainCamera().position();
        double dx = blockEntity.getBlockPos().getX() + 0.5 - camera.x();
        double dy = blockEntity.getBlockPos().getY() + 0.5 - camera.y();
        double dz = blockEntity.getBlockPos().getZ() + 0.5 - camera.z();
        double limit = PerformanceConfig.maxBlockEntityDistance;
        if (dx * dx + dy * dy + dz * dz > limit * limit) cir.setReturnValue(null);
    }
}
