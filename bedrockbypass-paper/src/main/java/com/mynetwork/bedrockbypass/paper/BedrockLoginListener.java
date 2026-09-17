package com.mynetwork.bedrockbypass.paper;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.geysermc.floodgate.api.FloodgateApi;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

/**
 * Saat player join, cek apakah dia player Bedrock (lewat Floodgate).
 * Kalau ya: register otomatis (kalau belum terdaftar) lalu force-login lewat AuthMe.
 * Player Java sama sekali tidak disentuh oleh listener ini.
 *
 * Dipakai skema retry (bukan sekali coba) karena AuthMe memuat data player
 * secara asynchronous - memanggil API-nya persis di awal PlayerJoinEvent
 * kadang terlalu cepat dan gagal diam-diam.
 */
public class BedrockLoginListener implements Listener {

    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final List<Integer> DEFAULT_DELAYS = List.of(10, 30, 60, 100);

    private final SecureRandom random = new SecureRandom();
    private final BedrockBypassPlugin plugin;
    private final FloodgateApi floodgateApi;

    public BedrockLoginListener(BedrockBypassPlugin plugin, FloodgateApi floodgateApi) {
        this.plugin = plugin;
        this.floodgateApi = floodgateApi;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Player Java biasa: dibiarkan, tetap pakai alur /register /login normal.
        if (!floodgateApi.isFloodgatePlayer(uuid)) {
            return;
        }

        if (!plugin.getConfig().getBoolean("enabled", true)) {
            return;
        }

        List<Integer> delays = plugin.getConfig().getIntegerList("retry-delays-ticks");
        if (delays == null || delays.isEmpty()) {
            delays = DEFAULT_DELAYS;
        }

        scheduleAttempt(player, delays, 0);
    }

    private void scheduleAttempt(Player player, List<Integer> delays, int attemptIndex) {
        if (attemptIndex >= delays.size()) {
            plugin.getLogger().warning("Gagal auto-login player Bedrock " + player.getName()
                    + " setelah " + delays.size() + " percobaan. Player harus /register atau /login manual.");
            return;
        }

        long delayTicks = delays.get(attemptIndex);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            boolean success = tryAutoLogin(player);
            if (!success) {
                scheduleAttempt(player, delays, attemptIndex + 1);
            }
        }, delayTicks);
    }

    private boolean tryAutoLogin(Player player) {
        AuthMeApi authMeApi = AuthMeApi.getInstance();
        if (authMeApi == null) {
            return false;
        }

        try {
            if (authMeApi.isAuthenticated(player)) {
                return true;
            }

            boolean registered = authMeApi.isRegistered(player.getName());

            if (!registered) {
                if (!plugin.getConfig().getBoolean("auto-register", true)) {
                    return false;
                }
                String password = generatePassword();
                // forceRegister otomatis melakukan login juga setelah registrasi.
                authMeApi.forceRegister(player, password);
            } else {
                authMeApi.forceLogin(player);
            }

            boolean nowAuthenticated = authMeApi.isAuthenticated(player);

            if (nowAuthenticated && plugin.getConfig().getBoolean("send-welcome-message", true)) {
                String msg = plugin.getConfig().getString("welcome-message", "");
                if (msg != null && !msg.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                }
            }

            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().info("[debug] " + player.getName()
                        + " authenticated=" + nowAuthenticated + " sudahTerdaftarSebelumnya=" + registered);
            }

            return nowAuthenticated;
        } catch (Throwable t) {
            if (plugin.getConfig().getBoolean("debug", false)) {
                plugin.getLogger().warning("Error saat auto-login " + player.getName() + ": " + t);
            }
            return false;
        }
    }

    private String generatePassword() {
        int length = plugin.getConfig().getInt("generated-password-length", 32);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
