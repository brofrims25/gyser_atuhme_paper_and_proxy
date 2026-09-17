package com.mynetwork.bedrockbypass.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.geysermc.floodgate.api.FloodgateApi;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Plugin sisi proxy: kalau player Bedrock (Floodgate) mau connect ke server
 * "login" (tempat AuthMe berjalan), dia dialihkan LANGSUNG ke server lain
 * (mis. lobby) - jadi tidak pernah menyentuh AuthMe sama sekali.
 *
 * Ini pelengkap plugin Paper (bedrockbypass-paper). Kalau plugin ini aktif
 * dan target server-nya benar, plugin Paper di server login jarang/tidak
 * akan kebagian kerja untuk player Bedrock - tapi tetap berguna sebagai
 * fallback kalau suatu saat player Bedrock tetap transit lewat server login.
 */
@Plugin(
        id = "bedrockauthbypass",
        name = "BedrockAuthBypass",
        version = "1.0.0",
        description = "Alihkan player Bedrock (Floodgate) langsung lewat server login, skip AuthMe.",
        dependencies = {
                @Dependency(id = "floodgate")
        }
)
public class BedrockBypassVelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private boolean enabled = true;
    private String loginServerName = "login";
    private String bypassTargetServerName = "lobby";

    @Inject
    public BedrockBypassVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        loadOrCreateConfig();
        logger.info("BedrockAuthBypass (Velocity) aktif. enabled={} login-server='{}' bypass-target='{}'",
                enabled, loginServerName, bypassTargetServerName);
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        if (!enabled) {
            return;
        }

        RegisteredServer original = event.getOriginalServer();
        if (original == null || !original.getServerInfo().getName().equalsIgnoreCase(loginServerName)) {
            // Bukan lagi menuju server login, biarkan proses konek berjalan normal.
            return;
        }

        Player player = event.getPlayer();
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            // Player Java: tetap diarahkan ke server login seperti biasa.
            return;
        }

        Optional<RegisteredServer> target = server.getServer(bypassTargetServerName);
        if (target.isEmpty()) {
            logger.warn("Server tujuan bypass '{}' tidak ditemukan di velocity.toml - "
                    + "player Bedrock tetap diarahkan ke server login apa adanya.", bypassTargetServerName);
            return;
        }

        event.setResult(ServerPreConnectEvent.ServerResult.allowed(target.get()));
    }

    private void loadOrCreateConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configFile = dataDirectory.resolve("config.properties");
            Properties props = new Properties();

            if (!Files.exists(configFile)) {
                props.setProperty("enabled", "true");
                props.setProperty("login-server", loginServerName);
                props.setProperty("bypass-target-server", bypassTargetServerName);
                try (var out = Files.newOutputStream(configFile)) {
                    props.store(out,
                            "Konfigurasi BedrockAuthBypass (Velocity)\n"
                            + "login-server         = nama server (sesuai velocity.toml) tempat AuthMe berjalan\n"
                            + "bypass-target-server  = server tujuan pengalihan untuk player Bedrock (mis. lobby)");
                }
            } else {
                try (var in = Files.newInputStream(configFile)) {
                    props.load(in);
                }
            }

            this.enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            this.loginServerName = props.getProperty("login-server", loginServerName);
            this.bypassTargetServerName = props.getProperty("bypass-target-server", bypassTargetServerName);
        } catch (IOException e) {
            logger.error("Gagal membaca/membuat config.properties, pakai nilai default (login/lobby).", e);
        }
    }
}
