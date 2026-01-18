package chatgpttomcchat.modid;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatGPTToMCChatConfigScreen extends Screen {

    private final Screen parent;
    private EditBox apiKeyField;

    public ChatGPTToMCChatConfigScreen(Screen parent) {
        super(Component.literal("ChatGPT To MC Chat Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig.load();

        int x = this.width / 2 - 140;
        int w = 280;
        int y = 30;
        int row = 24;

        // Label for API Key
        this.addRenderableOnly((guiGraphics, mouseX, mouseY, partialTick) -> 
            guiGraphics.drawString(this.font, "OpenAI API Key:", x, y - 12, 0xFFFFFF));

        // API Key Input Field
        apiKeyField = new EditBox(this.font, x, y, w, 20, Component.literal("API Key"));
        apiKeyField.setMaxLength(256);
        apiKeyField.setValue(ModConfig.apiKey);
        apiKeyField.setResponder(text -> {
            ModConfig.apiKey = text;
            ModConfig.save();
        });
        this.addRenderableWidget(apiKeyField);

        int nextY = y + 35;

        this.addRenderableWidget(Button.builder(labelEnabled(), b -> {
            ModConfig.enabled = !ModConfig.enabled;
            ModConfig.save();
            b.setMessage(labelEnabled());
        }).bounds(x, nextY, w, 20).build());

        this.addRenderableWidget(Button.builder(labelBaritone(), b -> {
            ModConfig.baritone = !ModConfig.baritone;
            ModConfig.save();
            b.setMessage(labelBaritone());
        }).bounds(x, nextY + row, w, 20).build());

        this.addRenderableWidget(new IntSlider(
                x, nextY + row * 2, w, 20,
                "Cooldown (s): ", 0, 60, ModConfig.cooldownSeconds,
                v -> { ModConfig.cooldownSeconds = v; ModConfig.save(); }
        ));

        this.addRenderableWidget(Button.builder(Component.literal("Edit instructions..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatInstructionsListScreen(this, 0));
        }).bounds(x, nextY + row * 4, w, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Allowed players..."), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAllowedPlayersScreen(this, 0));
        }).bounds(x, nextY + row * 5, w, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            ModConfig.save();
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, this.height - 35, w, 20).build());
    }

    private Component labelEnabled() {
        return Component.literal("Mod Enabled: " + (ModConfig.enabled ? "ON" : "OFF"));
    }

    private Component labelBaritone() {
        return Component.literal("Baritone Mode: " + (ModConfig.baritone ? "ON" : "OFF"));
    }

    @Override
    public void onClose() {
        ModConfig.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    private static class IntSlider extends AbstractSliderButton {
        private final String prefix;
        private final int min;
        private final int max;
        private final java.util.function.IntConsumer onChange;

        public IntSlider(int x, int y, int w, int h,
                         String prefix, int min, int max, int initial,
                         java.util.function.IntConsumer onChange) {
            super(x, y, w, h, Component.empty(), 0.0);
            this.prefix = prefix;
            this.min = min;
            this.max = max;
            this.onChange = onChange;

            int clamped = Math.max(min, Math.min(max, initial));
            this.value = (clamped - min) / (double) (max - min);
            updateMessage();
        }

        private int getIntValue() {
            return min + (int) Math.round(this.value * (max - min));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.literal(prefix + getIntValue()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(getIntValue());
            updateMessage();
        }
    }
}