package dev.ducky.utilities.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ChestScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(AbstractContainerScreen.class)
public abstract class ChestSortButtonMixin {
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected AbstractContainerMenu menu;

    @Inject(method = "init", at = @At("TAIL"))
    private void duckyUtilities$addSortButton(CallbackInfo ci) {
        if (!((Object) this instanceof ChestScreen) || !(menu instanceof ChestMenu)) return;
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        int buttonX = leftPos + imageWidth + 4;
        int buttonY = topPos + 6;
        if (buttonX + 64 > screen.width) {
            buttonX = leftPos + imageWidth - 68;
            buttonY = topPos - 22;
        }
        screen.addRenderableWidget(Button.builder(
                net.minecraft.network.chat.Component.literal("Sort"),
                button -> duckyUtilities$sortChest())
                .bounds(buttonX, buttonY, 64, 20).build());
    }

    private void duckyUtilities$sortChest() {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || client.gameMode == null || !(menu instanceof ChestMenu)) return;
        if (!menu.getCarried().isEmpty()) return;

        int containerSlots = ((ChestMenu) menu).getContainerSize();
        int syncId = menu.containerId;

        // First merge compatible partial stacks, using normal inventory click packets.
        for (int source = 0; source < containerSlots; source++) {
            ItemStack sourceStack = menu.getSlot(source).getItem();
            if (sourceStack.isEmpty()) continue;
            for (int target = 0; target < containerSlots; target++) {
                if (source == target) continue;
                ItemStack targetStack = menu.getSlot(target).getItem();
                if (targetStack.isEmpty() || !ItemStack.isSameItemSameComponents(sourceStack, targetStack)) continue;
                if (targetStack.getCount() >= targetStack.getMaxStackSize()) continue;
                client.gameMode.handleInventoryMouseClick(syncId, source, 0, ClickType.PICKUP, player);
                client.gameMode.handleInventoryMouseClick(syncId, target, 0, ClickType.PICKUP, player);
                client.gameMode.handleInventoryMouseClick(syncId, source, 0, ClickType.PICKUP, player);
                sourceStack = menu.getSlot(source).getItem();
                if (sourceStack.isEmpty()) break;
            }
        }

        // Then sort by item ID and component data, leaving empty slots at the end.
        Comparator<ItemStack> order = Comparator
                .comparing((ItemStack stack) -> stack.isEmpty() ? 1 : 0)
                .thenComparing(stack -> stack.isEmpty() ? "" :
                        BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                .thenComparing(stack -> stack.isEmpty() ? "" : stack.getComponents().toString());

        for (int i = 0; i < containerSlots; i++) {
            int smallest = i;
            for (int j = i + 1; j < containerSlots; j++) {
                if (order.compare(menu.getSlot(j).getItem(), menu.getSlot(smallest).getItem()) < 0) {
                    smallest = j;
                }
            }
            if (smallest != i) {
                client.gameMode.handleInventoryMouseClick(syncId, i, 0, ClickType.PICKUP, player);
                client.gameMode.handleInventoryMouseClick(syncId, smallest, 0, ClickType.PICKUP, player);
                client.gameMode.handleInventoryMouseClick(syncId, i, 0, ClickType.PICKUP, player);
            }
        }
    }
}
