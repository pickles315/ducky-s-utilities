package dev.ducky.utilities.client.mixin;

import dev.ducky.utilities.OffhandToolHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Minecraft.class)
public abstract class MinecraftToolSwingMixin {
    @ModifyArg(
        method = "startAttack()Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"
        ),
        index = 0
    )
    private InteractionHand duckyUtilities$useCorrectToolHandOnStart(InteractionHand original) {
        return duckyUtilities$getMiningHand(original);
    }

    @ModifyArg(
        method = "continueAttack(Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"
        ),
        index = 0
    )
    private InteractionHand duckyUtilities$useCorrectToolHandWhileMining(InteractionHand original) {
        return duckyUtilities$getMiningHand(original);
    }

    private InteractionHand duckyUtilities$getMiningHand(InteractionHand original) {
        Minecraft minecraft = (Minecraft) (Object) this;
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.hitResult == null
                || minecraft.hitResult.getType() != HitResult.Type.BLOCK
                || !(minecraft.hitResult instanceof BlockHitResult blockHit)
                || player.level() == null) {
            return original;
        }

        if (OffhandToolHelper.shouldUseOffhand(
                player, player.level().getBlockState(blockHit.getBlockPos()))) {
            return InteractionHand.OFF_HAND;
        }

        return original;
    }
}
