package org.synia.debugify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebugifyCompatConfigTest {

    @TempDir
    Path gameDir;

    @Test
    void createsConfigWhenMissing() throws Exception {
        assertTrue(DebugifyCompatConfig.ensureDisabledFixes(gameDir));
        String json = Files.readString(gameDir.resolve("config/debugify.json"), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"MC-93384\": false"));
        assertTrue(json.contains("\"MC-176559\": false"));
        assertTrue(json.contains(DebugifyPatchPlugin.NAME));
    }

    @Test
    void forcesTrueEntriesOff() {
        String input = """
                {
                  "opt_out_updater": false,
                  "MC-93384": true,
                  "MC-176559": true,
                  "MC-122627": true
                }
                """;
        Map<String, Boolean> required = new LinkedHashMap<>();
        required.put("MC-93384", false);
        required.put("MC-176559", false);

        String out = DebugifyCompatConfig.mergeDisabledFixes(input, required);
        assertTrue(out.contains("\"MC-93384\": false"));
        assertTrue(out.contains("\"MC-176559\": false"));
        assertTrue(out.contains("\"MC-122627\": true"));
    }

    @Test
    void insertsMissingKeys() {
        String input = """
                {
                  "opt_out_updater": true
                }
                """;
        Map<String, Boolean> required = new LinkedHashMap<>();
        required.put("MC-93384", false);
        required.put("MC-176559", false);

        String out = DebugifyCompatConfig.mergeDisabledFixes(input, required);
        assertTrue(out.contains("\"MC-93384\": false"));
        assertTrue(out.contains("\"MC-176559\": false"));
        assertTrue(out.contains("\"opt_out_updater\": true"));
    }

    @Test
    void idempotentWhenAlreadySafe() throws Exception {
        Path config = gameDir.resolve("config/debugify.json");
        Files.createDirectories(config.getParent());
        Files.writeString(
                config,
                """
                        {
                          "MC-93384": false,
                          "MC-176559": false
                        }
                        """,
                StandardCharsets.UTF_8
        );
        assertFalse(DebugifyCompatConfig.ensureDisabledFixes(gameDir));
    }
}
