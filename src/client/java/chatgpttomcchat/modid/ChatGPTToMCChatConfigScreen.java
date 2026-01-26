package chatgpttomcchat.modid;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatGPTToMCChatConfigScreen extends Screen {
    private final Screen parent;

    private Button creditsButton;

    public ChatGPTToMCChatConfigScreen(Screen parent) {
        super(Component.literal("ChatGPT To MC Chat"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig.load();
        
        int w = 280; 
        int h = 20;  
        int p = 4;   
        int x = this.width / 2 - (w / 2); 
        
        // Calculate dynamic centering to prevent overlap
        int totalContentHeight = (h + p) * 11 + 10; 
        int y = (this.height - totalContentHeight) / 2;

        // --- CREDITS BUTTON ---
        int btnSize = 20;
        this.creditsButton = Button.builder(Component.literal("ℹ"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatCreditsScreen(this));
        }).bounds(this.width - btnSize - 10, 10, btnSize, btnSize).build();

        this.addRenderableWidget(this.creditsButton);

        // --- MAIN MENU ---
        this.addRenderableWidget(Button.builder(Component.literal("AI Settings..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAiMenuScreen(this));
        }).bounds(x, y, w, h).build());
        y += h + p;

        this.addRenderableWidget(Button.builder(labelEnabled(), b -> {
            ModConfig.enabled = !ModConfig.enabled;
            b.setMessage(labelEnabled());
            ModConfig.save();
        }).bounds(x, y, w, h).build());
        y += h + p;

        this.addRenderableWidget(Button.builder(labelBaritone(), b -> {
            ModConfig.baritone = !ModConfig.baritone;
            b.setMessage(labelBaritone());
            ModConfig.save();
        }).bounds(x, y, w, h).build());
        y += h + p;

        this.addRenderableWidget(Button.builder(labelContext(), b -> {
            ModConfig.contextAwareness = !ModConfig.contextAwareness;
            b.setMessage(labelContext());
            ModConfig.save();
        }).bounds(x, y, w, h).build());
        y += h + p;

        this.addRenderableWidget(new IntSlider(x, y, w, h, "Cooldown (s): ", 0, 60, ModConfig.cooldownSeconds, v -> { ModConfig.cooldownSeconds = v; ModConfig.save(); }));
        y += h + p;
        
        this.addRenderableWidget(new IntSlider(x, y, w, h, "Max prompt chars: ", 50, 4000, ModConfig.maxPromptChars, v -> { ModConfig.maxPromptChars = v; ModConfig.save(); }));
        y += h + p;
        
        this.addRenderableWidget(new IntSlider(x, y, w, h, "Max reply chars: ", 50, 500, ModConfig.maxReplyChars, v -> { ModConfig.maxReplyChars = v; ModConfig.save(); }));
        y += h + p;

        y += 10; 

        this.addRenderableWidget(Button.builder(Component.literal("Edit instructions..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatInstructionsListScreen(this, 0));
        }).bounds(x, y, w, h).build());
        y += h + p;

        this.addRenderableWidget(Button.builder(Component.literal("Allowed players..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAllowedPlayersScreen(this, 0));
        }).bounds(x, y, w, h).build());
        y += h + p;

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            ModConfig.save();
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, y, w, h).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        // Credits button now uses text (ℹ) instead of texture
    }

    private Component labelEnabled() { return Component.literal("Mod Enabled: " + (ModConfig.enabled ? "ON" : "OFF")); }
    private Component labelBaritone() { return Component.literal("Baritone mode: " + (ModConfig.baritone ? "ON" : "OFF")); }
    private Component labelContext() { return Component.literal("Context (Coords): " + (ModConfig.contextAwareness ? "ON" : "OFF")); }

    private static class IntSlider extends AbstractSliderButton {
        private final String prefix; private final int min; private final int max; private final java.util.function.IntConsumer onChange;
        public IntSlider(int x, int y, int w, int h, String p, int min, int max, int init, java.util.function.IntConsumer c) {
            super(x, y, w, h, Component.empty(), 0.0); this.prefix = p; this.min = min; this.max = max; this.onChange = c;
            this.value = (Math.max(min, Math.min(max, init)) - min) / (double) (max - min); updateMessage();
        }
        @Override protected void updateMessage() { this.setMessage(Component.literal(prefix + (min + (int) Math.round(this.value * (max - min))))); }
        @Override protected void applyValue() { onChange.accept(min + (int) Math.round(this.value * (max - min))); updateMessage(); }
    }
}