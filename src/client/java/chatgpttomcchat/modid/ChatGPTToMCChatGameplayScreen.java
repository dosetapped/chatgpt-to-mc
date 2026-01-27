package chatgpttomcchat.modid;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatGPTToMCChatGameplayScreen extends Screen {
    private final Screen parent;

    public ChatGPTToMCChatGameplayScreen(Screen parent) {
        super(Component.literal("Gameplay Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 280;
        int h = 20;
        int p = 6; // Padding
        int x = this.width / 2 - (w / 2);
        int y = this.height / 4 + 20;

        // Toggles
        this.addRenderableWidget(Button.builder(labelContext(), b -> {
            ModConfig.contextAwareness = !ModConfig.contextAwareness;
            b.setMessage(labelContext());
            ModConfig.save();
        }).bounds(x, y, w, h).build());
        y += h + p;

        this.addRenderableWidget(Button.builder(labelBaritone(), b -> {
            ModConfig.baritone = !ModConfig.baritone;
            b.setMessage(labelBaritone());
            ModConfig.save();
        }).bounds(x, y, w, h).build());
        y += h + p;

        // Sliders
        this.addRenderableWidget(new IntSlider(x, y, w, h, "Cooldown (s): ", 0, 60, ModConfig.cooldownSeconds, v -> { ModConfig.cooldownSeconds = v; ModConfig.save(); }));
        y += h + p;

        this.addRenderableWidget(new IntSlider(x, y, w, h, "Max prompt chars: ", 50, 4000, ModConfig.maxPromptChars, v -> { ModConfig.maxPromptChars = v; ModConfig.save(); }));
        y += h + p;

        this.addRenderableWidget(new IntSlider(x, y, w, h, "Max reply chars: ", 50, 500, ModConfig.maxReplyChars, v -> { ModConfig.maxReplyChars = v; ModConfig.save(); }));
        y += h + p * 2;

        // Back Button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            ModConfig.save();
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, y, w, h).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 40, 0xFFFFFF);
    }

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