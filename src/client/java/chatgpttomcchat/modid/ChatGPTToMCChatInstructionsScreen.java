package chatgpttomcchat.modid;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatGPTToMCChatInstructionsScreen extends Screen {

    private final Screen parent;

    public ChatGPTToMCChatInstructionsScreen(Screen parent) {
        super(Component.literal("Instructions"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // We no longer use a single textbox screen.
        // Immediately forward to the list-based instructions UI.
        if (this.minecraft != null) {
            this.minecraft.setScreen(new ChatGPTToMCChatInstructionsListScreen(parent, 0));
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }
}
