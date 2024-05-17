package dev.tserato.advancedlogging;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.bukkit.scheduler.BukkitRunnable;

import static org.codehaus.plexus.util.FileUtils.deleteDirectory;

public class AdvancedLogging extends JavaPlugin implements Listener {

    private int months;
    private int days;
    private int hours;
    private int minutes;
    private int seconds;
    private @NotNull BukkitTask clearLogsTask;
    private Map<String, WebhookEvent> webhookEvents;

    @Override
    public void onEnable() {
        getLogger().info("AdvancedLogging Enabled");
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        loadConfigValues();
        checkForUpdates();
        createDirectoriesIfNeeded();
        loadWebhookEvents();
        int pluginId = 21901;
        Metrics metrics = new Metrics(this, pluginId);

        if (shouldDeleteLogs()) {
            getLogger().warning("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            getLogger().warning("Deleting logs every:");
            getLogger().warning(months + " Months");
            getLogger().warning(days + " Days");
            getLogger().warning(hours + " Hours");
            getLogger().warning(minutes + " Minutes");
            getLogger().warning(seconds + " Seconds");
            getLogger().warning("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            startClearLogsTask();
        } else {
            getLogger().info("Log deletion is disabled in the configuration.");
        }
    }

    private void loadWebhookEvents() {
        webhookEvents = new HashMap<>();
        ConfigurationSection webhookSection = getConfig().getConfigurationSection("webhook-events");
        if (webhookSection != null) {
            for (String key : webhookSection.getKeys(false)) {
                ConfigurationSection eventSection = webhookSection.getConfigurationSection(key);
                if (eventSection != null && eventSection.getBoolean("enabled", false)) {
                    String eventName = eventSection.getString("event");
                    String webhookUrl = eventSection.getString("webhook-url");
                    if (eventName != null && webhookUrl != null) {
                        webhookEvents.put(eventName, new WebhookEvent(eventName, webhookUrl));
                    }
                }
            }
        }
    }

    private static class WebhookEvent {
        private final String eventName;
        private final String webhookUrl;
        private final boolean enabled;

        public WebhookEvent(String eventName, String webhookUrl) {
            this.eventName = eventName;
            this.webhookUrl = webhookUrl;
            this.enabled = true; // Assuming events are enabled by default unless specified otherwise
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void sendWebhook(String message, String event) {
            try {
                DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
                webhook.addEmbed(new DiscordWebhook.EmbedObject().setTitle(event).setDescription(message).setColor(Color.GREEN));
                webhook.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startClearLogsTask() {
        long period = seconds * 20 + minutes * 20 * 60 + hours * 20 * 60 * 60 + days * 20 * 60 * 60 * 24 + months * 20 * 60 * 60 * 24 * 30;
        if (clearLogsTask != null) {
            clearLogsTask.cancel();
        }
        clearLogsTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    autoClearLogs();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.runTaskTimer(this, period, period);
    }

    private void loadConfigValues() {
        FileConfiguration config = getConfig();
        months = config.getInt("delete-logs.months", 0);
        days = config.getInt("delete-logs.days", 0);
        hours = config.getInt("delete-logs.hours", 0);
        minutes = config.getInt("delete-logs.minutes", 0);
        seconds = config.getInt("delete-logs.seconds", 0);
    }

    private boolean shouldDeleteLogs() {
        return months > 0 || days > 0 || hours > 0 || minutes > 0 || seconds > 0;
    }

    @Override
    public void onDisable() {
        getLogger().info("AdvancedLogging Disabled");
        if (clearLogsTask != null) {
            clearLogsTask.cancel();
        }
    }

    private final Set<String> confirmingClear = new HashSet<>();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("advancedlogging") || label.equalsIgnoreCase("al") || label.equalsIgnoreCase("advlog")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("advlog.use")) {
                        reloadConfig();
                        loadConfigValues();
                        loadWebhookEvents(); // Reload webhook events
                        sender.sendMessage("Config reloaded.");
                        if (shouldDeleteLogs()) {
                            startClearLogsTask();
                        } else {
                            if (clearLogsTask != null) {
                                clearLogsTask.cancel();
                            }
                        }
                        return true;
                    } else {
                        if (sender instanceof Player) {
                            sender.sendMessage(ChatColor.RED + "You don't have permission!");
                        } else {
                            sender.sendMessage("You don't have permission!");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("clear")) {
                    if (sender.hasPermission("advlog.use")) {
                        if (!confirmingClear.contains(sender.getName())) {
                            confirmingClear.add(sender.getName());
                            sender.sendMessage(ChatColor.RED + "Are you sure you want to clear all log files? If yes, run the command again.");
                            return true;
                        } else {
                            confirmingClear.remove(sender.getName());
                            try {
                                clearLogs(sender);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        }
                    } else {
                        if (sender instanceof Player) {
                            sender.sendMessage(ChatColor.RED + "You don't have permission!");
                        } else {
                            sender.sendMessage("You don't have permission!");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")) {
                    // New version commands
                    sender.sendMessage(ChatColor.GREEN + "Current Version: " + getDescription().getVersion());
                    checkAndSendLatestVersion(sender);
                    return true;
                }
            }
        }
        return false;
    }

    private void checkAndSendLatestVersion(CommandSender sender) {
        try {
            int resourceId = 116766;
            String updateCheckUrl = "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId;
            URL url = new URL(updateCheckUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String latestVersion = in.readLine();
                in.close();
                sender.sendMessage(ChatColor.GREEN + "Latest Version: " + latestVersion);
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to check for latest version.");
            }
            connection.disconnect();
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Failed to check for latest version: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkForUpdates(player);
    }

    private void checkForUpdates() {
        try {
            int resourceId = 116766;
            String updateCheckUrl = "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId;
            URL url = new URL(updateCheckUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String version = in.readLine();
                in.close();
                String currentVersion = getDescription().getVersion();
                if (compareVersions(currentVersion, version) < 0) {
                    getLogger().warning("A new version of AdvLog (v" + version + ") is available! You are currently running v" + currentVersion + ". Update at: https://www.spigotmc.org/resources/" + resourceId);
                } else {
                    getLogger().info("You are running the latest version of AdvLog.");
                }
            }
            connection.disconnect();
        } catch (IOException e) {
            getLogger().warning("Failed to check for updates: " + e.getMessage());
        }
    }

    private void checkForUpdates(Player player) {
        try {
            int resourceId = 116766;
            String updateCheckUrl = "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId;
            URL url = new URL(updateCheckUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String version = in.readLine();
                in.close();
                String currentVersion = getDescription().getVersion();
                if (compareVersions(currentVersion, version) < 0) {
                    if (player.hasPermission("advlog.use")) {
                        player.sendMessage("A new version of AdvLog (v" + version + ") is available! You are currently running v" + currentVersion + ". Update at: https://www.spigotmc.org/resources/" + resourceId);
                    }
                }
            }
            connection.disconnect();
        } catch (IOException e) {
            getLogger().warning("Failed to check for updates: " + e.getMessage());
        }
    }

    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (part1 < part2) return -1;
            if (part1 > part2) return 1;
        }
        return 0;
    }

    private void createDirectoriesIfNeeded() {
        File logsDirectory = new File(getDataFolder(), "Logs");
        if (!logsDirectory.exists()) {
            logsDirectory.mkdirs();
        }

        String[] eventDirectories = {"Block Events", "Enchantment Events", "Entity Events", "Inventory Events", "Player Events", "Vehicle Events", "Weather Events"};
        for (String eventDir : eventDirectories) {
            File eventDirectory = new File(logsDirectory, eventDir);
            if (!eventDirectory.exists()) {
                eventDirectory.mkdirs();
            }
        }
    }

    private void logToFile(String directory, String fileName, String message) {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String logMessage = String.format("[%s] %s", timestamp, message);
            File logFile = new File(getDataFolder() + "/Logs/" + directory + "/" + fileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileWriter writer = new FileWriter(logFile, true);
            writer.write(logMessage);
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            getLogger().severe("An error occurred while logging: " + e.getMessage());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (cmd.getName().equalsIgnoreCase("advlog") && args.length == 1 || cmd.getName().equalsIgnoreCase("advancedlogging") && args.length == 1 || cmd.getName().equalsIgnoreCase("al") && args.length == 1) {
            completions.add("reload");
            completions.add("clear");
            completions.add("version");
            completions.add("v");
        }
        return completions;
    }

    private void clearLogs(CommandSender sender) throws IOException {
        File logsDirectory = new File(getDataFolder(), "Logs");
        if (logsDirectory.exists() && logsDirectory.isDirectory()) {
            File[] eventDirectories = logsDirectory.listFiles();
            if (eventDirectories != null) {
                for (File eventDir : eventDirectories) {
                    deleteDirectory(eventDir);
                }
            }
            createDirectoriesIfNeeded();
            sender.sendMessage(ChatColor.GREEN + "All log files deleted successfully.");
        } else {
            sender.sendMessage(ChatColor.RED + "No log files found.");
        }
    }

    private void autoClearLogs() throws IOException {
        File logsDirectory = new File(getDataFolder(), "Logs");
        if (logsDirectory.exists() && logsDirectory.isDirectory()) {
            File[] eventDirectories = logsDirectory.listFiles();
            if (eventDirectories != null) {
                for (File eventDir : eventDirectories) {
                    deleteDirectory(eventDir);
                }
            }
            createDirectoriesIfNeeded();
            getLogger().info("All log files deleted successfully.");
        } else {
            getLogger().severe("No log files found.");
        }
    }









    //BLOCKS
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-break")) {
            String playerName = event.getPlayer().getName();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKBREAK: Broken by: %s; Type: %s; Location: %s; World: %s.", playerName, blockType, location, world);
            logToFile("Block Events", "block_break.log", logMessage);

            String eventName = "BLOCK_BREAK";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Break event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-break-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-break-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-break-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-burn")) {
            String Name = Objects.requireNonNull(event.getIgnitingBlock()).getType().name();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKBURN: Burned by: %s; Type: %s; Location: %s; World: %s.", Name, blockType, location, world);
            logToFile("Block Events", "block_burn.log", logMessage);

            String eventName = "BLOCK_BURN";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Burn event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-burn-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-burn-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-burn-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockCanBuild(BlockCanBuildEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-can-build")) {
            String Name = Objects.requireNonNull(event.getPlayer()).getName();
            String blockType = event.getBlock().getType().toString();
            String buildable = String.valueOf(event.isBuildable());
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKCANBUILD: Trying to get build by: %s; Type: %s; Buildable? %s; Location: %s; World: %s.", Name, blockType, buildable, location, world);
            logToFile("Block Events", "block_can_build.log", logMessage);

            String eventName = "BLOCK_CAN_BUILD";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Can Build event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-can-build-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-can-build-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-can-build-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-damage")) {
            String Name = event.getPlayer().getName();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKDAMAGE: Damaged by: %s; Type: %s; Location: %s; World: %s.", Name, blockType, location, world);
            logToFile("Block Events", "block_damage.log", logMessage);

            String eventName = "BLOCK_DAMAGE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Damage event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-damage-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-damage-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-damage-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-dispense")) {
            String Velocity = String.valueOf(event.getVelocity());
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKDISPENSE: Velocity: %s; Type: %s; Location: %s; World: %s.", Velocity, blockType, location, world);
            logToFile("Block Events", "block_dispense.log", logMessage);

            String eventName = "BLOCK_DISPENSE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Dispense event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-dispense-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-dispense-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-dispense-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-fade")) {
            String state = event.getNewState().toString();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKFADE: New State: %s; Type: %s; Location: %s; World: %s.", state, blockType, location, world);
            logToFile("Block Events", "block_fade.log", logMessage);

            String eventName = "BLOCK_FADE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Fade event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-fade-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-fade-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-fade-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-form")) {
            String state = event.getNewState().toString();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKFORM: New State: %s; Type: %s; Location: %s; World: %s.", state, blockType, location, world);
            logToFile("Block Events", "block_form.log", logMessage);

            String eventName = "BLOCK_FORM";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Form event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-form-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-form-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-form-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-from-to")) {
            String blockto = event.getToBlock().toString();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKFROMTO: To Block: %s; Type: %s; Location: %s; World: %s.", blockto, blockType, location, world);
            logToFile("Block Events", "block_from_to.log", logMessage);

            String eventName = "BLOCK_FROM_TO";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block From To event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-from-to-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-from-to-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-from-to-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-grow")) {
            String state = event.getNewState().toString();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKGROW: New State: %s; Type: %s; Location: %s; World: %s.", state, blockType, location, world);
            logToFile("Block Events", "block_grow.log", logMessage);

            String eventName = "BLOCK_GROW";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Grow event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-grow-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-grow-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-grow-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-ignite")) {
            String playerName = Objects.requireNonNull(event.getPlayer()).getName();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKIGNITE: Ignited by: %s; Type: %s; Location: %s; World: %s.", playerName, blockType, location, world);
            logToFile("Block Events", "block_ignite.log", logMessage);

            String eventName = "BLOCK_IGNITE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Ignite event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-ignite-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-ignite-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-ignite-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-piston-extend")) {
            String sticky = String.valueOf(event.isSticky());
            String facing = event.getDirection().toString();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKPISTONEXTEND: Sticky? %s; Type: %s; Facing: %s Location: %s; World: %s.", sticky, blockType, facing, location, world);
            logToFile("Block Events", "block_piston-extend.log", logMessage);

            String eventName = "BLOCK_PISTON_EXTEND";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Piston Extend event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-piston-extend-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-piston-extend-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-piston-extend-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-piston-retract")) {
            String sticky = String.valueOf(event.isSticky());
            String facing = event.getDirection().toString();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKPISTONRETRACT: Sticky? %s; Type: %s; Facing: %s Location: %s; World: %s.", sticky, blockType, facing, location, world);
            logToFile("Block Events", "block_piston_retract.log", logMessage);

            String eventName = "BLOCK_PISTON_RETRACT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Piston Retract event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-piston-retract-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-piston-retract-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-piston-retract-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-place")) {
            String playerName = event.getPlayer().getName();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKPLACE: Placed by: %s; Type: %s; Location: %s; World: %s.", playerName, blockType, location, world);
            logToFile("Block Events", "block_place.log", logMessage);

            String eventName = "BLOCK_PLACE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Place event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-place-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-place-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-place-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-redstone")) {
            String oldCurrent = String.valueOf(event.getOldCurrent());
            String newCurrent = String.valueOf(event.getNewCurrent());
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKREDSTONE: Old current: %s; New current: %s; Type: %s; Location: %s; World: %s.", oldCurrent, newCurrent, blockType, location, world);
            logToFile("Block Events", "block_redstone.log", logMessage);

            String eventName = "BLOCK_REDSTONE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Redstone event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-redstone-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-redstone-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-redstone-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-spread")) {
            String state = event.getNewState().toString();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKSPREAD: New state: %s; Type: %s; Location: %s; World: %s.", state, blockType, location, world);
            logToFile("Block Events", "block_spread.log", logMessage);

            String eventName = "BLOCK_SPREAD";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Spread event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-spread-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-spread-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-spread-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockLeavesDecay(LeavesDecayEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-leaves-decay")) {
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKLEAVESDECAY: Type: %s; Location: %s; World: %s.", blockType, location, world);
            logToFile("Block Events", "block_leaves_decay.log", logMessage);

            String eventName = "BLOCK_LEAVES_DECAY";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Leaves Decay event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-leaves-decay-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-leaves-decay-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-leaves-decay-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockNotePlay(NotePlayEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-note-play")) {
            String instrument = event.getInstrument().toString();
            String note = event.getNote().toString();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKNOTEPLAY: Instrument: %s; Note: %s; Type: %s; Location: %s; World: %s.", instrument, note, blockType, location, world);
            logToFile("Block Events", "block_note_play.log", logMessage);

            String eventName = "BLOCK_NOTE_PLAY";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Note Play event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-note-play-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-note-play-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-note-play-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBlockSignChange(SignChangeEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("block-sign-change")) {
            String playerName = event.getPlayer().getName();
            String blockType = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getWorld().getName();
            String logMessage = String.format("BLOCKSIGNCHANGE: Changed by: %s; Type: %s; Location: %s; World: %s.", playerName, blockType, location, world);
            logToFile("Block Events", "block_sign_change.log", logMessage);

            String eventName = "BLOCK_SIGN_CHANGE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Block Sign Change event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("block-sign-change-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("block-sign-change-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("block-sign-change-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-block-console")) {
                getLogger().info(logMessage);
            }
        }
    }




    //Enchantments
    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("enchant-enchantment")) {
            String playerName = event.getEnchanter().getName();
            String blockType = event.getItem().getType().toString();
            String location = event.getEnchanter().getLocation().toString();
            String world = event.getEnchanter().getWorld().getName();
            String logMessage = String.format("ENCHANTMENT: Enchanted by: %s; Type: %s; Location: %s; World: %s.", playerName, blockType, location, world);
            logToFile("Enchantment Events", "enchant_enchantment.log", logMessage);

            String eventName = "ENCHANT_ENCHANTMENT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Enchant Enchantment event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("enchant-enchantment-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("enchant-enchantment-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("enchant-enchantment-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-enchantment-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onPrepareEnchantItem(PrepareItemEnchantEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("enchant-prepare-enchantment")) {
            String playerName = event.getEnchanter().getName();
            String blockType = event.getItem().getType().toString();
            String location = event.getEnchanter().getLocation().toString();
            String world = event.getEnchanter().getWorld().getName();
            String logMessage = String.format("PREPAREENCHANTMENT: Prepared by: %s; Type: %s; Location: %s; World: %s.", playerName, blockType, location, world);
            logToFile("Enchantment Events", "enchant_prepare_enchantment.log", logMessage);

            String eventName = "ENCHANT_PREPARE_ENCHANTMENT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Enchant Prepare Enchantment event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("enchant-prepare-enchantment-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("enchant-prepare-enchantment-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("enchant-prepare-enchantment-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-enchantment-console")) {
                getLogger().info(logMessage);
            }
        }
    }




    //Entity
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-creature-spawn")) {
            String playerName = event.getEntity().getName();
            String reason = event.getSpawnReason().toString();
            String location = event.getLocation().toString();
            String world = event.getLocation().getWorld().getName();
            String logMessage = String.format("SPAWN: Spawned: %s; Reason: %s; Location: %s; World: %s.", playerName, reason, location, world);
            logToFile("Entity Events", "entity_creature_spawn.log", logMessage);

            String eventName = "ENTITY_CREATURE_SPAWN";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Creature Spawn event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-spawn-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-spawn-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-spawn-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onCreeperPower(CreeperPowerEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("creeper-power")) {
            String reason = event.getCause().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("CREEPERPOWER: Cause: %s; Location: %s; World: %s.", reason, location, world);
            logToFile("Entity Events", "entity_creeper_power.log", logMessage);

            String eventName = "ENTITY_CREEPER_POWER";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Creeper Power event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("creeper-power-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("creeper-power-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("creeper-power-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-break-door")) {
            String playerName = event.getEntity().getName();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("BREAKDOOR: Entity: %s; Location: %s; World: %s.", playerName, location, world);
            logToFile("Entity Events", "entity_break_door.log", logMessage);

            String eventName = "ENTITY_BREAK_DOOR";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Break Door event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-break-door-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-break-door-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-break-door-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-change-block")) {
            String name = event.getEntity().getName();
            String type = event.getBlock().getType().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getLocation().getWorld().getName();
            String logMessage = String.format("CHANGEBLOCK: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_change_block.log", logMessage);

            String eventName = "ENTITY_CHANGE_BLOCK";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Change Block event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-change-block-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-change-block-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-change-block-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-combust")) {
            String name = event.getEntity().getName();
            String type = event.getEntity().getType().toString();
            String duration = String.valueOf(event.getDuration());
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("COMBUST: Name: %s; Type: %s; Duration: %s; Location: %s; World: %s.", name, type, duration, location, world);
            logToFile("Entity Events", "entity_combust.log", logMessage);

            String eventName = "ENTITY_COMBUST";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Combust event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-combust-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-combust-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-combust-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityCreatePortal(EntityPortalReadyEvent event) {
        FileConfiguration config = getConfig();
        if (getServer().getVersion().contains("Paper")) {
            if (config.getBoolean("entity-create-portal")) {
                String name = event.getEntity().getName();
                String type = event.getPortalType().toString();
                String location = event.getEntity().getLocation().toString();
                String world = event.getEntity().getLocation().getWorld().getName();
                String logMessage = String.format("CREATEPORTAL: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
                logToFile("Entity Events", "entity_create_portal.log", logMessage);

                String eventName = "ENTITY_CREATE_PORTAL";
                if (webhookEvents.containsKey(eventName)) {
                    WebhookEvent webhookEvent = webhookEvents.get(eventName);
                    if (webhookEvent.isEnabled()) {
                        webhookEvent.sendWebhook("Entity Create Portal event occurred!", String.valueOf(event));
                    }
                }

                if (config.getBoolean("enable-console") && config.getBoolean("entity-create-portal-console")) {
                    getLogger().info(logMessage);
                } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-create-portal-console")) {
                    getLogger().info(logMessage);
                } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-create-portal-console")) {
                    getLogger().info(logMessage);
                } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                    getLogger().info(logMessage);
                }
            }
        } else {
            getLogger().warning("Running non-paper server -> can not pass Entity Create Portal Event");
        }

    }

    @EventHandler
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-damage-by-block")) {
            String name = event.getEntity().getName();
            String type = Objects.requireNonNull(event.getDamager()).getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("DAMAGEBYBLOCK: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_damage_by_block.log", logMessage);

            String eventName = "ENTITY_DAMAGE_BY_BLOCK";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Damage By Block event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-damage-by-block-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-damage-by-block-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-damage-by-block-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-damage-by-entity")) {
            String name = event.getEntity().getName();
            String type = event.getDamager().getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("DAMAGEBYENTITY: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_damage_by_entity.log", logMessage);

            String eventName = "ENTITY_DAMAGE_BY_ENTITY";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Damage By Entity event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-damage-by-entity-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-damage-by-entity-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-damage-by-entity-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-death")) {
            String name = event.getEntity().getName();
            String type = event.getEntity().getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("DEATH: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_death.log", logMessage);

            String eventName = "ENTITY_DEATH";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Death event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-death-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-death-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-death-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-explode")) {
            String name = event.getEntity().getName();
            String type = String.valueOf(event.getYield());
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("EXPLODE: Name: %s; Yield: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_explode.log", logMessage);

            String eventName = "ENTITY_EXPLODE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Explode event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-explode-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-explode-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-explode-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-interact")) {
            String name = event.getEntity().getName();
            String type = event.getBlock().getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("INTERACT: Name: %s; Interacted with: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_interact.log", logMessage);

            String eventName = "ENTITY_INTERACT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Interact event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-interact-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-interact-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-interact-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-portal-enter")) {
            String name = event.getEntity().getName();
            String type = event.getEntity().getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("PORTALENTER: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_portal_enter.log", logMessage);

            String eventName = "ENTITY_PORTAL_ENTER";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Portal Enter event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-enter-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-portal-enter-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-portal-enter-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityPortalExit(EntityPortalExitEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-portal-exit")) {
            String name = event.getEntity().getName();
            String type = event.getEntity().getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("PORTALEXIT: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_portal_exit.log", logMessage);

            String eventName = "ENTITY_PORTAL_EXIT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Portal Exit event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-portal-exit-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-protal-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity--console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-regain-health")) {
            String name = event.getEntity().getName();
            String type = event.getRegainReason().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("REGAINHEALTH: Name: %s; Reason: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_regain_health.log", logMessage);

            String eventName = "ENTITY_REGAIN_HEALTH";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Regain Health event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-regain-health-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-regain-health-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-regain-health-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-shoot-bow")) {
            String name = event.getEntity().getName();
            String type = event.getProjectile().getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("SHOOTBOW: Name: %s; Projectile: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_shoot_bow.log", logMessage);

            String eventName = "ENTITY_SHOOT_BOW";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Shoot Bow event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-shoot-bow-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-shoot-bow-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-shoot-bow-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-tame")) {
            String name = event.getEntity().getName();
            String type = event.getOwner().getName();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("TAME: Name: %s; Owner: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_tame.log", logMessage);

            String eventName = "ENTITY_TAME";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Tame event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-tame-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-tame-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-tame-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-target")) {
            String name = event.getEntity().getName();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("TARGET: Name: %s; Location: %s; World: %s.", name, location, world);
            logToFile("Entity Events", "entity_target.log", logMessage);

            String eventName = "ENTITY_TARGET";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Target event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-target-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-target-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-target-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-teleport")) {
            String name = event.getEntity().getName();
            String from = event.getFrom().toString();
            String to = Objects.requireNonNull(event.getTo()).toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("TELEPORT: Name: %s; From: %s; To %s; Location: %s; World: %s.", name, from, to, location, world);
            logToFile("Entity Events", "entity_teleport.log", logMessage);

            String eventName = "ENTITY_TELEPORT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Teleport event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-teleport-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-teleport-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-teleport-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEntityExpBottle(ExpBottleEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-exp-bottle")) {
            String name = event.getEntity().getName();
            String type = String.valueOf(event.getExperience());
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("EXPBOTTLE: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_exp_bottle.log", logMessage);

            String eventName = "ENTITY_EXP_BOTTLE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Exp Bottle event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-exp-bottle-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-exp-bottle-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-exp-bottle-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-explosion-prime")) {
            String name = event.getEntity().getName();
            String type = String.valueOf(event.getRadius());
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("EXPLOSIONPRIME: Name: %s; Radius: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_explosion_prime.log", logMessage);

            String eventName = "ENTITY_EXPLOSION_PRIME";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Explosion Prime event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-explosion-prime-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-explosion-prime-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-explosion-prime-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-food-level-change")) {
            String name = event.getEntity().getName();
            String level = String.valueOf(event.getFoodLevel());
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("FOODLEVELCHANGE: Name: %s; Level: %s; Location: %s; World: %s.", name, level, location, world);
            logToFile("Entity Events", "entity_food_level_change.log", logMessage);

            String eventName = "ENTITY_FOOD_LEVEL_CHANGE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Food Change Event event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-food-level-change-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-food-level-change-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-food-level-change-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-item-despawn")) {
            String name = event.getEntity().getName();
            String type = event.getEntity().getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("ITEMDESPAWN: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_item_despawn.log", logMessage);

            String eventName = "ENTITY_ITEM_DESPAWN";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Item Despawn event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-item-despawn-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-item-despawn-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-item-despawn-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onPigZap(PigZapEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-pig-zap")) {
            String name = event.getEntity().getName();
            String type = Objects.requireNonNull(event.getLightning().getCausingEntity()).getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("PIGZAP: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_pig_zap.log", logMessage);

            String eventName = "ENTITY_PIG_ZAP";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Pig Zap event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-pig-zap-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-pig-zap-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-pig-zap-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-player-death")) {
            String name = event.getEntity().getName();
            String clientBrandName = event.getEntity().getClientBrandName();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("PLAYERDEATH: Name: %s; ClientBrandName: %s; Location: %s; World: %s.", name, clientBrandName, location, world);
            logToFile("Entity Events", "entity_player_death.log", logMessage);

            String eventName = "ENTITY_PLAYER_DEATH";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Player Death event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-player-death-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-player-death-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-player-death-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-potion-splash")) {
            String name = event.getEntity().getName();
            String type = event.getPotion().getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("POTIONSPLASH: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_potion_splash.log", logMessage);

            String eventName = "ENTITY_POTION_SPLASH";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Potion Splash event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-potion-splash-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-potion-splash-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-potion-splash-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onSheepDyeWool(SheepDyeWoolEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-sheep-dye-wool")) {
            String name = event.getEntity().getName();
            String color = event.getColor().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("SHEEPDYEWOOL: Name: %s; Color: %s; Location: %s; World: %s.", name, color, location, world);
            logToFile("Entity Events", "entity_sheep_dye_wool.log", logMessage);

            String eventName = "ENTITY_SHEEP_DYE_WOOL";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Sheep Dye Wool event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-sheep-dye-wool-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-sheep-dye-wool-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-sheep-dye-wool-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onSheepRegrowWool(SheepRegrowWoolEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-sheep-regrow-wool")) {
            String name = event.getEntity().getName();
            String type = event.getEntity().getType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("SHEEPREGROWWOOL: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_sheep_regrow_wool.log", logMessage);

            String eventName = "ENTITY_SHEEP_REGROW_WOOL";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Sheep Regrow Wool event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-sheep-regrow-wool-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-sheep-regrow-wool-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-sheep-regrow-wool-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onSlimeSplit(SlimeSplitEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("entity-slime-split")) {
            String name = event.getEntity().getName();
            String count = String.valueOf(event.getCount());
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("SLIMESPLIT: Name: %s; Count: %s; Location: %s; World: %s.", name, count, location, world);
            logToFile("Entity Events", "entity_slime_split.log", logMessage);

            String eventName = "ENTITY_SLIME_SPLIT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Entity Slime Split event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("entity-slime-split-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("entity-slime-split-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("entity-slime-split-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-entity-console")) {
                getLogger().info(logMessage);
            }
        }
    }




    //Inventory
    @EventHandler
    public void onBrew(BrewEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("inventory-brew")) {
            String contents = event.getContents().toString();
            String fuellevel = String.valueOf(event.getFuelLevel());
            String location = Objects.requireNonNull(event.getContents().getLocation()).toString();
            String world = event.getContents().getLocation().getWorld().getName();
            String logMessage = String.format("BREW: Contents: %s; Fuellevel: %s; Location: %s; World: %s.", contents, fuellevel, location, world);
            logToFile("Inventory Events", "inventory_brew.log", logMessage);

            String eventName = "INVENTORY_BREW";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Inventory Brew event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("inventory-brew-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("inventory-brew-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("inventory-brew-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-inventory-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("inventory-craft-item")) {
            String name = event.getWhoClicked().getName();
            String result = String.valueOf(event.getCurrentItem());
            String location = event.getWhoClicked().getLocation().toString();
            String world = event.getWhoClicked().getLocation().getWorld().getName();
            String logMessage = String.format("CRAFTITEM: Name: %s; Result: %s; Location: %s; World: %s.", name, result, location, world);
            logToFile("Inventory Events", "inventory_craft_item.log", logMessage);

            String eventName = "INVENTORY_CRAFT_ITEM";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Inventory Craft Item event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("inventory-craft-item-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("inventory-craft-item-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("inventory-craft-item-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-inventory-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("inventory-furnace-burn")) {
            String burnTime = String.valueOf(event.getBurnTime());
            String fuel = event.getFuel().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getLocation().getWorld().getName();
            String logMessage = String.format("FURNACEBURN: Burntime: %s; Fuel: %s; Location: %s; World: %s.", burnTime, fuel, location, world);
            logToFile("Inventory Events", "inventory_furnace_burn.log", logMessage);

            String eventName = "INVENTORY_FURNACE_BURN";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Inventory Furnace Burn event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("inventory-furnace-burn-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("inventory-furnace-burn-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("inventory-furnace-burn-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-inventory-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("inventory-furnace-smelt")) {
            String block = event.getBlock().toString();
            String result = event.getResult().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getLocation().getWorld().getName();
            String logMessage = String.format("FURNACESMELT: Block: %s; Result: %s; Location: %s; World: %s.", block, result, location, world);
            logToFile("Inventory Events", "inventory_furnace_smelt.log", logMessage);

            String eventName = "INVENTORY_FURNACE_SMELT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Inventory Furnace Smelt event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("inventory-furnace-smelt-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("inventory-furnace-smelt-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("inventory-furnace-smelt-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-inventory-console")) {
                getLogger().info(logMessage);
            }
        }
    }




    //Player
    @EventHandler
    public void onAnimation(PlayerAnimationEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-animation")) {
            String name = event.getPlayer().getName();
            String type = event.getAnimationType().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("ANIMATION: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Player Events", "player_animation.log", logMessage);

            String eventName = "PLAYER_ANIMATION";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Animation event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-animation-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-animation-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-animation-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-bed-enter")) {
            String name = event.getPlayer().getName();
            String type = event.getBed().getType().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("BEDENTER: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Player Events", "player_bed_enter.log", logMessage);

            String eventName = "PLAYER_BED_ENTER";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Bed Enter event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-bed-enter-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-bed-enter-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-bed-enter-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-bed-leave")) {
            String name = event.getPlayer().getName();
            String type = event.getBed().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("BEDLEAVE: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Player Events", "player_bed_leave.log", logMessage);

            String eventName = "PLAYER_BED_LEAVE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Bed Leave event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-bed-leave-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-bed-leave-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-bed-leave-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-bucket-empty")) {
            String name = event.getPlayer().getName();
            String type = event.getBucket().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("BUCKETEMPTY: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Player Events", "player_bucket_empty.log", logMessage);

            String eventName = "PLAYER_BUCKET_EMPTY";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Bucket Empty event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-bucket-empty-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-bucket-empty-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-bucket-empty-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-bucket-fill")) {
            String name = event.getPlayer().getName();
            String type = event.getBucket().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("BUCKETFILL: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Player Events", "player_bucket_fill.log", logMessage);

            String eventName = "PLAYER_BUCKET_FILL";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Bucket Fill event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-bucket-fill-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-bucket-fill-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-bucket-fill-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (getServer().getVersion().contains("Paper")) {
            FileConfiguration config = getConfig();
            if (config.getBoolean("player-chat")) {
                String name = event.getPlayer().getName();
                String message = event.message().toString();
                String location = event.getPlayer().getLocation().toString();
                String world = event.getPlayer().getLocation().getWorld().getName();
                String logMessage = String.format("CHAT: Name: %s; Message: %s; Location: %s; World: %s.", name, message, location, world);
                logToFile("Player Events", "player_chat.log", logMessage);

                String eventName = "PLAYER_CHAT";
                if (webhookEvents.containsKey(eventName)) {
                    WebhookEvent webhookEvent = webhookEvents.get(eventName);
                    if (webhookEvent.isEnabled()) {
                        webhookEvent.sendWebhook("Player Chat event occurred!", String.valueOf(event));
                    }
                }

                if (config.getBoolean("enable-console") && config.getBoolean("player-chat-console")) {
                    getLogger().info(logMessage);
                } else if (config.getBoolean("enable-console") && !config.getBoolean("player-chat-console")) {
                    getLogger().info(logMessage);
                } else if (!config.getBoolean("enable-console") && config.getBoolean("player-chat-console")) {
                    getLogger().info(logMessage);
                } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                    getLogger().info(logMessage);
                }
            }
        } else {
            getLogger().warning("Running non-paper server -> can not pass Chat Event");
        }

    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-drop-item")) {
            String name = event.getPlayer().getName();
            String type = event.getItemDrop().getType().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("ITEMDROP: Item Drop: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Player Events", "player_drop_item.log", logMessage);

            String eventName = "PLAYER_DROP_ITEM";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Drop Item event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-drop-item-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-drop-item-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-drop-item-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-egg-throw")) {
            String name = event.getPlayer().getName();
            String type = event.getEgg().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("EGGTHROW: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Player Events", "player_egg_throw.log", logMessage);

            String eventName = "PLAYER_EGG_THROW";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Egg Throw event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-egg-throw-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-egg-throw-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-egg-throw-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-exp-change")) {
            String name = event.getPlayer().getName();
            String amount = String.valueOf(event.getAmount());
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("EXPCHANGE: Name: %s; Amount: %s; Location: %s; World: %s.", name, amount, location, world);
            logToFile("Player Events", "player_exp_change.log", logMessage);

            String eventName = "PLAYER_EXP_CHANGE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Exp Change event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-exp-change-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-exp-change-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-exp-change-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-fish")) {
            String name = event.getPlayer().getName();
            String caught = Objects.requireNonNull(event.getCaught()).getType().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("FISH: Name: %s; Caught: %s; Location: %s; World: %s.", name, caught, location, world);
            logToFile("Player Events", "player_fish.log", logMessage);

            String eventName = "PLAYER_FISH";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Fish event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-fish-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-fish-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-fish-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-gamemode-change")) {
            String name = event.getPlayer().getName();
            String newgamemode = event.getNewGameMode().name();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("GAMEMODECHANGE: Name: %s; New Gamemode: %s; Location: %s; World: %s.", name, newgamemode, location, world);
            logToFile("Player Events", "player_gamemode_change.log", logMessage);

            String eventName = "PLAYER_GAMEMODE_CHANGE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Gamemode Change event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-gamemode-change-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-gamemode-change-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-gamemode-change-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-interact-entity")) {
            String name = event.getPlayer().getName();
            String type = event.getRightClicked().getType().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("INTERACTENTITY: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Player Events", "player_interact_entity.log", logMessage);

            String eventName = "PLAYER_INTERACT_ENTITY";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Interact Entity event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-interact-entity-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-interact-entity-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-interact-entity-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-item-break")) {
            String name = event.getPlayer().getName();
            String type = event.getBrokenItem().getType().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("BROKENITEM: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Player Events", "player_item_break.log", logMessage);

            String eventName = "PLAYER_ITEM_BREAK";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Item Break event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-item-break-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-item-break-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-item-break-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-item-held")) {
            String name = event.getPlayer().getName();
            String oldslot = String.valueOf(event.getPreviousSlot());
            String newslot = String.valueOf(event.getNewSlot());
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("ITEMHELD: Name: %s; Old Slot: %s; New Slot: %s; Location: %s; World: %s.", name, oldslot, newslot, location, world);
            logToFile("Player Events", "player_item_held.log", logMessage);

            String eventName = "PLAYER_ITEM_HELD";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Item Held event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-item-held-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-item-held-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-item-held-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-join")) {
            String name = event.getPlayer().getName();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("JOIN: Name: %s; Location: %s; World: %s.", name, location, world);
            logToFile("Player Events", "player_join.log", logMessage);

            String eventName = "PLAYER_JOIN";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Join event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-join-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-join-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-join-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-kick")) {
            String name = event.getPlayer().getName();
            String cause = event.getCause().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("KICK: Name: %s; Cause: %s; Location: %s; World: %s.", name, cause, location, world);
            logToFile("Player Events", "player_kick.log", logMessage);

            String eventName = "PLAYER_KICK";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Kick event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-kick-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-kick-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-kick-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-level-change")) {
            String name = event.getPlayer().getName();
            String oldLevel = String.valueOf(event.getOldLevel());
            String newLevel = String.valueOf(event.getNewLevel());
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("LEVELCHANGE: Name: %s; Old Level: %s; New Level: %s; Location: %s; World: %s.", name, oldLevel, newLevel, location, world);
            logToFile("Player Events", "player_level_change.log", logMessage);

            String eventName = "PLAYER_LEVEL_CHANGE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Level Change event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-level-change-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-level-change-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-level-change-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-login")) {
            String name = event.getPlayer().getName();
            String address = Arrays.toString(event.getRealAddress().getAddress());
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("LOGIN: Name: %s; Type: %s; Address: %s; World: %s.", name, address, location, world);
            logToFile("Player Events", "player_login.log", logMessage);

            String eventName = "PLAYER_LOGIN";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Login event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-login-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-login-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-login-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-move")) {
            String name = event.getPlayer().getName();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("MOVE: Name: %s; Location: %s; World: %s.", name, location, world);
            logToFile("Player Events", "player_move.log", logMessage);

            String eventName = "PLAYER_MOVE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Move event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-move-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-move-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-move-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onPickItem(PlayerPickItemEvent event) {
        if (getServer().getVersion().contains("Paper")) {
            FileConfiguration config = getConfig();
            if (config.getBoolean("player-pick-item")) {
                String name = event.getPlayer().getName();
                String slot = String.valueOf(event.getTargetSlot());
                String location = event.getPlayer().getLocation().toString();
                String world = event.getPlayer().getLocation().getWorld().getName();
                String logMessage = String.format("PICKITEM: Name: %s; Slot: %s; Location: %s; World: %s.", name, slot, location, world);
                logToFile("Player Events", "player_pick_item.log", logMessage);

                String eventName = "PLAYER_";
                if (webhookEvents.containsKey(eventName)) {
                    WebhookEvent webhookEvent = webhookEvents.get(eventName);
                    if (webhookEvent.isEnabled()) {
                        webhookEvent.sendWebhook("Player  event occurred!", String.valueOf(event));
                    }
                }

                if (config.getBoolean("enable-console") && config.getBoolean("player-pick-item-console")) {
                    getLogger().info(logMessage);
                } else if (config.getBoolean("enable-console") && !config.getBoolean("player-pick-item-console")) {
                    getLogger().info(logMessage);
                } else if (!config.getBoolean("enable-console") && config.getBoolean("player-pick-item-console")) {
                    getLogger().info(logMessage);
                } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                    getLogger().info(logMessage);
                }
            }
        } else {
            getLogger().warning("Running non-paper server -> can not pass Player Pick Item Event");
        }
    }


    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-pre-login")) {
            String name = event.getPlayerProfile().getName();
            String UUID = event.getUniqueId().toString();
            String logMessage = String.format("PRELOGIN: Name: %s; UUID: %s.", name, UUID);
            logToFile("Player Events", "player_pre_login.log", logMessage);

            String eventName = "PLAYER_PRE_LOGIN";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Pre Login event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-pre-login-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-pre-login-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-pre-login-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-quit")) {
            String name = event.getPlayer().getName();
            String reason = event.getReason().name();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("QUIT: Name: %s; Reason: %s; Location: %s; World: %s.", name, reason, location, world);
            logToFile("Player Events", "player_quit.log", logMessage);

            String eventName = "PLAYER_QUIT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Quit event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-quit-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-quit-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-quit-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-respawn")) {
            String name = event.getPlayer().getName();
            String reason = event.getRespawnReason().name();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("RESPAWN: Name: %s; Reason: %s; Location: %s; World: %s.", name, reason, location, world);
            logToFile("Player Events", "player_respawn.log", logMessage);

            String eventName = "PLAYER_RESPAWN";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Respawn event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-respawn-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-respawn-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-respawn-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onShearEntity(PlayerShearEntityEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-shear-entity")) {
            String name = event.getPlayer().getName();
            String type = event.getEntity().getType().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("SHEARENTITY: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Player Events", "player_.log", logMessage);

            String eventName = "PLAYER_SHEAR_ENTITY";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Shear Entity event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-shear-entity-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-shear-entity-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-shear-entity-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-teleport")) {
            String name = event.getPlayer().getName();
            String from = event.getFrom().getWorld().getName();
            String to = event.getTo().getWorld().getName();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("TELEPORT: Name: %s; From: %s; To: %s; Location: %s; World: %s.", name, from, to, location, world);
            logToFile("Player Events", "player_teleport.log", logMessage);

            String eventName = "PLAYER_TELEPORT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Teleport event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-teleport-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-teleport-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-teleport-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-toogle-flight")) {
            String name = event.getPlayer().getName();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("TOGGLEFLIGHT: Name: %s; Location: %s; World: %s.", name, location, world);
            logToFile("Player Events", "player_toggle_flight.log", logMessage);

            String eventName = "PLAYER_TOGGLE_FLIGHT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Toggle Flight event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-toggle-flight-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-toggle-flight-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-toggle-flight-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-toogle-sneak")) {
            String name = event.getPlayer().getName();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("TOGGLESNEAK: Name: %s; Location: %s; World: %s.", name, location, world);
            logToFile("Player Events", "player_toggle_sneak.log", logMessage);

            String eventName = "PLAYER_TOGGLE_SNEAK";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Toggle Sneak event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-toggle-sneak-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-toggle-sneak-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-toggle-sneak-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-toogle-sprint")) {
            String name = event.getPlayer().getName();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("TOGGLESPRINT: Name: %s; Location: %s; World: %s.", name, location, world);
            logToFile("Player Events", "player_toggle_sprint.log", logMessage);

            String eventName = "PLAYER_TOGGLE_SPRINT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Player Toggle Sprint event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("player-toggle-sprint-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("player-toggle-sprint-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("player-toggle-sprint-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-player-console")) {
                getLogger().info(logMessage);
            }
        }
    }




    //Vehicle
    @EventHandler
    public void onBlockCollision(VehicleBlockCollisionEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("vehicle-block-collision")) {
            String block = event.getBlock().getType().toString();
            String velocity = event.getVelocity().toString();
            String location = event.getBlock().getLocation().toString();
            String world = event.getBlock().getLocation().getWorld().getName();
            String logMessage = String.format("BLOCKCOLLISION: Hit Block: %s; Velocity: %s; Location: %s; World: %s.", block, velocity, location, world);
            logToFile("Vehicle Events", "vehicle_block_collision.log", logMessage);

            String eventName = "VEHICLE_BLOCK_COLLISION";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Vehicle Block Collision event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("vehicle-block-collision-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("vehicle-block-collision-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("vehicle-block-collision-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-vehicle-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onCreate(VehicleCreateEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("vehicle-create")) {
            String name = event.getVehicle().getName();
            String type = event.getVehicle().getType().toString();
            String location = event.getVehicle().getLocation().toString();
            String world = event.getVehicle().getLocation().getWorld().getName();
            String logMessage = String.format("CREATE: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Vehicle Events", "vehicle_create.log", logMessage);

            String eventName = "VEHICLE_CREATE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Vehicle Create event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("vehicle-create-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("vehicle-create-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("vehicle-create-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-vehicle-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onDamage(VehicleDamageEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("vehicle-damage")) {
            String name = event.getVehicle().getName();
            String attacker = Objects.requireNonNull(event.getAttacker()).getName();
            String location = event.getVehicle().getLocation().toString();
            String world = event.getVehicle().getLocation().getWorld().getName();
            String logMessage = String.format("DAMAGE: Name: %s; Attacker: %s; Location: %s; World: %s.", name, attacker, location, world);
            logToFile("Vehicle Events", "vehicle_damage.log", logMessage);

            String eventName = "VEHICLE_DAMAGE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Vehicle Damage event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("vehicle-damage-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("vehicle-damage-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("vehicle-damage-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-vehicle-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onDestroy(VehicleDestroyEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("vehicle-destroy")) {
            String name = event.getVehicle().getName();
            String attacker = Objects.requireNonNull(event.getAttacker()).getName();
            String location = event.getVehicle().getLocation().toString();
            String world = event.getVehicle().getLocation().getWorld().getName();
            String logMessage = String.format("DESTROY: Name: %s; Attacker: %s; Location: %s; World: %s.", name, attacker, location, world);
            logToFile("Vehicle Events", "vehicle_destroy.log", logMessage);

            String eventName = "VEHICLE_DESTROY";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Vehicle Destroy event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("vehicle-destroy-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("vehicle-destroy-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("vehicle-destroy-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-vehicle-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onEnter(VehicleEnterEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("vehicle-enter")) {
            String name = event.getVehicle().getName();
            String entered = event.getEntered().getName();
            String location = event.getVehicle().getLocation().toString();
            String world = event.getVehicle().getLocation().getWorld().getName();
            String logMessage = String.format("ENTER: Name: %s; Entered: %s; Location: %s; World: %s.", name, entered, location, world);
            logToFile("Vehicle Events", "vehicle_enter.log", logMessage);

            String eventName = "VEHICLE_ENTER";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Vehicle Enter event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("vehicle-enter-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("vehicle-enter-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("vehicle-enter-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-enter-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onExit(VehicleExitEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("vehicle-exit")) {
            String name = event.getVehicle().getName();
            String exited = event.getExited().getName();
            String location = event.getVehicle().getLocation().toString();
            String world = event.getVehicle().getLocation().getWorld().getName();
            String logMessage = String.format("EXIT: Name: %s; Exited: %s; Location: %s; World: %s.", name, exited, location, world);
            logToFile("Vehicle Events", "vehicle_exited.log", logMessage);

            String eventName = "VEHICLE_EXIT";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Vehicle Exit event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("vehicle-exited-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("vehicle-exited-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("vehicle-exited-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-vehicle-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onMove(VehicleMoveEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("vehicle-move")) {
            String name = event.getVehicle().getName();
            String from = String.valueOf(event.getFrom());
            String to = String.valueOf(event.getTo());
            String location = event.getVehicle().getLocation().toString();
            String world = event.getVehicle().getLocation().getWorld().getName();
            String logMessage = String.format("MOVE: Name: %s; From: %s; To: %s; Location: %s; World: %s.", name, from, to, location, world);
            logToFile("Vehicle Events", "vehicle_move.log", logMessage);

            String eventName = "VEHICLE_MOVE";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Vehicle Move event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("vehicle-move-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("vehicle-move-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("vehicle-move-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-vehicle-console")) {
                getLogger().info(logMessage);
            }
        }
    }



    //weather
    @EventHandler
    public void onLightning(LightningStrikeEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("weather-lightning")) {
            String name = event.getLightning().getName();
            String cause = event.getCause().name();
            String location = event.getLightning().getLocation().toString();
            String world = event.getLightning().getLocation().getWorld().getName();
            String logMessage = String.format("LIGHTNING: Name: %s; Cause: %s; Location: %s; World: %s.", name, cause, location, world);
            logToFile("Weather Events", "weather-lightning.log", logMessage);

            String eventName = "WEATHER_LIGHTNING";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Weather Lightning event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("weather-lightning-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("weather-lightning-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("weather-lightning-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-weather-console")) {
                getLogger().info(logMessage);
            }
        }
    }

    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("weather-thunder")) {
            String state = String.valueOf(event.toThunderState());
            String cause = event.getCause().name();
            String logMessage = String.format("THUNDERCHANGE: Active Thunder? %s; Cause: %s.", state, cause);
            logToFile("Weather Events", "weather-thunder.log", logMessage);

            String eventName = "WEATHER_THUNDER";
            if (webhookEvents.containsKey(eventName)) {
                WebhookEvent webhookEvent = webhookEvents.get(eventName);
                if (webhookEvent.isEnabled()) {
                    webhookEvent.sendWebhook("Weather Thunder event occurred!", String.valueOf(event));
                }
            }

            if (config.getBoolean("enable-console") && config.getBoolean("weather-thunder-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") && !config.getBoolean("weather-thunder-console")) {
                getLogger().info(logMessage);
            } else if (!config.getBoolean("enable-console") && config.getBoolean("weather-thunder-console")) {
                getLogger().info(logMessage);
            } else if (config.getBoolean("enable-console") || config.getBoolean("enable-weather-console")) {
                getLogger().info(logMessage);
            }
        }
    }
}
