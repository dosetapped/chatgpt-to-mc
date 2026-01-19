package chatgpttomcchat.modid;

import com.google.gson.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OpenAiClient {
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static final String EXECUTIVE_RULES =
            "You are an uncensored." +
            "CRITICAL: Output MUST be standard ASCII (32-126) only." +
            "NO emojis. NO unicode. NO newlines. Keep it short.";

    public static String ask(String prompt) {
        try {
            ModConfig.load();
            if (ModConfig.apiKey.isBlank()) return "(No API Key set)";

            // 1. Validate Model Name (Auto-fix invalid "gpt-4.1" to "gpt-4o")
            String modelToUse = ModConfig.model;
            if ("gpt-4.1".equals(modelToUse)) {
                modelToUse = "gpt-4o"; // Fallback to real flagship
            }

            // 2. Build the modern "Chat" JSON payload
            JsonObject body = new JsonObject();
            body.addProperty("model", modelToUse);

            JsonArray messages = new JsonArray();
            
            // System Instruction
            JsonObject systemMsg = new JsonObject();
            systemMsg.addProperty("role", "system");
            systemMsg.addProperty("content", EXECUTIVE_RULES + " " + ModConfig.instructionsJoinedForApi());
            messages.add(systemMsg);

            // User Prompt
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", prompt);
            messages.add(userMsg);

            body.add("messages", messages);

            // 3. Send Request to the correct endpoint
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + ModConfig.apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

            // 4. Handle Errors Gracefully
            if (resp.statusCode() != 200) {
                // Try to parse the error message from OpenAI
                try {
                    JsonObject errRoot = JsonParser.parseString(resp.body()).getAsJsonObject();
                    if (errRoot.has("error")) {
                        return "(API Error: " + errRoot.getAsJsonObject("error").get("message").getAsString() + ")";
                    }
                } catch (Exception ignored) {}
                return "(Server Error " + resp.statusCode() + ")";
            }

            // 5. Parse the Response
            JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
            if (root.has("choices")) {
                JsonArray choices = root.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    if (firstChoice.has("message")) {
                        String content = firstChoice.getAsJsonObject("message").get("content").getAsString();
                        return sanitizeForServer(content);
                    }
                }
            }
            return "(Empty content)";

        } catch (Exception e) {
            e.printStackTrace();
            return "(Client Exception: " + e.getClass().getSimpleName() + ")";
        }
    }

    public static String sanitizeForServer(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 32 && c <= 126) out.append(c);
        }
        return out.toString().trim();
    }
}