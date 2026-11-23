package dev.ducky.utilities.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin {
    private static final int DUCKY_MAX_BOOK_PAGES = 100;
    private static final int DUCKY_MAX_PAGE_LENGTH = 1024;

    @Shadow private List<String> pages;
    @Shadow private int currentPage;
    @Shadow private MultiLineEditBox page;

    @Inject(method = "init", at = @At("TAIL"))
    private void duckyUtilities$addBookClipboardButtons(CallbackInfo ci) {
        BookEditScreen screen = (BookEditScreen) (Object) this;
        int x = screen.width / 2 + 101;
        int y = screen.height / 2 - 24;

        screen.addRenderableWidget(Button.builder(
                net.minecraft.network.chat.Component.literal("Copy"),
                button -> duckyUtilities$copyBook())
                .bounds(x, y, 54, 20).build());

        screen.addRenderableWidget(Button.builder(
                net.minecraft.network.chat.Component.literal("Paste"),
                button -> duckyUtilities$pasteBook())
                .bounds(x, y + 24, 54, 20).build());
    }

    private void duckyUtilities$saveCurrentPage() {
        if (page != null && currentPage >= 0 && currentPage < pages.size()) {
            pages.set(currentPage, page.getValue());
        }
    }

    private void duckyUtilities$copyBook() {
        duckyUtilities$saveCurrentPage();
        String clipboardText = String.join("\n", pages);
        Minecraft.getInstance().keyboardHandler.setClipboard(clipboardText);
    }

    private void duckyUtilities$pasteBook() {
        String clipboardText = Minecraft.getInstance().keyboardHandler.getClipboard();
        if (clipboardText == null) clipboardText = "";

        List<String> pastedPages = new ArrayList<>();
        for (int start = 0; start < clipboardText.length()
                && pastedPages.size() < DUCKY_MAX_BOOK_PAGES;
             start += DUCKY_MAX_PAGE_LENGTH) {
            int end = Math.min(start + DUCKY_MAX_PAGE_LENGTH, clipboardText.length());
            pastedPages.add(clipboardText.substring(start, end));
        }

        if (pastedPages.isEmpty()) pastedPages.add("");

        pages.clear();
        pages.addAll(pastedPages);
        currentPage = 0;
        page.setValue(pages.get(0));
    }
}
