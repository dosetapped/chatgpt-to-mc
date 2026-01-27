package chatgpttomcchat.modid;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatGPTToMCChatConfigScreen extends Screen {
    private final Screen parent;

    public ChatGPTToMCChatConfigScreen(Screen parent) {
        super(Component.literal("ChatGPT To MC Chat"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig.load();
        
        int w = 310; // Total width of our button block
        int btnW = 150; // Width of the split buttons
        int h = 20;     // Button height
        int p = 10;     // Padding between rows (more space is cleaner)
        
        int xCenter = this.width / 2;
        int y = this.height / 4 + 20; // Start 1/4th down the screen

        // --- 1. CREDITS BUTTON (Top Right) ---
        int credSize = 20;
        this.addRenderableWidget(Button.builder(Component.literal("ℹ"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatCreditsScreen(this));
        }).bounds(this.width - credSize - 10, 10, credSize, credSize).build());

        // --- 2. TITLE (Visual only, rendered later) ---

        // --- 3. MAIN TOGGLE (Full Width) ---
        this.addRenderableWidget(Button.builder(labelEnabled(), b -> {
            ModConfig.enabled = !ModConfig.enabled;
            b.setMessage(labelEnabled());
            ModConfig.save();
        }).bounds(xCenter - (w / 2), y, w, h).build());
        y += h + p;

        // --- 4. CATEGORY GRID (2x2 Buttons) ---
        
        // Row A: AI Settings & Gameplay Settings
        this.addRenderableWidget(Button.builder(Component.literal("AI Configuration..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAiMenuScreen(this));
        }).bounds(xCenter - (w / 2), y, btnW, h).build());

        this.addRenderableWidget(Button.builder(Component.literal("Gameplay & Limits..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatGameplayScreen(this));
        }).bounds(xCenter - (w / 2) + btnW + 10, y, btnW, h).build());
        y += h + p;

        // Row B: Instructions & Permissions
        this.addRenderableWidget(Button.builder(Component.literal("Edit Instructions..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatInstructionsListScreen(this, 0));
        }).bounds(xCenter - (w / 2), y, btnW, h).build());

        this.addRenderableWidget(Button.builder(Component.literal("Allowed Players..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAllowedPlayersScreen(this, 0));
        }).bounds(xCenter - (w / 2) + btnW + 10, y, btnW, h).build());
        y += h + p * 2; // Extra gap before "Done"

        // --- 5. DONE BUTTON ---
        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            ModConfig.save();
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(xCenter - (w / 2), y, w, h).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 40, 0xFFFFFF);
    }

    private Component labelEnabled() { return Component.literal("Mod Enabled: " + (ModConfig.enabled ? "ON" : "OFF")); }
}