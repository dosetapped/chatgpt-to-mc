package chatgpttomcchat.modid;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatGPTToMCChatAddInstructionScreen extends Screen {

    private final Screen parent;
    private EditBox box;

    public ChatGPTToMCChatAddInstructionScreen(Screen parent) {
        super(Component.literal("Add Instruction"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig.load();

        int x = this.width / 2 - 160;
        int w = 320;

        box = new EditBox(this.font, x, 55, w, 20, Component.literal("Instruction"));
        box.setMaxLength(500);
        box.setValue("");
        this.addRenderableWidget(box);
        this.setInitialFocus(box);

        this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            ModConfig.addInstruction(box.getValue());
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
