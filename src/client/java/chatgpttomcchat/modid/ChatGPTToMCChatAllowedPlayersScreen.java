package chatgpttomcchat.modid;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatGPTToMCChatAllowedPlayersScreen extends Screen {

    private final Screen parent;
    private int page;
    private static final int PER_PAGE = 7;

    public ChatGPTToMCChatAllowedPlayersScreen(Screen parent, int page) {
        super(Component.literal("Allowed Players"));
        this.parent = parent;
        this.page = Math.max(0, page);
    }

    @Override
    protected void init() {
        ModConfig.load();

        int x = this.width / 2 - 160;
        int w = 320;

        this.addRenderableWidget(Button.builder(labelAllowlist(), b -> {
            ModConfig.allowlistEnabled = !ModConfig.allowlistEnabled;
            ModConfig.save();
            b.setMessage(labelAllowlist());
        }).bounds(x, 20, w, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("+ Add player"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAddPlayerScreen(this));
        }).bounds(x, 44, w, 20).build());

        int total = ModConfig.allowedPlayers.size();
        int maxPage = Math.max(0, (total - 1) / PER_PAGE);
        if (page > maxPage) page = maxPage;

        int start = page * PER_PAGE;
        int end = Math.min(total, start + PER_PAGE);

        int y = 75;
        for (int i = start; i < end; i++) {
            int idx = i;
            String name = ModConfig.allowedPlayers.get(i);

            this.addRenderableWidget(Button.builder(Component.literal(name), b -> {})
                    .bounds(x, y, w - 44, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("X"), b -> {
                ModConfig.removeAllowedPlayer(ModConfig.allowedPlayers.get(idx));
                ModConfig.save();
                if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAllowedPlayersScreen(parent, page));
            }).bounds(x + w - 40, y, 40, 20).build());

            y += 24;
        }

        // Page controls
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAllowedPlayersScreen(parent, Math.max(0, page - 1)));
        }).bounds(x, this.height - 60, 40, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(new ChatGPTToMCChatAllowedPlayersScreen(parent, Math.min(maxPage, page + 1)));
        }).bounds(x + 44, this.height - 60, 40, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> {
            if (this.minecraft != null) this.minecraft.setScreen(parent);
        }).bounds(x, this.height - 35, w, 20).build());
    }

    private Component labelAllowlist() {
        return Component.literal("Allowlist enabled: " + (ModConfig.allowlistEnabled ? "ON" : "OFF"));
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }
}
