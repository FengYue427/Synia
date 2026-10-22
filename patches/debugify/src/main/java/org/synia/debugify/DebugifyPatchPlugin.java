package org.synia.debugify;

/**
 * Synia patch identity for the Debugify compatibility gate.
 *
 * <p>Connector 3.x exposes {@code org.sinytra.connector.transformer.api.TransformerPlugin}
 * for MethodPatch / jar transformers. That SPI is <strong>not</strong> present on the
 * 1.21.1 LTS line (Connector 2.x). This class is the 1.21.1 stand-in: same plugin id
 * ({@code synia:debugify}), same conditional targeting philosophy, delivered via an early
 * FML {@link SyniaDebugifyLocator} that seeds Debugify's config before mixins apply.
 *
 * <p><strong>Kill switch:</strong> {@code -Dsynia.debugify.enabled=false}
 * (or env {@code SYNIA_DEBUGIFY_ENABLED=false}).
 */
public final class DebugifyPatchPlugin {
    public static final String NAME = "synia:debugify";

    /** System property / env used to disable this gate without removing the jar. */
    public static final String ENABLED_PROPERTY = "synia.debugify.enabled";
    public static final String ENABLED_ENV = "SYNIA_DEBUGIFY_ENABLED";

    /** Bug fixes that conflict with NeoForge / Connector on 1.21.1. */
    public static final String[] DISABLED_FIXES = {
            "MC-93384",
            "MC-176559",
    };

    private DebugifyPatchPlugin() {
    }

    public String name() {
        return NAME;
    }

    /**
     * @return {@code false} when the user explicitly disabled this patch
     */
    public static boolean isEnabled() {
        String prop = System.getProperty(ENABLED_PROPERTY);
        if (prop != null && !prop.isBlank()) {
            return parseTruthy(prop);
        }
        String env = System.getenv(ENABLED_ENV);
        if (env != null && !env.isBlank()) {
            return parseTruthy(env);
        }
        return true;
    }

    private static boolean parseTruthy(String raw) {
        String v = raw.trim().toLowerCase();
        return !(v.equals("false") || v.equals("0") || v.equals("off") || v.equals("no"));
    }
}
