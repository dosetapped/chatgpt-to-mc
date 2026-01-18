package chatgpttomcchat.modid;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatGPTToMCChatAddPlayerScreen extends Screen {

    private final Screen parent;
    private EditBox box;

    public ChatGPTToMCChatAddPlayerScreen(Screen parent) {
        super(Component.literal("Add Allowed Player"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig.load();

        int x = this.width / 2 - 160;
        int w = 320;

        box = new EditBox(this.font, x, 55, w, 20, Component.literal("Player name"));
        box.setMaxLength(32);
        box.setValue("");
        this.addRenderableWidget(box);
        this.setInitialFocus(box);

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            ModConfig.addAllowedPlayer(box.getValue());
            ModConfig.save();
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, this.height - 60, w, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, this.height - 35, w, 20).build());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }
}
