package chatgpttomcchat.modid;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Desktop;
import java.net.URI;

public class ChatGPTToMCChatCreditsScreen extends Screen {

    private final Screen parent;

    public ChatGPTToMCChatCreditsScreen(Screen parent) {
        super(Component.literal("Info"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 200;
        int h = 20;
        int x = (this.width - w) / 2;

        // --- FEEDBACK BUTTON ---
        this.addRenderableWidget(Button.builder(Component.literal("Feedback / Issue Tracker"), b -> {
            openUrl("https://github.com/dosetapped/chatgpt-to-mc/issues");
        }).bounds(x, 170, w, h).build());

        // --- BACK BUTTON ---
        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, this.height - 40, w, h).build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Black Background
        context.fill(0, 0, this.width, this.height, 0xFF000000);

        super.render(context, mouseX, mouseY, delta);
        
        int center = this.width / 2;
        int white = 0xFFFFFFFF;
        int green = 0xFF55FF55;
        
        // Title
        context.drawCenteredString(this.font, Component.literal("INFO"), center, 40, white);

        // Content
        context.drawCenteredString(this.font, Component.literal("Mod by: dosetap"), center, 70, green);
        context.drawCenteredString(this.font, Component.literal("Version: beta-1.1.0"), center, 90, white);
        
        context.drawCenteredString(this.font, Component.literal("Thanks for playing!"), center, 140, white);
    }

    private void openUrl(String url) {
        try {
            // Method 1: Try Java Desktop API (works on Windows, Mac, Linux with GUI)
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }
        } catch (Exception e) {
            // Fall through to next method
        }

        try {
            // Method 2: Try using ProcessBuilder (works on Windows, Mac, Linux)
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                // Windows
                pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
            } else if (os.contains("mac")) {
                // macOS
                pb = new ProcessBuilder("open", url);
            } else {
                // Linux and others
                pb = new ProcessBuilder("xdg-open", url);
            }
            pb.start();
        } catch (Exception e) {
            // If all methods fail, show error message to player
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(
                    Component.literal("§cFailed to open link. Please visit: " + url), 
                    false
                );
            }
        }
    }
}