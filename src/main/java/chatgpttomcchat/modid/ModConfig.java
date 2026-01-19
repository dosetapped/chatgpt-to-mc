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
    public static boolean enabled = true;
    public static boolean baritone = false;
    public static boolean allowlistEnabled = false;
    public static final List<String> allowedPlayers = new ArrayList<>();
    public static String apiKey = ""; 
    public static String model = "gpt-4.1";
    public static int cooldownSeconds = 5;
    public static int maxPromptChars = 600;
    public static int maxReplyChars = 240;
    public static final List<String> userInstructionsList = new ArrayList<>();

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("chatgpt-to-mc-chat.properties");
    }

    public static void load() {
        Properties props = new Properties();
        Path path = configPath();
        
        apiKey = ""; // Safety: Keep default empty
        model = "gpt-4.1";

        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (Exception ignored) {}
        }

        enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
        baritone = Boolean.parseBoolean(props.getProperty("baritone", "false"));
        allowlistEnabled = Boolean.parseBoolean(props.getProperty("allowlistEnabled", "false"));
        cooldownSeconds = parseInt(props.getProperty("cooldownSeconds"), 5);
        maxPromptChars = parseInt(props.getProperty("maxPromptChars"), 600);
        maxReplyChars = parseInt(props.getProperty("maxReplyChars"), 240);
        apiKey = props.getProperty("apiKey", "");
        model = props.getProperty("model", "gpt-4.1");

        String allowed = props.getProperty("allowedPlayers", "");
        allowedPlayers.clear();
        if (!allowed.isBlank()) {
            for (String part : allowed.split(",")) {
                addAllowedPlayer(part);
            }
        }

        String inst = props.getProperty("userInstructionsList", "");
        if (!inst.isBlank()) {
            userInstructionsList.clear();
            for (String part : inst.split("\\|\\|")) {
                addInstruction(part);
            }
        }
        save();
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("enabled", String.valueOf(enabled));
        props.setProperty("baritone", String.valueOf(baritone));
        props.setProperty("allowlistEnabled", String.valueOf(allowlistEnabled));
        props.setProperty("allowedPlayers", String.join(",", allowedPlayers));
        props.setProperty("cooldownSeconds", String.valueOf(cooldownSeconds));
        props.setProperty("maxPromptChars", String.valueOf(maxPromptChars));
        props.setProperty("maxReplyChars", String.valueOf(maxReplyChars));
        props.setProperty("apiKey", apiKey);
        props.setProperty("model", model);
        props.setProperty("userInstructionsList", String.join("||", userInstructionsList));

        try {
            Files.createDirectories(configPath().getParent());
            try (OutputStream out = Files.newOutputStream(configPath())) {
                props.store(new OutputStreamWriter(out, StandardCharsets.UTF_8), "Config");
            }
        } catch (Exception ignored) {}
    }

    public static boolean isPlayerAllowed(String name) {
        if (!allowlistEnabled) return true;
        String n = normalizeName(name);
        if (n.isBlank() || allowedPlayers.isEmpty()) return false;
        return allowedPlayers.stream().anyMatch(p -> p.equalsIgnoreCase(n));
    }

    public static void addAllowedPlayer(String name) {
        String n = normalizeName(name);
        if (!n.isBlank() && !allowedPlayers.contains(n)) allowedPlayers.add(n);
    }

    public static void removeAllowedPlayer(String name) {
        allowedPlayers.removeIf(p -> p.equalsIgnoreCase(normalizeName(name)));
    }

    public static void addInstruction(String s) {
        if (s != null && !s.trim().isBlank()) userInstructionsList.add(s.trim());
    }

    public static void removeInstructionAt(int index) {
        if (index >= 0 && index < userInstructionsList.size()) userInstructionsList.remove(index);
    }

    public static String instructionsJoinedForApi() {
        return String.join(" ", userInstructionsList);
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static String normalizeName(String s) {
        return s == null ? "" : s.trim();
    }
}