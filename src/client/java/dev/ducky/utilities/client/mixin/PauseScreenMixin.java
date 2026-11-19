package dev.ducky.utilities.client.mixin;

import dev.ducky.utilities.client.gui.DuckyUtilitiesScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void duckyUtilities$addUtilitiesButton(CallbackInfo ci) {
        PauseScreen screen = (PauseScreen) (Object) this;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int x = (screen.width - buttonWidth) / 2;
        int y = 40;

        // Find the first centered row that does not overlap an existing pause-menu button.
        while (y + buttonHeight < screen.height - 8) {
            boolean occupied = false;
            for (var child : screen.children()) {
                if (child instanceof Button button
                        && x < button.getX() + button.getWidth()
                        && x + buttonWidth > button.getX()
                        && y < button.getY() + button.getHeight()
                        && y + buttonHeight > button.getY()) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                break;
            }
            y += buttonHeight + 4;
        }

        ((ScreenAccessor) (Object) screen).duckyUtilities$addRenderableWidget(
                Button.builder(Component.literal("Ducky's Utilities"),
                        press -> net.minecraft.client.Minecraft.getInstance().gui.setScreen(
                                new DuckyUtilitiesScreen(screen)))
                        .bounds(x, y, buttonWidth, buttonHeight)
                        .build());
    }
}
