package chatgpttomcchat.modid;

import net.minecraft.client.gui.components.AbstractSliderButton;
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
        int x = this.width / 2 - 140;
        int w = 280;
        int y = 20;
        int row = 24;

        this.addRenderableWidget(Button.builder(Component.literal("AI Settings..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAiMenuScreen(this));
        }).bounds(x, y, w, 20).build());

        this.addRenderableWidget(Button.builder(labelEnabled(), b -> {
            ModConfig.enabled = !ModConfig.enabled;
            b.setMessage(labelEnabled());
            ModConfig.save();
        }).bounds(x, y + row, w, 20).build());

        this.addRenderableWidget(Button.builder(labelBaritone(), b -> {
            ModConfig.baritone = !ModConfig.baritone;
            b.setMessage(labelBaritone());
            ModConfig.save();
        }).bounds(x, y + row * 2, w, 20).build());
        
        // NEW BUTTON
        this.addRenderableWidget(Button.builder(labelContext(), b -> {
            ModConfig.contextAwareness = !ModConfig.contextAwareness;
            b.setMessage(labelContext());
            ModConfig.save();
        }).bounds(x, y + row * 3, w, 20).build());

        this.addRenderableWidget(new IntSlider(x, y + row * 4, w, 20, "Cooldown (s): ", 0, 60, ModConfig.cooldownSeconds, v -> { ModConfig.cooldownSeconds = v; ModConfig.save(); }));
        this.addRenderableWidget(new IntSlider(x, y + row * 5, w, 20, "Max prompt chars: ", 50, 4000, ModConfig.maxPromptChars, v -> { ModConfig.maxPromptChars = v; ModConfig.save(); }));
        this.addRenderableWidget(new IntSlider(x, y + row * 6, w, 20, "Max reply chars: ", 50, 500, ModConfig.maxReplyChars, v -> { ModConfig.maxReplyChars = v; ModConfig.save(); }));

        this.addRenderableWidget(Button.builder(Component.literal("Edit instructions..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatInstructionsListScreen(this, 0));
        }).bounds(x, y + row * 8, w, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Allowed players..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAllowedPlayersScreen(this, 0));
        }).bounds(x, y + row * 9, w, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            ModConfig.save();
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, this.height - 35, w, 20).build());
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