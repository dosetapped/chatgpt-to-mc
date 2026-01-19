package chatgpttomcchat.modid;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.concurrent.CompletableFuture;

public class ChatGPTToMCChatAiMenuScreen extends Screen {
    private final Screen parent;
    private EditBox keyField;
    private String statusMessage = "Status: Ready";

    public ChatGPTToMCChatAiMenuScreen(Screen parent) {
        super(Component.literal("AI Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 140;
        int y = 40;

        this.addRenderableWidget(Button.builder(Component.literal("Model: " + ModConfig.model), b -> {
            ModConfig.model = ModConfig.model.equals("gpt-4.1") ? "gpt-4o" : "gpt-4.1";
            b.setMessage(Component.literal("Model: " + ModConfig.model));
            ModConfig.save();
        }).bounds(x, y, 280, 20).build());

        keyField = new EditBox(this.font, x, y + 30, 280, 20, Component.literal("API Key"));
        keyField.setMaxLength(256);
        keyField.setValue(ModConfig.apiKey);
        this.addRenderableWidget(keyField);

        this.addRenderableWidget(Button.builder(Component.literal("Test Connection"), b -> {
            statusMessage = "Testing...";
            CompletableFuture.runAsync(() -> {
                ModConfig.apiKey = keyField.getValue();
                String result = OpenAiClient.ask("Are you there?");
                statusMessage = result.contains("Error") ? "FAILED" : "SUCCESS!";
            });
        }).bounds(x, y + 55, 280, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Back & Save"), b -> {
            ModConfig.apiKey = keyField.getValue();
            ModConfig.save();
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, this.height - 35, 280, 20).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, statusMessage, this.width / 2, 130, 0xFFFFFF);
    }
}