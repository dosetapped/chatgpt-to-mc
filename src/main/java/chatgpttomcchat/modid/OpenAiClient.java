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
            "You are controlling chat in Minecraft. " +
            "CRITICAL: Output MUST be ASCII only (characters 32-126). " +
            "NO emojis. NO unicode. NO newlines. " +
            "No markdown. Keep it short.";

    private static final String BARITONE_EXECUTIVE_RULES =
            "Baritone mode is ENABLED. " +
            "Respond with ONLY the Baritone command starting with '#' (hash) and nothing else. " +
            "No 'ChatGPT:' prefix, no explanations. " +
            "Examples: #mine diamond_ore  |  #goto 100 64 200  |  #follow playerName";

    public static String ask(String prompt) {
        try {
            ModConfig.load();

            if (ModConfig.apiKey == null || ModConfig.apiKey.isBlank()) {
                return "(no apiKey set)";
            }

            if (prompt == null) prompt = "";
            if (prompt.length() > ModConfig.maxPromptChars) {
                prompt = prompt.substring(0, ModConfig.maxPromptChars);
            }

            String userRules = ModConfig.instructionsJoinedForApi();
            String instructions = EXECUTIVE_RULES
                    + (ModConfig.baritone ? (" " + BARITONE_EXECUTIVE_RULES) : "")
                    + (userRules.isBlank() ? "" : (" " + userRules));

            JsonObject body = new JsonObject();
            body.addProperty("model", (ModConfig.model == null || ModConfig.model.isBlank()) ? "gpt-4.1" : ModConfig.model);
            body.addProperty("instructions", instructions);
            body.addProperty("input", prompt);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Authorization", "Bearer " + ModConfig.apiKey.trim())
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (resp.statusCode() / 100 != 2) {
                return "(OpenAI error " + resp.statusCode() + ")";
            }

            String reply = extractOutputText(resp.body());
            if (reply == null || reply.isBlank()) return "(empty response)";

            reply = sanitizeForServer(reply);

            if (ModConfig.baritone) {
                reply = stripChatGptPrefix(reply);

                if (!reply.startsWith("#")) {
                    int hash = reply.indexOf('#');
                    if (hash >= 0) {
                        reply = reply.substring(hash).trim();
                        reply = sanitizeForServer(reply);
                    }
                }

                if (!reply.startsWith("#")) {
                    return "(no baritone command)";
                }
            }

            if (reply.length() > ModConfig.maxReplyChars) {
                reply = reply.substring(0, ModConfig.maxReplyChars);
                reply = sanitizeForServer(reply);
            }

            return reply.isBlank() ? "(reply blocked)" : reply;

        } catch (Exception e) {
            return "(OpenAI exception: " + e.getClass().getSimpleName() + ")";
        }
    }

    private static String extractOutputText(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            if (root.has("output_text") && root.get("output_text").isJsonPrimitive()) {
                return root.get("output_text").getAsString();
            }

            if (!root.has("output") || !root.get("output").isJsonArray()) return null;
            JsonArray output = root.getAsJsonArray("output");

            StringBuilder sb = new StringBuilder();
            for (JsonElement outEl : output) {
                if (!outEl.isJsonObject()) continue;
                JsonObject outObj = outEl.getAsJsonObject();
                if (!outObj.has("content") || !outObj.get("content").isJsonArray()) continue;

                JsonArray content = outObj.getAsJsonArray("content");
                for (JsonElement cEl : content) {
                    if (!cEl.isJsonObject()) continue;
                    JsonObject cObj = cEl.getAsJsonObject();
                    if (cObj.has("text") && cObj.get("text").isJsonPrimitive()) {
                        sb.append(cObj.get("text").getAsString());
                    }
                }
            }

            String s = sb.toString();
            return s.isBlank() ? null : s;

        } catch (Exception ignored) {
            return null;
        }
    }

    public static String sanitizeForServer(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length());
        boolean lastWasSpace = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '\n' || c == '\r' || c == '\t') c = ' ';
            if (c < 32 || c > 126) continue;

            if (c == ' ') {
                if (lastWasSpace) continue;
                lastWasSpace = true;
            } else lastWasSpace = false;

            out.append(c);
        }

        return out.toString().trim();
    }

    private static String stripChatGptPrefix(String s) {
        if (s == null) return "";
        String t = s.trim();
        String lower = t.toLowerCase(Locale.ROOT);

        if (lower.startsWith("chatgpt:")) return t.substring(8).trim();
        if (lower.startsWith("chatgpt -")) return t.substring(8).trim();
        if (lower.startsWith("chatgpt")) {
            int idx = t.indexOf(' ');
            if (idx > 0) return t.substring(idx + 1).trim();
        }
        return t;
    }
}
