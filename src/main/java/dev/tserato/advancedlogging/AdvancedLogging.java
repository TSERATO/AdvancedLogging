package dev.tserato.advancedlogging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
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
import org.jetbrains.annotations.NotNull;

public class AdvancedLogging extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("AdvancedLogging Enabled");
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        createDirectoriesIfNeeded();
    }

    @Override
    public void onDisable() {
        getLogger().info("AdvancedLogging Disabled");
    }

    private void createDirectoriesIfNeeded() {
        File logsDirectory = new File(getDataFolder(), "Logs");
        if (!logsDirectory.exists()) {
            logsDirectory.mkdirs();
        }
        // Create subdirectories for different types of events
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
            logToFile("Entity Events", "entity_spawn.log", logMessage);
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
        if (config.getBoolean("entity-create-portal")) {
            String name = event.getEntity().getName();
            String type = event.getPortalType().toString();
            String location = event.getEntity().getLocation().toString();
            String world = event.getEntity().getLocation().getWorld().getName();
            String logMessage = String.format("CREATEPORTAL: Name: %s; Type: %s; Location: %s; World: %s.", name, type, location, world);
            logToFile("Entity Events", "entity_create_portal.log", logMessage);
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
            logToFile("Entity Events", "entity_.log", logMessage);
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
            String logMessage = String.format("FURNACEBURN: Block: %s; Result: %s; Location: %s; World: %s.", block, result, location, world);
            logToFile("Inventory Events", "inventory_furnace_smelt.log", logMessage);
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
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-chat")) {
            String name = event.getPlayer().getName();
            String message = event.message().toString();
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("CHAT: Name: %s; Message: %s; Location: %s; World: %s.", name, message, location, world);
            logToFile("Player Events", "player_chat.log", logMessage);
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
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-pick-item")) {
            String name = event.getPlayer().getName();
            String slot = String.valueOf(event.getTargetSlot());
            String location = event.getPlayer().getLocation().toString();
            String world = event.getPlayer().getLocation().getWorld().getName();
            String logMessage = String.format("PICKITEM: Name: %s; Slot: %s; Location: %s; World: %s.", name, slot, location, world);
            logToFile("Player Events", "player_pick_item.log", logMessage);
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
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        FileConfiguration config = getConfig();
        if (config.getBoolean("player-pre-login")) {
            String name = event.getPlayerProfile().getName();
            String UUID = event.getUniqueId().toString();
            String logMessage = String.format("PRELOGIN: Name: %s; UUID: %s.", name, UUID);
            logToFile("Player Events", "player_pre_login.log", logMessage);
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










    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("advancedlogging") || label.equalsIgnoreCase("al")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                sender.sendMessage("Config reloaded.");
                return true;
            }
        }
        return false;
    }
}
