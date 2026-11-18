package dev.ducky.utilities.mixin;

import dev.ducky.utilities.OffhandToolHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {
    @Redirect(method = "attack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getWeaponItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack duckyUtilities$useOffhandSwordForAttack(LivingEntity entity) {
        Player player = (Player) (Object) this;
        return OffhandToolHelper.shouldUseOffhandForAttack(player)
                ? player.getOffhandItem() : entity.getWeaponItem();
    }
}
