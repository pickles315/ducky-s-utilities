package dev.ducky.utilities.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.registries.BuiltInRegistries;

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
                || !isTool(player.getOffhandItem())) {
            return;
        }

        // Avoid repeatedly sending inventory clicks while the crosshair stays on one target.
        if (player.tickCount - lastSwapTick < 5) return;

        HitResult hit = client.hitResult;
        ItemStack replacement = ItemStack.EMPTY;

        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            BlockState state = client.level.getBlockState(blockHit.getBlockPos());
            replacement = findBestBlockTool(player, state);
        } else if (hit instanceof EntityHitResult entityHit && hit.getType() == HitResult.Type.ENTITY) {
            replacement = findSword(player, entityHit.getEntity());
        }

        if (replacement.isEmpty() || ItemStack.isSameItemSameComponents(replacement, player.getOffhandItem())) {
            return;
        }

        int sourceSlot = findStackSlot(player, replacement);
        if (sourceSlot < 0) return;

        // Pick up the selected inventory stack, exchange it with the offhand,
        // then put the previous offhand stack into the selected inventory slot.
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

    private static ItemStack findSword(LocalPlayer player, Entity target) {
        ItemStack current = player.getOffhandItem();
        if (isSword(current)) return ItemStack.EMPTY;

        for (int slot = 0; slot < 36; slot++) {
            if (slot == player.getInventory().getSelectedSlot()) continue;
            ItemStack candidate = player.getInventory().getItem(slot);
            if (isSword(candidate)) return candidate;
        }
        return ItemStack.EMPTY;
    }

    private static int findStackSlot(LocalPlayer player, ItemStack target) {
        for (int slot = 0; slot < 36; slot++) {
            if (slot == player.getInventory().getSelectedSlot()) continue;
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate == target) return slot < 9 ? slot : slot;
        }
        return -1;
    }

    private static boolean isTool(ItemStack stack) {
        return isBlockTool(stack) || isSword(stack)
                || itemPath(stack).endsWith("_mace")
                || itemPath(stack).equals("trident");
    }

    private static boolean isBlockTool(ItemStack stack) {
        String path = itemPath(stack);
        return path.endsWith("_pickaxe") || path.endsWith("_axe")
                || path.endsWith("_shovel") || path.endsWith("_hoe");
    }

    private static boolean isSword(ItemStack stack) {
        return itemPath(stack).endsWith("_sword");
    }

    private static String itemPath(ItemStack stack) {
        return stack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }
}
