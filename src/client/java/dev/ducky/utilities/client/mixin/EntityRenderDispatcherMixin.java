package dev.ducky.utilities.client.mixin;

import dev.ducky.utilities.client.config.PerformanceConfig;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void duckyUtilities$limitDistantEntities(Entity entity, Frustum frustum,
            double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ItemEntity && PerformanceConfig.itemEntityCulling) {
            double itemLimit = PerformanceConfig.maxItemEntityDistance;
            double ix = entity.getX() - cameraX, iy = entity.getY() - cameraY, iz = entity.getZ() - cameraZ;
            if (ix * ix + iy * iy + iz * iz > itemLimit * itemLimit) {
                cir.setReturnValue(false);
                return;
            }
        }
        if (!PerformanceConfig.entityCulling) return;
        double limit = PerformanceConfig.maxEntityDistance;
        double dx = entity.getX() - cameraX, dy = entity.getY() - cameraY, dz = entity.getZ() - cameraZ;
        if (dx * dx + dy * dy + dz * dz > limit * limit) cir.setReturnValue(false);
    }
}
