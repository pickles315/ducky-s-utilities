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
    private static final int OFFHAND_BUTTON = 40;

    private static boolean swapActive;
    private static int swappedInventorySlot = -1;
    private static boolean swappedWithOffhand;
    private static int swappedHotbarSlot = -1;
    private static int targetEntityId = -1;
    private static net.minecraft.core.BlockPos targetBlock;

    private AdaptiveOffhand() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AdaptiveOffhand::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null
                || client.gui.screen() != null || player.containerMenu != player.inventoryMenu) {
            restoreIfNeeded(client, player);
            return;
        }

        HitResult hit = client.hitResult;
        boolean sameTarget = swapActive && isSameTarget(hit);
        if (swapActive && !sameTarget) {
            restoreIfNeeded(client, player);
        }
        if (swapActive) return;

        // The feature is enabled by carrying a supported tool in the offhand.
        ItemStack offhand = player.getOffhandItem();
        if (!isSupportedTool(offhand)) return;

        if (hit instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity living
                && living.isAlive()) {
            int swordSlot = findSwordSlot(player);
            if (isSword(player.getMainHandItem())) return;

            if (isSword(offhand)) {
                swappedHotbarSlot = player.getInventory().getSelectedSlot();
                swapMainHandWithOffhand(client, player, swappedHotbarSlot);
                swappedWithOffhand = true;
            } else if (swordSlot >= 0) {
                swappedHotbarSlot = player.getInventory().getSelectedSlot();
                swapInventorySlotIntoMainHand(client, player, swordSlot, swappedHotbarSlot);
                swappedInventorySlot = swordSlot;
            } else {
                return;
            }
            targetEntityId = living.getId();
            swapActive = true;
            return;
        }

        if (hit instanceof BlockHitResult blockHit) {
            BlockState state = client.level.getBlockState(blockHit.getBlockPos());
            ToolChoice choice = findBestBlockTool(player, state);
            if (choice == null || !isBetterThanMainHand(player, state, choice.stack())) return;

            if (choice.offhand()) {
                swapMainHandWithOffhand(client, player);
                swappedWithOffhand = true;
            } else {
                swappedHotbarSlot = player.getInventory().getSelectedSlot();
                swapInventorySlotIntoMainHand(client, player, choice.slot(), swappedHotbarSlot);
                swappedInventorySlot = choice.slot();
            }
            targetBlock = blockHit.getBlockPos();
            swapActive = true;
        }
    }

    private static boolean isSameTarget(HitResult hit) {
        if (targetEntityId >= 0 && hit instanceof EntityHitResult entityHit) {
            return entityHit.getEntity().getId() == targetEntityId;
        }
        return targetBlock != null && hit instanceof BlockHitResult blockHit
                && targetBlock.equals(blockHit.getBlockPos());
    }

    private static void restoreIfNeeded(Minecraft client, LocalPlayer player) {
        if (!swapActive || player == null || client.gameMode == null) {
            return;
        }
        if (player.containerMenu != player.inventoryMenu) {
            return;
        }

        if (swappedWithOffhand) {
            swapMainHandWithOffhand(client, player, swappedHotbarSlot);
        } else if (swappedInventorySlot >= 0) {
            swapInventorySlotIntoMainHand(client, player, swappedInventorySlot, swappedHotbarSlot);
        }
        clearState();
    }

    private static void clearState() {
        swapActive = false;
        swappedInventorySlot = -1;
        swappedWithOffhand = false;
        swappedHotbarSlot = -1;
        targetEntityId = -1;
        targetBlock = null;
    }

    private static void swapMainHandWithOffhand(
            Minecraft client, LocalPlayer player, int hotbarSlot) {
        int selectedMenuSlot = hotbarSlot + 36;
        client.gameMode.handleContainerInput(
                player.inventoryMenu.containerId, selectedMenuSlot, OFFHAND_BUTTON,
                ContainerInput.SWAP, player);
    }

    private static void swapInventorySlotIntoMainHand(
            Minecraft client, LocalPlayer player, int inventorySlot, int hotbarSlot) {
        int menuSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
        int selectedHotbarButton = hotbarSlot;
        client.gameMode.handleContainerInput(
                player.inventoryMenu.containerId, menuSlot, selectedHotbarButton,
                ContainerInput.SWAP, player);
    }

    private static ToolChoice findBestBlockTool(LocalPlayer player, BlockState state) {
        ItemStack best = ItemStack.EMPTY;
        int bestSlot = -1;
        boolean bestIsOffhand = false;
        float bestSpeed = -1.0F;
        boolean bestCorrect = false;

        ItemStack offhand = player.getOffhandItem();
        if (isBlockTool(offhand)) {
            best = offhand;
            bestIsOffhand = true;
            bestSpeed = offhand.getDestroySpeed(state);
            bestCorrect = offhand.isCorrectToolForDrops(state);
        }

        for (int slot = 0; slot < 36; slot++) {
            if (slot == player.getInventory().getSelectedSlot()) continue;
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.isEmpty() || !isBlockTool(candidate)) continue;

            boolean correct = candidate.isCorrectToolForDrops(state);
            float speed = candidate.getDestroySpeed(state);
            boolean preferred = state.requiresCorrectToolForDrops()
                    ? correct && !bestCorrect
                    : false;
            if (preferred || (correct == bestCorrect && speed > bestSpeed)) {
                best = candidate;
                bestSlot = slot;
                bestIsOffhand = false;
                bestSpeed = speed;
                bestCorrect = correct;
            }
        }

        return best.isEmpty() ? null : new ToolChoice(best, bestSlot, bestIsOffhand);
    }

    private static boolean isBetterThanMainHand(
            LocalPlayer player, BlockState state, ItemStack candidate) {
        ItemStack main = player.getMainHandItem();
        boolean candidateCorrect = candidate.isCorrectToolForDrops(state);
        boolean mainCorrect = main.isCorrectToolForDrops(state);
        if (state.requiresCorrectToolForDrops() && candidateCorrect != mainCorrect) {
            return candidateCorrect;
        }
        return candidate.getDestroySpeed(state) > main.getDestroySpeed(state);
    }

    private static int findSwordSlot(LocalPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            if (slot == player.getInventory().getSelectedSlot()) continue;
            if (isSword(player.getInventory().getItem(slot))) return slot;
        }
        return -1;
    }

    private static boolean isSupportedTool(ItemStack stack) {
        return isBlockTool(stack) || isSword(stack);
    }

    private static boolean isBlockTool(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_pickaxe") || path.endsWith("_axe")
                || path.endsWith("_shovel") || path.endsWith("_hoe");
    }

    private static boolean isSword(ItemStack stack) {
        return !stack.isEmpty()
                && BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_sword");
    }

    private record ToolChoice(ItemStack stack, int slot, boolean offhand) {}
}
