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
        int y = 28;
        int row = 24;

        this.addRenderableWidget(Button.builder(Component.literal("AI Settings..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAiMenuScreen(this));
        }).bounds(x, y, 280, 20).build());

        this.addRenderableWidget(Button.builder(labelEnabled(), b -> {
            ModConfig.enabled = !ModConfig.enabled;
            b.setMessage(labelEnabled());
            ModConfig.save();
        }).bounds(x, y + row, 280, 20).build());

        this.addRenderableWidget(new IntSlider(x, y + row * 3, 280, 20, "Cooldown (s): ", 0, 60, ModConfig.cooldownSeconds, v -> { ModConfig.cooldownSeconds = v; ModConfig.save(); }));

        this.addRenderableWidget(Button.builder(Component.literal("Edit instructions..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatInstructionsListScreen(this, 0));
        }).bounds(x, y + row * 5, 280, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            ModConfig.save();
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, this.height - 35, 280, 20).build());
    }

    private Component labelEnabled() { return Component.literal("Mod Enabled: " + (ModConfig.enabled ? "ON" : "OFF")); }

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