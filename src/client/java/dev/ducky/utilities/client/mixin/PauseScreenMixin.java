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
        for (var child : screen.children()) {
            if (child instanceof Button button
                    && (button.getMessage().getString().equals("Back to Game")
                    || button.getMessage().getString().equals("Return to Game"))) {
                int x = button.getX();
                int y = button.getY();
                int buttonWidth = button.getWidth();
                int buttonHeight = button.getHeight();

                ((ScreenAccessor) (Object) screen).duckyUtilities$addRenderableWidget(
                        Button.builder(Component.literal("Ducky's Utilities"),
                                press -> net.minecraft.client.Minecraft.getInstance().gui.setScreen(
                                        new DuckyUtilitiesScreen(screen)))
                                .bounds(x, y + buttonHeight + 4, buttonWidth, buttonHeight).build());
                return;
            }
        }
    }
}