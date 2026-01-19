package chatgpttomcchat.modid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class ChatGPTToMCChatClient implements ClientModInitializer {

    private static final Map<String, Long> lastUseByName = new ConcurrentHashMap<>();
    private static final String TRIGGER = "hey chatgpt";

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (!ModConfig.enabled) return;

            String raw = message.getString();
            if (raw == null || raw.isBlank()) return;

            ParsedChat pc = parseChatLine(raw);
            if (!ModConfig.isPlayerAllowed(pc.name)) return;

            String content = pc.content;
            String lower = content.toLowerCase(Locale.ROOT);
            if (!lower.trim().startsWith(TRIGGER)) return;

            String prompt = content.substring(lower.indexOf(TRIGGER) + TRIGGER.length()).trim();
            if (prompt.isBlank()) {
                showLocalNotice("Error: No prompt provided after 'Hey ChatGPT'.");
                return;
            }

            long now = System.currentTimeMillis();
            long cooldownMs = ModConfig.cooldownSeconds * 1000L;
            Long last = lastUseByName.get(pc.name);

            if (last != null && now - last < cooldownMs) {
                showLocalNotice("Cooldown: Wait " + ((cooldownMs - (now - last)) / 1000) + "s.");
                return;
            }
            lastUseByName.put(pc.name, now);

            CompletableFuture.runAsync(() -> {
                String reply = OpenAiClient.ask(prompt);
                
                // If the reply starts with '(', it's an error from OpenAiClient
                if (reply.startsWith("(")) {
                    showLocalNotice("Connection Error: " + reply);
                } else {
                    sendChatAsYou(ModConfig.baritone ? reply : "ChatGPT: " + reply);
                }
            });
        });

        showLocalNotice("Client initialized and ready.");
    }

    private static void sendChatAsYou(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.player.connection.sendChat(text);
        }
    }

    public static void showLocalNotice(String msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§7[ChatGPT] §f" + msg), false);
        }
    }

    private static ParsedChat parseChatLine(String raw) {
        String r = raw.trim();
        if (r.startsWith("<")) {
            int end = r.indexOf('>');
            if (end > 1) {
                return new ParsedChat(r.substring(1, end).trim(), r.substring(end + 1).trim());
            }
        }
        return new ParsedChat("player", r);
    }

    private static class ParsedChat {
        final String name;
        final String content;
        ParsedChat(String n, String c) { this.name = n; this.content = c; }
    }
}