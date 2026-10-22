package org.synia.debugify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebugifyPatchPluginTest {

    @AfterEach
    void clearProperty() {
        System.clearProperty(DebugifyPatchPlugin.ENABLED_PROPERTY);
    }

    @Test
    void enabledByDefault() {
        System.clearProperty(DebugifyPatchPlugin.ENABLED_PROPERTY);
        assertTrue(DebugifyPatchPlugin.isEnabled());
    }

    @Test
    void disabledBySystemProperty() {
        System.setProperty(DebugifyPatchPlugin.ENABLED_PROPERTY, "false");
        assertFalse(DebugifyPatchPlugin.isEnabled());
    }

    @Test
    void enabledByExplicitTrue() {
        System.setProperty(DebugifyPatchPlugin.ENABLED_PROPERTY, "true");
        assertTrue(DebugifyPatchPlugin.isEnabled());
    }
}
