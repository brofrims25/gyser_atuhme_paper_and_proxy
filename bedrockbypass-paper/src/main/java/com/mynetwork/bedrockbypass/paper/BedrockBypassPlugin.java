package com.mynetwork.bedrockbypass.paper;

import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

/**
 * Plugin Paper untuk server "login" - membuat player Bedrock (Geyser/Floodgate)
 * otomatis ter-register & login di AuthMe, tanpa mengubah perilaku untuk
 * player Java (mereka tetap wajib /register dan /login seperti biasa).
 */
public final class BedrockBypassPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (getServer().getPluginManager().getPlugin("Floodgate") == null) {
            getLogger().severe("Floodgate tidak ditemukan di server ini! " +
                    "Pastikan floodgate-spigot terpasang di server LOGIN, bukan cuma di proxy. " +
                    "Plugin dinonaktifkan.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("AuthMe") == null) {
            getLogger().severe("AuthMe tidak ditemukan di server ini! Plugin dinonaktifkan.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        FloodgateApi floodgateApi;
        try {
            floodgateApi = FloodgateApi.getInstance();
        } catch (Throwable t) {
            getLogger().severe("Gagal mengambil instance FloodgateApi: " + t.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getConfig().getBoolean("enabled", true)) {
            getLogger().info("BedrockAuthBypass dimuat tapi 'enabled: false' di config.yml - tidak melakukan apa-apa.");
            return;
        }

        getServer().getPluginManager().registerEvents(new BedrockLoginListener(this, floodgateApi), this);
        getLogger().info("BedrockAuthBypass aktif - player Bedrock akan auto-login lewat AuthMe.");
    }
}
