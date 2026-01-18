package chatgpttomcchat.modid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatGPTToMCChatClient implements ClientModInitializer {

    private static final Map<String, Long> lastUseByName = new ConcurrentHashMap<>();
    private static final String TRIGGER = "hey chatgpt";

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            try {
                if (!ModConfig.enabled) return;

                String raw = message.getString();
                if (raw == null || raw.isBlank()) return;

                ParsedChat pc = parseChatLine(raw);

                // Allowlist check
                if (!ModConfig.isPlayerAllowed(pc.name)) {
                    return;
                }

                String content = pc.content;
                if (content == null || content.isBlank()) return;

                String lower = content.toLowerCase(Locale.ROOT);
                int idx = lower.indexOf(TRIGGER);
                if (idx < 0) return;

                if (!lower.trim().startsWith(TRIGGER)) return;

                String prompt = content.substring(idx + TRIGGER.length()).trim();
                if (prompt.isBlank()) {
                    showLocalNotice(pc.name + ", ask something after 'Hey ChatGPT'");
                    return;
                }

                long now = System.currentTimeMillis();
                long cooldownMs = ModConfig.cooldownSeconds * 1000L;
                String who = pc.name;

                Long last = lastUseByName.get(who);
                if (last != null && now - last < cooldownMs) {
                    long remaining = (cooldownMs - (now - last) + 999) / 1000;
                    showLocalNotice(who + ", please wait " + remaining + "s before using again");
                    return;
                }
                lastUseByName.put(who, now);

                new Thread(() -> {
                    try {
                        String reply = OpenAiClient.ask(prompt);
                        if (reply == null || reply.isBlank()) {
                            showLocalNotice("AI returned empty reply");
                            return;
                        }

                        if (ModConfig.baritone) {
                            reply = OpenAiClient.sanitizeForServer(reply);
                            if (!reply.startsWith("#")) {
                                showLocalNotice("Baritone mode: blocked non-command reply");
                                return;
                            }
                            sendChatAsYou(reply);
                            return;
                        }

                        sendChatAsYou("ChatGPT: " + reply);

                    } catch (Exception e) {
                        showLocalNotice("AI thread error: " + e.getClass().getSimpleName());
                    }
                }, "ChatGPTToMCChat-AI").start();

            } catch (Exception e) {
                showLocalNotice("Chat handler error: " + e.getClass().getSimpleName());
            }
        });

        showLocalNotice("ChatGPTToMCChat client ready");
    }

    private static void sendChatAsYou(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        text = OpenAiClient.sanitizeForServer(text);
        if (text.isBlank()) return;

        if (text.length() > 240) {
            text = text.substring(0, 240);
            text = OpenAiClient.sanitizeForServer(text);
        }

        mc.player.connection.sendChat(text);
    }

    private static void showLocalNotice(String msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        mc.player.displayClientMessage(Component.literal("[ChatGPTToMCChat] " + msg), false);
    }

    private static ParsedChat parseChatLine(String raw) {
        String r = raw.trim();
        if (r.startsWith("<")) {
            int end = r.indexOf('>');
            if (end > 1 && end + 1 < r.length()) {
                String name = r.substring(1, end).trim();
                String content = r.substring(end + 1).trim();
                if (content.startsWith(":")) content = content.substring(1).trim();
                return new ParsedChat(name.isBlank() ? "player" : name, content);
            }
        }
        return new ParsedChat("player", r);
    }

    private static class ParsedChat {
        final String name;
        final String content;

        ParsedChat(String name, String content) {
            this.name = name;
            this.content = content;
        }
    }
}
