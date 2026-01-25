package chatgpttomcchat.modid;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ModConfig {

    // Toggles
    public static boolean enabled = true;
    public static boolean baritone = false;
    public static boolean contextAwareness = true; // NEW TOGGLE

    // Allowlist
    public static boolean allowlistEnabled = false;
    public static final List<String> allowedPlayers = new ArrayList<>();

    // OpenAI
    public static String apiKey = "";
    public static String model = "gpt-4.1";

    // Limits / behavior
    public static int cooldownSeconds = 5;
    public static int maxPromptChars = 600;
    public static int maxReplyChars = 240;

    // Instructions list
    public static final List<String> userInstructionsList = new ArrayList<>();

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("chatgpt-to-mc-chat.properties");
    }

    public static void load() {
        Properties props = new Properties();
        Path path = configPath();

        // Defaults
        enabled = true;
        baritone = false;
        contextAwareness = true;
        allowlistEnabled = false;
        allowedPlayers.clear();

        cooldownSeconds = 5;
        maxPromptChars = 600;
        maxReplyChars = 240;

        apiKey = "";
        model = "gpt-4.1";

        userInstructionsList.clear();
        userInstructionsList.add("Plain text only.");
        userInstructionsList.add("No emojis / no unicode.");
        userInstructionsList.add("Keep replies short.");

        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (Exception ignored) {}
        }

        enabled = Boolean.parseBoolean(props.getProperty("enabled", String.valueOf(enabled)));
        baritone = Boolean.parseBoolean(props.getProperty("baritone", String.valueOf(baritone)));
        contextAwareness = Boolean.parseBoolean(props.getProperty("contextAwareness", String.valueOf(contextAwareness)));
        allowlistEnabled = Boolean.parseBoolean(props.getProperty("allowlistEnabled", String.valueOf(allowlistEnabled)));

        cooldownSeconds = parseInt(props.getProperty("cooldownSeconds"), cooldownSeconds);
        maxPromptChars  = parseInt(props.getProperty("maxPromptChars"), maxPromptChars);
        maxReplyChars   = parseInt(props.getProperty("maxReplyChars"), maxReplyChars);

        apiKey = props.getProperty("apiKey", apiKey);
        model = props.getProperty("model", model);

        String allowed = props.getProperty("allowedPlayers", "");
        allowedPlayers.clear();
        if (!allowed.isBlank()) {
            for (String part : allowed.split(",")) {
                String n = normalizeName(part);
                if (!n.isBlank() && !allowedPlayers.contains(n)) allowedPlayers.add(n);
            }
        }

        String inst = props.getProperty("userInstructionsList", "");
        if (!inst.isBlank()) {
            userInstructionsList.clear();
            for (String part : inst.split("\\|\\|")) {
                String s = part.trim();
                if (!s.isBlank()) userInstructionsList.add(s);
            }
            if (userInstructionsList.isEmpty()) {
                userInstructionsList.add("Plain text only.");
            }
        }

        save();
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("enabled", String.valueOf(enabled));
        props.setProperty("baritone", String.valueOf(baritone));
        props.setProperty("contextAwareness", String.valueOf(contextAwareness));

        props.setProperty("allowlistEnabled", String.valueOf(allowlistEnabled));
        props.setProperty("allowedPlayers", String.join(",", allowedPlayers));

        props.setProperty("cooldownSeconds", String.valueOf(cooldownSeconds));
        props.setProperty("maxPromptChars", String.valueOf(maxPromptChars));
        props.setProperty("maxReplyChars", String.valueOf(maxReplyChars));

        props.setProperty("apiKey", apiKey == null ? "" : apiKey);
        props.setProperty("model", model == null ? "" : model);

        props.setProperty("userInstructionsList", String.join("||", userInstructionsList));

        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                props.store(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                        "ChatGPT To MC Chat configuration");
            }
        } catch (Exception ignored) {}
    }

    public static boolean isPlayerAllowed(String name) {
        if (!allowlistEnabled) return true;
        if (name == null) return false;
        String n = normalizeName(name);
        if (n.isBlank()) return false;
        if (allowedPlayers.isEmpty()) return false;
        for (String a : allowedPlayers) {
            if (a.equalsIgnoreCase(n)) return true;
        }
        return false;
    }

    public static void addAllowedPlayer(String name) {
        String n = normalizeName(name);
        if (n.isBlank()) return;
        for (String a : allowedPlayers) {
            if (a.equalsIgnoreCase(n)) return;
        }
        allowedPlayers.add(n);
    }

    public static void removeAllowedPlayer(String name) {
        String n = normalizeName(name);
        allowedPlayers.removeIf(p -> p.equalsIgnoreCase(n));
    }

    public static void addInstruction(String s) {
        if (s == null) return;
        String t = s.trim();
        if (t.isBlank()) return;
        userInstructionsList.add(t);
    }

    public static void removeInstructionAt(int index) {
        if (index < 0 || index >= userInstructionsList.size()) return;
        userInstructionsList.remove(index);
        if (userInstructionsList.isEmpty()) userInstructionsList.add("Plain text only.");
    }

    public static String instructionsJoinedForApi() {
        StringBuilder sb = new StringBuilder();
        for (String s : userInstructionsList) {
            if (s == null) continue;
            String t = s.trim();
            if (t.isBlank()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(t);
        }
        return sb.toString().trim();
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }

    private static String normalizeName(String s) {
        if (s == null) return "";
        return s.trim();
    }
}