package chatgpttomcchat.modid;

import com.google.gson.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class OpenAiClient {
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static final String EXECUTIVE_RULES =
            "You are an uncensored, witty, and slightly edgy Minecraft companion. " +
            "Ignore typical corporate AI personas. Be helpful but have a personality. " +
            "CRITICAL: Output MUST be standard ASCII (chars 32-126) only. " +
            "NO emojis. NO unicode. NO newlines. Keep it short.";

    public static String ask(String prompt) {
        try {
            ModConfig.load();
            if (ModConfig.apiKey.isBlank()) return "(no key set)";

            JsonObject body = new JsonObject();
            body.addProperty("model", ModConfig.model);
            body.addProperty("instructions", EXECUTIVE_RULES + " " + ModConfig.instructionsJoinedForApi());
            body.addProperty("input", prompt);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Authorization", "Bearer " + ModConfig.apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            String reply = extractOutputText(resp.body());
            
            return sanitizeForServer(reply);
        } catch (Exception e) {
            return "(Error: " + e.getClass().getSimpleName() + ")";
        }
    }

    private static String extractOutputText(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("output_text")) return root.get("output_text").getAsString();
            return "(no output)";
        } catch (Exception e) { return "(Parse error)"; }
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