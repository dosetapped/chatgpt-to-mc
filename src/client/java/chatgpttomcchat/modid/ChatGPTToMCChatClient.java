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

            // 1. Get the sender's name safely from the chat parameters
            String senderName = "System";
            if (params != null && params.name() != null) {
                senderName = params.name().getString();
            }
            
            if (!ModConfig.isPlayerAllowed(senderName)) return;

            // 2. Extract the message content
            String fullText = message.getString();
            String lowerFull = fullText.toLowerCase(Locale.ROOT);
            
            int triggerIdx = lowerFull.indexOf(TRIGGER);
            if (triggerIdx == -1) return;

            // 3. Extract the prompt (everything after "hey chatgpt")
            String prompt = fullText.substring(triggerIdx + TRIGGER.length()).trim();
            if (prompt.isBlank()) {
                showLocalNotice("Error: No prompt provided after 'Hey ChatGPT'.");
                return;
            }

            // 4. Handle Cooldown
            long now = System.currentTimeMillis();
            long cooldownMs = ModConfig.cooldownSeconds * 1000L;
            Long last = lastUseByName.get(senderName);

            if (last != null && now - last < cooldownMs) {
                showLocalNotice("Cooldown: Wait " + ((cooldownMs - (now - last)) / 1000) + "s.");
                return;
            }
            lastUseByName.put(senderName, now);

            // 5. Gather Context and Call AI
            CompletableFuture.runAsync(() -> {
                String context = "Unknown Location";
                Minecraft mc = Minecraft.getInstance();
                
                if (mc.player != null && mc.level != null) {
                    int x = (int) mc.player.getX();
                    int y = (int) mc.player.getY();
                    int z = (int) mc.player.getZ();
                    
                    // Get biome name (e.g., minecraft:plains)
                    String biome = mc.level.getBiome(mc.player.blockPosition()).getRegisteredName();
                    context = String.format("Coordinates: X=%d, Y=%d, Z=%d | Biome: %s", x, y, z, biome);
                }

                String reply = OpenAiClient.ask(prompt, context);
                
                if (reply != null) {
                    if (reply.startsWith("(")) {
                        showLocalNotice("Connection Error: " + reply);
                    } else {
                        sendChatAsYou(ModConfig.baritone ? reply : "ChatGPT: " + reply);
                    }
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
}