package dev.ducky.utilities.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import dev.ducky.utilities.client.config.PerformanceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class AdaptiveOffhand {
    private static boolean swapActive;
    private static int originalHotbarSlot = -1;
    private static int autoSelectedSlot = -1;
    private static int targetEntityId = -1;
    private static net.minecraft.core.BlockPos targetBlock;

    private AdaptiveOffhand() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(AdaptiveOffhand::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        LocalPlayer player = client.player;
        if (!PerformanceConfig.adaptiveToolSwitching) {
            restoreIfNeeded(client, player);
            return;
        }
        if (player == null || client.level == null || client.gameMode == null
                || client.gui.screen() != null || player.containerMenu != player.inventoryMenu) {
            restoreIfNeeded(client, player);
            return;
        }

        HitResult hit = client.hitResult;
        if (swapActive && !isSameTarget(hit)) {
            restoreIfNeeded(client, player);
        }
        if (swapActive) return;

        // Only activate when the player is already holding a tool.
        // This prevents switching away from food, blocks, and other held items.
        if (!isSupportedTool(player.getMainHandItem())) return;

        if (hit instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity living
                && living.isAlive()) {
            if (isSword(player.getMainHandItem())) return;

            int swordSlot = findSwordSlot(player);
            if (swordSlot < 0) return;

            selectHotbarSlot(player, swordSlot);
            targetEntityId = living.getId();
            swapActive = true;
            return;
        }

        if (hit instanceof BlockHitResult blockHit) {
            BlockState state = client.level.getBlockState(blockHit.getBlockPos());
            int bestSlot = findBestBlockToolSlot(player, state);
            if (bestSlot < 0 || bestSlot == player.getInventory().getSelectedSlot()) return;

            ItemStack candidate = player.getInventory().getItem(bestSlot);
            ItemStack current = player.getMainHandItem();
            if (!isBetterForBlock(candidate, current, state)) return;

            selectHotbarSlot(player, bestSlot);
            targetBlock = blockHit.getBlockPos();
            swapActive = true;
        }
    }

    private static void selectHotbarSlot(LocalPlayer player, int slot) {
        originalHotbarSlot = player.getInventory().getSelectedSlot();
        autoSelectedSlot = slot;
        player.getInventory().setSelectedSlot(slot);
    }

    private static boolean isSameTarget(HitResult hit) {
        if (targetEntityId >= 0 && hit instanceof EntityHitResult entityHit) {
            return entityHit.getEntity().getId() == targetEntityId;
        }
        return targetBlock != null && hit instanceof BlockHitResult blockHit
                && targetBlock.equals(blockHit.getBlockPos());
    }

    private static void restoreIfNeeded(Minecraft client, LocalPlayer player) {
        if (!swapActive) return;
        if (player == null || client.gameMode == null) {
            clearState();
            return;
        }
        if (player.containerMenu != player.inventoryMenu) return;

        // Do not override a hotbar choice the player made manually.
        if (player.getInventory().getSelectedSlot() == autoSelectedSlot) {
            player.getInventory().setSelectedSlot(originalHotbarSlot);
        }
        clearState();
    }

    private static void clearState() {
        swapActive = false;
        originalHotbarSlot = -1;
        autoSelectedSlot = -1;
        targetEntityId = -1;
        targetBlock = null;
    }

    private static int findBestBlockToolSlot(LocalPlayer player, BlockState state) {
        String requiredTool = getRequiredTool(state);
        if (requiredTool == null) return -1;

        int bestSlot = -1;
        float bestSpeed = -1.0F;
        for (int slot = 0; slot < 9; slot++) {
            if (slot == player.getInventory().getSelectedSlot()) continue;
            ItemStack candidate = player.getInventory().getItem(slot);
            if (!isToolFor(candidate, requiredTool)) continue;

            float speed = candidate.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    private static String getRequiredTool(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) return "axe";
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return "shovel";
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return "pickaxe";
        if (state.is(BlockTags.MINEABLE_WITH_HOE)) return "hoe";
        return null;
    }

    private static boolean isToolFor(ItemStack stack, String tool) {
        if (stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.endsWith("_" + tool);
    }

    private static boolean isBetterForBlock(
            ItemStack candidate, ItemStack current, BlockState state) {
        return candidate.getDestroySpeed(state) > current.getDestroySpeed(state);
    }

    private static int findSwordSlot(LocalPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
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
}
