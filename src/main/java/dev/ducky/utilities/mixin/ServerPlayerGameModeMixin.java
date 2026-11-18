package dev.ducky.utilities.mixin;

import dev.ducky.utilities.OffhandToolHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Unique private static final ThreadLocal<BlockPos> DUCKY$BREAKING_POS = new ThreadLocal<>();

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void duckyUtilities$captureBreakingPosition(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        DUCKY$BREAKING_POS.set(pos);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void duckyUtilities$clearBreakingPosition(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        DUCKY$BREAKING_POS.remove();
    }

    @Redirect(method = "destroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack duckyUtilities$selectMiningStack(ServerPlayer player) {
        BlockPos pos = DUCKY$BREAKING_POS.get();
        if (pos == null) return player.getMainHandItem();
        BlockState state = player.level().getBlockState(pos);
        return OffhandToolHelper.shouldUseOffhand(player, state)
                ? player.getOffhandItem() : player.getMainHandItem();
    }
}
