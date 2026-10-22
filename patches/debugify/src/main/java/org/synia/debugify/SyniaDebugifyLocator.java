package org.synia.debugify;

import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Early FML service: runs during mod discovery, before Connector transforms Fabric jars /
 * before Debugify's {@code MixinPlugin.onLoad} preloads {@code config/debugify.json}.
 *
 * <p>Registered via {@code META-INF/services/net.neoforged.neoforgespi.locating.IModFileCandidateLocator}.
 * Does not discover mods — it only applies the Debugify config gate and exits.
 *
 * <p><strong>Hard constraint:</strong> this jar must remain a pure early-service artifact
 * (no {@code neoforge.mods.toml}). If a future module also needs a normal FML mod entrypoint,
 * ship it via Jar-in-Jar — never merge both roles into one jar.
 *
 * <p>Kill switch: {@code -Dsynia.debugify.enabled=false}
 */
public final class SyniaDebugifyLocator implements IModFileCandidateLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger("Synia/debugify");

    @Override
    public int getPriority() {
        // Ahead of default locators so the config file exists as early as possible.
        return HIGHEST_SYSTEM_PRIORITY;
    }

    @Override
    public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
        Instant serviceTs = Instant.now();
        LOGGER.info(
                "[{}] Early service active (Connector 2.x / 1.21.1 path) service_ts={}",
                DebugifyPatchPlugin.NAME,
                serviceTs
        );

        if (!DebugifyPatchPlugin.isEnabled()) {
            LOGGER.warn(
                    "[{}] Gate DISABLED via -D{} or env {} — no config writes this launch",
                    DebugifyPatchPlugin.NAME,
                    DebugifyPatchPlugin.ENABLED_PROPERTY,
                    DebugifyPatchPlugin.ENABLED_ENV
            );
            return;
        }

        try {
            // FML 4.x (1.21.1): game dir comes from FMLPaths, not ILaunchContext.gameDirectory().
            DebugifyCompatConfig.ensureDisabledFixes(FMLPaths.GAMEDIR.get());
            LOGGER.info(
                    "[{}] Compare this write_ts_* with Debugify's \"Preloading Debugify\" line — "
                            + "Synia must appear FIRST for the gate to be effective",
                    DebugifyPatchPlugin.NAME
            );
        } catch (Exception e) {
            LOGGER.error(
                    "[{}] Failed to apply Debugify compatibility gate — "
                            + "manually set MC-93384 and MC-176559 to false in config/debugify.json",
                    DebugifyPatchPlugin.NAME,
                    e
            );
        }
    }
}
