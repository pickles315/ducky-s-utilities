package dev.ducky.utilities.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class AdaptiveOffhand {
    private static final int OFFHAND_MENU_SLOT = 40;
    private static int lastSwapTick = -100;

    private AdaptiveOffhand() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AdaptiveOffhand::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.screen != null
                || client.interactionManager == null
                || player.containerMenu != player.inventoryMenu
                || player.getOffhandItem().isEmpty()
                || !isBlockTool(player.getOffhandItem())) {
            return;
        }

        if (player.tickCount - lastSwapTick < 5) return;

        HitResult hit = client.hitResult;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockState state = client.level.getBlockState(blockHit.getBlockPos());
        ItemStack replacement = findBestBlockTool(player, state);
        if (replacement.isEmpty()
                || ItemStack.isSameItemSameComponents(replacement, player.getOffhandItem())) {
            return;
        }

        int sourceSlot = findStackSlot(player, replacement);
        if (sourceSlot < 0) return;

        client.interactionManager.clickSlot(player.inventoryMenu.containerId, sourceSlot, 0, ClickType.PICKUP, player);
        client.interactionManager.clickSlot(player.inventoryMenu.containerId, OFFHAND_MENU_SLOT, 0, ClickType.PICKUP, player);
        client.interactionManager.clickSlot(player.inventoryMenu.containerId, sourceSlot, 0, ClickType.PICKUP, player);
        lastSwapTick = player.tickCount;
    }

    private static ItemStack findBestBlockTool(LocalPlayer player, BlockState state) {
        ItemStack current = player.getOffhandItem();
        ItemStack best = ItemStack.EMPTY;
        float bestSpeed = current.getDestroySpeed(state);
        boolean currentCorrect = current.isCorrectToolForDrops(state);

        for (int slot = 0; slot < 36; slot++) {
            if (slot == player.getInventory().getSelectedSlot()) continue;
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.isEmpty() || !isBlockTool(candidate)) continue;

            boolean candidateCorrect = candidate.isCorrectToolForDrops(state);
            float speed = candidate.getDestroySpeed(state);

            if (state.requiresCorrectToolForDrops() && candidateCorrect && !currentCorrect) {
                best = candidate;
                bestSpeed = speed;
                currentCorrect = true;
            } else if (candidateCorrect == currentCorrect && speed > bestSpeed) {
                best = candidate;
                bestSpeed = speed;
            }
        }
        return best;
    }

    private static int findStackSlot(LocalPlayer player, ItemStack target) {
        for (int slot = 0; slot < 36; slot++) {
            if (slot == player.getInventory().getSelectedSlot()) continue;
            if (player.getInventory().getItem(slot) == target) return slot;
        }
        return -1;
    }

    private static boolean isBlockTool(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_pickaxe") || path.endsWith("_axe")
                || path.endsWith("_shovel") || path.endsWith("_hoe");
    }
}
