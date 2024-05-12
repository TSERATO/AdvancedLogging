package dev.tserato.advancedlogging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.plugin.java.JavaPlugin;

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
        String[] eventDirectories = {"Block Events", "Enchantment Events", "Entity Events"};
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
    public void onEntityCreatePortalEvent(EntityPortalReadyEvent event) {
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










    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
