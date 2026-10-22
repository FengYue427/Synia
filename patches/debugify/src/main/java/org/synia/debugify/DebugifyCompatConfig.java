package org.synia.debugify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Forces known Connector-hostile Debugify special fixes off in {@code config/debugify.json}.
 *
 * <p>Debugify's mixin plugin reads this file during {@code MixinPlugin.onLoad} →
 * {@code Debugify.onPreInitialize()} → {@code CONFIG.preload()}. Disabling the entries
 * here prevents the conflicting mixins from applying (community workaround for
 * Sinytra/Connector#1322 / #1770).
 *
 * <p>Timing assumption (Connector 2.x / FML 4): {@link SyniaDebugifyLocator#findCandidates}
 * runs during mod discovery, which is before Fabric mixin configs are applied. Logs emit
 * an ISO-8601 write timestamp plus a post-write disk audit so in-game logs can be compared
 * against Debugify's own "Preloading Debugify" line.
 */
public final class DebugifyCompatConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("Synia/debugify");
    private static final Pattern ENTRY = Pattern.compile(
            "\"(MC-\\d+)\"\\s*:\\s*(true|false)",
            Pattern.CASE_INSENSITIVE
    );

    private DebugifyCompatConfig() {
    }

    /**
     * Ensures the gated bug-fix keys exist and are {@code false}.
     *
     * @return {@code true} if the file was created or modified
     */
    public static boolean ensureDisabledFixes(Path gameDirectory) throws IOException {
        Path configDir = gameDirectory.resolve("config");
        Files.createDirectories(configDir);
        Path configPath = configDir.resolve("debugify.json");

        Map<String, Boolean> required = new LinkedHashMap<>();
        for (String id : DebugifyPatchPlugin.DISABLED_FIXES) {
            required.put(id, false);
        }

        Instant writeStarted = Instant.now();
        boolean changed;
        if (!Files.exists(configPath)) {
            String body = writeMinimalConfig(required);
            Files.writeString(configPath, body, StandardCharsets.UTF_8);
            changed = true;
            LOGGER.info(
                    "[{}] Created {} with Connector-safe defaults ({})",
                    DebugifyPatchPlugin.NAME,
                    configPath.toAbsolutePath(),
                    String.join(", ", required.keySet())
            );
        } else {
            String original = Files.readString(configPath, StandardCharsets.UTF_8);
            MergeResult merged = mergeDisabledFixesDetailed(original, required);
            if (!merged.changed()) {
                changed = false;
                LOGGER.info(
                        "[{}] {} already disables {}",
                        DebugifyPatchPlugin.NAME,
                        configPath.getFileName(),
                        String.join(", ", required.keySet())
                );
            } else {
                Files.writeString(configPath, merged.json(), StandardCharsets.UTF_8);
                changed = true;
                LOGGER.info(
                        "[{}] Updated {} — forced off {}",
                        DebugifyPatchPlugin.NAME,
                        configPath.toAbsolutePath(),
                        String.join(", ", required.keySet())
                );
            }
        }

        Instant writeFinished = Instant.now();
        Map<String, Boolean> onDisk = readFixFlags(configPath, required.keySet());
        boolean auditOk = required.keySet().stream()
                .allMatch(id -> Boolean.FALSE.equals(onDisk.get(id)));

        LOGGER.info(
                "[{}] write_ts_start={} write_ts_end={} changed={} disk_audit={} values={}",
                DebugifyPatchPlugin.NAME,
                writeStarted,
                writeFinished,
                changed,
                auditOk ? "PASS" : "FAIL",
                onDisk
        );

        if (!auditOk) {
            LOGGER.error(
                    "[{}] Disk audit FAILED — expected all gated fixes false, got {}. "
                            + "Gate may be ineffective if Debugify already preloaded.",
                    DebugifyPatchPlugin.NAME,
                    onDisk
            );
        }

        return changed;
    }

    /**
     * Reads current boolean values for the given fix ids from an existing config file.
     */
    static Map<String, Boolean> readFixFlags(Path configPath, Iterable<String> ids) throws IOException {
        Map<String, Boolean> wanted = new LinkedHashMap<>();
        for (String id : ids) {
            wanted.put(id, null);
        }
        if (!Files.exists(configPath)) {
            return wanted;
        }
        String json = Files.readString(configPath, StandardCharsets.UTF_8);
        Matcher matcher = ENTRY.matcher(json);
        while (matcher.find()) {
            String id = matcher.group(1).toUpperCase(Locale.ROOT);
            if (wanted.containsKey(id)) {
                wanted.put(id, Boolean.parseBoolean(matcher.group(2)));
            }
        }
        return wanted;
    }

    static String mergeDisabledFixes(String json, Map<String, Boolean> required) {
        return mergeDisabledFixesDetailed(json, required).json();
    }

    static MergeResult mergeDisabledFixesDetailed(String json, Map<String, Boolean> required) {
        String trimmed = json == null ? "" : json.trim();
        if (trimmed.isEmpty()) {
            return new MergeResult(writeMinimalConfig(required), true);
        }

        Map<String, Boolean> present = new LinkedHashMap<>();
        boolean changed = false;
        Matcher matcher = ENTRY.matcher(trimmed);
        StringBuffer rewritten = new StringBuffer();
        while (matcher.find()) {
            String id = matcher.group(1).toUpperCase(Locale.ROOT);
            if (required.containsKey(id)) {
                present.put(id, false);
                boolean wasEnabled = Boolean.parseBoolean(matcher.group(2));
                if (wasEnabled) {
                    changed = true;
                    matcher.appendReplacement(
                            rewritten,
                            Matcher.quoteReplacement("\"" + id + "\": false")
                    );
                } else {
                    matcher.appendReplacement(rewritten, Matcher.quoteReplacement(matcher.group()));
                }
            } else {
                matcher.appendReplacement(rewritten, Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(rewritten);

        StringBuilder result = new StringBuilder(rewritten);
        for (Map.Entry<String, Boolean> entry : required.entrySet()) {
            if (present.containsKey(entry.getKey())) {
                continue;
            }
            changed = true;
            insertBooleanEntry(result, entry.getKey(), entry.getValue());
        }
        return new MergeResult(changed ? result.toString() : trimmed, changed);
    }

    record MergeResult(String json, boolean changed) {
    }

    private static String writeMinimalConfig(Map<String, Boolean> required) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"_synia\": \"Managed by ").append(DebugifyPatchPlugin.NAME).append("\",\n");
        int i = 0;
        for (Map.Entry<String, Boolean> entry : required.entrySet()) {
            sb.append("  \"").append(entry.getKey()).append("\": ")
                    .append(entry.getValue());
            if (++i < required.size()) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static void insertBooleanEntry(StringBuilder json, String key, boolean value) {
        int close = json.lastIndexOf("}");
        if (close < 0) {
            json.setLength(0);
            json.append(writeMinimalConfig(Map.of(key, value)));
            return;
        }

        int lastNonWs = close - 1;
        while (lastNonWs >= 0 && Character.isWhitespace(json.charAt(lastNonWs))) {
            lastNonWs--;
        }
        boolean needsComma = lastNonWs >= 0 && json.charAt(lastNonWs) != '{' && json.charAt(lastNonWs) != ',';

        String insertion = (needsComma ? "," : "")
                + "\n  \"" + key + "\": " + value + "\n";
        json.insert(close, insertion);
    }
}
