package chatgpttomcchat.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ChatGPTToMCChat implements ModInitializer {

    public static final String MOD_ID = "chatgpt-to-mc-chat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // per-player cooldown tracking
    private static final Map<UUID, Long> LAST_USE = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        ModConfig.load();
        LOGGER.info("ChatGPTToMCChat loaded");

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (!ModConfig.enabled) return;

            String raw = message.signedContent();
            if (raw == null) return;

            // case-insensitive trigger
            if (!raw.toLowerCase().startsWith("hey chatgpt")) return;

            UUID id = sender.getUUID();
            long now = System.currentTimeMillis();
            long cooldownMs = ModConfig.cooldownSeconds * 1000L;

            long last = LAST_USE.getOrDefault(id, 0L);
            if (now - last < cooldownMs) {
                long remaining = (cooldownMs - (now - last)) / 1000;
                String name = sender.getName().getString();
                sender.sendSystemMessage(
                        Component.literal("⏳ " + name + ", please wait " + remaining + "s before using again.")
                );
                return;
            }

            LAST_USE.put(id, now);

            // remove the trigger phrase from the prompt
            String prompt = raw.substring("hey chatgpt".length()).trim();
            if (prompt.isEmpty()) {
                sender.sendSystemMessage(Component.literal("Ask me something after saying Hey ChatGPT 🙂"));
                return;
            }

            // async OpenAI call (never block server thread)
            // PASSING "Server Side" AS CONTEXT to satisfy the new (String, String) signature
            CompletableFuture
                    .supplyAsync(() -> OpenAiClient.ask(prompt, "Server Side"))
                    .thenAccept(reply -> {
                        sender.sendSystemMessage(
                                Component.literal("🤖 ChatGPT: " + reply)
                        );
                    });
        });
    }
}