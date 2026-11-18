package dev.ducky.utilities.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class AdaptiveOffhand {
    // InventoryMenu uses button 40 with ClickType.SWAP to exchange a slot with the offhand.
    private static final int OFFHAND_SWAP_BUTTON = 40;
    private static final int SWAP_COOLDOWN_TICKS = 5;

    private static int lastSwapTick = -100;
    private static ItemStack mobReturnStack = ItemStack.EMPTY;
    private static boolean mobSwordActive = false;

    private AdaptiveOffhand() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AdaptiveOffhand::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gui.screen() != null
                || client.gameMode == null
                || player.containerMenu != player.inventoryMenu) {
            return;
        }

        if (player.tickCount - lastSwapTick < SWAP_COOLDOWN_TICKS) return;

        HitResult hit = client.hitResult;

        // While looking at a living entity, temporarily equip a sword in the offhand.
        if (hit instanceof EntityHitResult entityHit
                && hit.getType() == HitResult.Type.ENTITY
                && entityHit.getEntity() instanceof LivingEntity) {
            if (mobSwordActive) return;

            ItemStack currentOffhand = player.getOffhandItem();
            if (currentOffhand.isEmpty() || !isSupportedTool(currentOffhand) || isSword(currentOffhand)) {
                return;
            }

            int swordSlot = findSwordSlot(player);
            if (swordSlot < 0) return;

            mobReturnStack = currentOffhand.copy();
            swapInventorySlotWithOffhand(client, player, swordSlot);
            mobSwordActive = true;
            lastSwapTick = player.tickCount;
            return;
        }

        // As soon as the crosshair leaves the mob, put the previous offhand item back.
        if (mobSwordActive) {
            int returnSlot = findMatchingStackSlot(player, mobReturnStack);
            if (returnSlot >= 0) {
                swapInventorySlotWithOffhand(client, player, returnSlot);
                lastSwapTick = player.tickCount;
            }
            mobSwordActive = false;
            mobReturnStack = ItemStack.EMPTY;
        }

        // Normal adaptive block-tool selection.
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty() || !isSupportedTool(offhand)) return;
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockState state = client.level.getBlockState(blockHit.getBlockPos());
        ItemStack replacement = findBestBlockTool(player, state);
        if (replacement.isEmpty()
                || ItemStack.isSameItemSameComponents(replacement, player.getOffhandItem())) {
            return;
        }

        int sourceSlot = findMatchingStackSlot(player, replacement);
        if (sourceSlot < 0) return;

        swapInventorySlotWithOffhand(client, player, sourceSlot);
        lastSwapTick = player.tickCount;
    }

    private static void swapInventorySlotWithOffhand(
            Minecraft client, LocalPlayer player, int inventorySlot) {
        int menuSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
        client.gameMode.handleContainerInput(
                player.inventoryMenu.containerId,
                menuSlot,
                OFFHAND_SWAP_BUTTON,
                ContainerInput.SWAP,
                player
        );
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

    private static int findSwordSlot(LocalPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            if (slot == player.getInventory().getSelectedSlot()) continue;
            if (isSword(player.getInventory().getItem(slot))) return slot;
        }
        return -1;
    }

    private static int findMatchingStackSlot(LocalPlayer player, ItemStack target) {
        if (target.isEmpty()) return -1;
        for (int slot = 0; slot < 36; slot++) {
            if (slot == player.getInventory().getSelectedSlot()) continue;
            if (ItemStack.isSameItemSameComponents(player.getInventory().getItem(slot), target)) {
                return slot;
            }
        }
        return -1;
    }

    private static boolean isSupportedTool(ItemStack stack) {
        return isBlockTool(stack) || isSword(stack);
    }

    private static boolean isBlockTool(ItemStack stack) {
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_pickaxe") || path.endsWith("_axe")
                || path.endsWith("_shovel") || path.endsWith("_hoe");
    }

    private static boolean isSword(ItemStack stack) {
        return !stack.isEmpty()
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_sword");
    }
}
