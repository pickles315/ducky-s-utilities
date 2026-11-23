package dev.ducky.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DuckyUtilities.MOD_ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class ClientFeatures {
    private static int previousSlot = -1;
    private static int switchedSlot = -1;
    private static Object currentTarget;

    private ClientFeatures() {}

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.screen != null
                || player.containerMenu != player.inventoryMenu
                || !UtilityConfig.ADAPTIVE_TOOLS.get()) {
            restore(player);
            return;
        }

        HitResult hit = mc.hitResult;
        Object target = null;
        int candidate = -1;
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living && living.isAlive()) {
            target = living;
            if (!isSword(player.getMainHandItem())) candidate = findSword(player);
        } else if (hit instanceof BlockHitResult blockHit) {
            target = blockHit.getBlockPos();
            var state = mc.level.getBlockState(blockHit.getBlockPos());
            String tool = state.is(BlockTags.MINEABLE_WITH_PICKAXE) ? "pickaxe"
                    : state.is(BlockTags.MINEABLE_WITH_AXE) ? "axe"
                    : state.is(BlockTags.MINEABLE_WITH_SHOVEL) ? "shovel"
                    : state.is(BlockTags.MINEABLE_WITH_HOE) ? "hoe" : "";
            if (!tool.isEmpty()) candidate = findTool(player, state, tool);
        }

        if (previousSlot >= 0 && !java.util.Objects.equals(currentTarget, target)) restore(player);
        if (previousSlot < 0 && candidate >= 0 && candidate != player.getInventory().selected) {
            previousSlot = player.getInventory().selected;
            switchedSlot = candidate;
            player.getInventory().selected = candidate;
            currentTarget = target;
        }
    }

    private static void restore(LocalPlayer player) {
        if (previousSlot < 0) return;
        if (player != null && player.getInventory().selected == switchedSlot) player.getInventory().selected = previousSlot;
        previousSlot = switchedSlot = -1;
        currentTarget = null;
    }

    private static int findSword(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (i != player.getInventory().selected && isSword(player.getInventory().getItem(i))) return i;
        }
        return -1;
    }

    private static int findTool(LocalPlayer player, net.minecraft.world.level.block.state.BlockState state, String tool) {
        int bestSlot = -1;
        float bestSpeed = 1.0F;
        for (int i = 0; i < 9; i++) {
            if (i == player.getInventory().selected) continue;
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_" + tool)) continue;
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private static boolean isSword(ItemStack stack) {
        return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().endsWith("_sword");
    }

    @SubscribeEvent
    public static void cullLiving(RenderLivingEvent.Pre<?, ?> event) {
        if (!UtilityConfig.ENTITY_CULLING.get()) return;
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double limit = UtilityConfig.ENTITY_DISTANCE.get();
        if (event.getEntity().distanceToSqr(camera.x, camera.y, camera.z) > limit * limit) {
            event.setCanceled(true);
        }
    }
}
