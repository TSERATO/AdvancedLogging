package dev.tserato.advancedlogging;

import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedLogging extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("AdvancedLogging Enabled");
        getServer().getPluginManager().registerEvents(new AdvancedLoggingListener(), this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("AdvancedLogging Disabled");
    }
}
