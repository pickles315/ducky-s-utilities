package dev.ducky.utilities.client.mixin;

import dev.ducky.utilities.client.config.PerformanceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
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
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        // The player's own inventory has its own independent toggle and sorting action.
        if (menu instanceof InventoryMenu) {
            if (!PerformanceConfig.inventorySortingEnabled) return;
            int buttonX = leftPos + imageWidth - 64;
            int buttonY = Math.max(4, topPos - 24);
            buttonX = Math.max(4, Math.min(buttonX, screen.width - 68));
            ((ScreenAccessor) (Object) screen).duckyUtilities$addRenderableWidget(
                    Button.builder(net.minecraft.network.chat.Component.literal("Sort"),
                                    button -> duckyUtilities$sortPlayerInventory())
                            .bounds(buttonX, buttonY, 64, 20).build());
            return;
        }

        if (!PerformanceConfig.sortingEnabled || duckyUtilities$containerSlotCount() <= 0) return;
        int buttonX = leftPos + (imageWidth - 64) / 2;
        int buttonY = Math.max(4, topPos - 24);
        buttonX = Math.max(4, Math.min(buttonX, screen.width - 68));
        Button sortButton = Button.builder(
                net.minecraft.network.chat.Component.literal("Sort"),
                button -> duckyUtilities$sortContainer())
                .bounds(buttonX, buttonY, 64, 20).build();
        ((ScreenAccessor) (Object) screen).duckyUtilities$addRenderableWidget(sortButton);
    }

    private int duckyUtilities$containerSlotCount() {
        if (menu instanceof ChestMenu) return menu.slots.size() - 36;
        String menuName = menu.getClass().getSimpleName();
        return switch (menuName) {
            case "ShulkerBoxMenu" -> 27;
            case "HopperMenu" -> 5;
            case "DispenserMenu" -> 9;
            default -> 0;
        };
    }

    private static Comparator<ItemStack> duckyUtilities$itemOrder() {
        return Comparator
                .comparing((ItemStack stack) -> stack.isEmpty() ? 1 : 0)
                .thenComparing(stack -> stack.isEmpty() ? "" :
                        BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                .thenComparing(stack -> stack.isEmpty() ? "" : stack.getComponents().toString());
    }

    private void duckyUtilities$sortPlayerInventory() {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || client.gameMode == null || !(menu instanceof InventoryMenu)) return;
        if (!menu.getCarried().isEmpty()) return;

        // InventoryMenu slots 9..35 are the 27 main-inventory slots.
        // Hotbar (36..44), armor, crafting, and offhand are deliberately untouched.
        duckyUtilities$sortSlots(9, 27);
    }

    private void duckyUtilities$sortContainer() {
        duckyUtilities$sortSlots(0, duckyUtilities$containerSlotCount());
    }

    private void duckyUtilities$sortSlots(int firstSlot, int count) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || client.gameMode == null || count <= 0 || !menu.getCarried().isEmpty()) return;
        int syncId = menu.containerId;

        // Merge matching partial stacks first using normal inventory click packets.
        for (int source = firstSlot; source < firstSlot + count; source++) {
            ItemStack sourceStack = menu.getSlot(source).getItem();
            if (sourceStack.isEmpty()) continue;
            for (int target = firstSlot; target < firstSlot + count; target++) {
                if (source == target) continue;
                ItemStack targetStack = menu.getSlot(target).getItem();
                if (targetStack.isEmpty() || !ItemStack.isSameItemSameComponents(sourceStack, targetStack)) continue;
                if (targetStack.getCount() >= targetStack.getMaxStackSize()) continue;
                client.gameMode.handleContainerInput(syncId, source, 0, ContainerInput.PICKUP, player);
                client.gameMode.handleContainerInput(syncId, target, 0, ContainerInput.PICKUP, player);
                client.gameMode.handleContainerInput(syncId, source, 0, ContainerInput.PICKUP, player);
                sourceStack = menu.getSlot(source).getItem();
                if (sourceStack.isEmpty()) break;
            }
        }

        Comparator<ItemStack> order = duckyUtilities$itemOrder();
        for (int i = firstSlot; i < firstSlot + count; i++) {
            int smallest = i;
            for (int j = i + 1; j < firstSlot + count; j++) {
                if (order.compare(menu.getSlot(j).getItem(), menu.getSlot(smallest).getItem()) < 0) smallest = j;
            }
            if (smallest != i) {
                client.gameMode.handleContainerInput(syncId, i, 0, ContainerInput.PICKUP, player);
                client.gameMode.handleContainerInput(syncId, smallest, 0, ContainerInput.PICKUP, player);
                client.gameMode.handleContainerInput(syncId, i, 0, ContainerInput.PICKUP, player);
            }
        }
    }
}
