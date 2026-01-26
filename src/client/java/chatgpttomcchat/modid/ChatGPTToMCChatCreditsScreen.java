package chatgpttomcchat.modid;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatGPTToMCChatCreditsScreen extends Screen {

    private final Screen parent;

    public ChatGPTToMCChatCreditsScreen(Screen parent) {
        super(Component.literal("Credits"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 200;
        int h = 20;
        int x = this.width / 2 - (w / 2);

        // Back Button
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, this.height - 40, w, h).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        context.drawCenteredString(this.font, "Credits", this.width / 2, 40, 0xFFFFFF);

        // Placeholder Text
        int y = 70;
        int spacing = 15;

        context.drawCenteredString(this.font, "Created by dosetap", this.width / 2, y, 0xAAAAAA);
        y += spacing;
        
        context.drawCenteredString(this.font, "Development: You", this.width / 2, y, 0xAAAAAA);
        y += spacing;
        
        context.drawCenteredString(this.font, "Design: You", this.width / 2, y, 0xAAAAAA);
    }
}