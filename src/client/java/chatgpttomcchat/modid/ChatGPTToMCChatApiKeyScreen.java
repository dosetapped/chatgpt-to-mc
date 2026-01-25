package chatgpttomcchat.modid;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.concurrent.CompletableFuture;

public class ChatGPTToMCChatApiKeyScreen extends Screen {
    private final Screen parent;
    private EditBox keyField;
    private String statusMessage = "Waiting for input...";
    private int statusColor = 0xFFFFFF;

    public ChatGPTToMCChatApiKeyScreen(Screen parent) {
        super(Component.literal("API Key Management"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 140;
        int w = 280;
        int y = 60;

        // API Key Input
        keyField = new EditBox(this.font, x, y, w, 20, Component.literal("Paste API Key here"));
        keyField.setMaxLength(256);
        keyField.setValue(ModConfig.apiKey);
        this.addRenderableWidget(keyField);

        // Test Connection Button
        this.addRenderableWidget(Button.builder(Component.literal("Test Connection"), b -> {
            this.statusMessage = "Testing...";
            this.statusColor = 0xFFFF00;
            
            // Run in a background thread so the game doesn't freeze
            CompletableFuture.supplyAsync(() -> {
                ModConfig.apiKey = keyField.getValue();
                // PASSING "API Test" AS CONTEXT to satisfy the new signature
                return OpenAiClient.ask("test", "API Test");
            }).thenAccept(result -> {
                if (this.minecraft != null) {
                    this.minecraft.execute(() -> {
                        if (result.contains("error") || result.contains("exception") || result.contains("no apiKey set")) {
                            this.statusMessage = "FAILED: " + result;
                            this.statusColor = 0xFF5555;
                        } else {
                            this.statusMessage = "SUCCESS! Connection established.";
                            this.statusColor = 0x55FF55;
                        }
                    });
                }
            });
        }).bounds(x, y + 25, w, 20).build());

        // Back & Save Button
        this.addRenderableWidget(Button.builder(Component.literal("Back & Save"), b -> {
            ModConfig.apiKey = keyField.getValue();
            ModConfig.save();
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, this.height - 35, w, 20).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int x = this.width / 2 - 140;
        context.drawString(this.font, "Enter OpenAI API Key:", x, 48, 0xFFFFFF);
        context.drawCenteredString(this.font, statusMessage, this.width / 2, 110, statusColor);
    }
}