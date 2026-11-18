package dev.ducky.utilities.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ducky.utilities.OffhandToolHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {
    @ModifyExpressionValue(
        method = "attack(Lnet/minecraft/world/entity/Entity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getWeaponItem()Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private ItemStack duckyUtilities$useOffhandSwordForAttack(ItemStack original) {
        Player player = (Player) (Object) this;
        return OffhandToolHelper.shouldUseOffhandForAttack(player)
                ? player.getOffhandItem()
                : original;
    }
}
