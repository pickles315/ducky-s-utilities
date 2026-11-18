package dev.ducky.utilities.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class OffhandToolHelper {
    private OffhandToolHelper() {}

    public static boolean shouldUseOffhandForAttack(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return isSword(offHand) && !isSword(mainHand);
    }

    private static boolean isSword(ItemStack stack) {
        return !stack.isEmpty()
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_sword");
    }

    public static boolean shouldUseOffhand(Player player, BlockState state) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (offHand.isEmpty()) return false;
        if (state.requiresCorrectToolForDrops()) {
            return !mainHand.isCorrectToolForDrops(state) && offHand.isCorrectToolForDrops(state);
        }
        return offHand.getDestroySpeed(state) > mainHand.getDestroySpeed(state);
    }
}
