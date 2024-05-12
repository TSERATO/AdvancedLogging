package dev.tserato.advancedlogging;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdvancedLoggingListener extends JavaPlugin implements Listener {

    FileConfiguration config = getConfig();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        String BlockBreakPlayerName = event.getPlayer().getName();
        String BlockBreakBlockType = event.getBlock().getType().toString();
        String BlockBreakLocation = event.getBlock().getLocation().toString();
        String BlockBreakWorld = event.getBlock().getWorld().getName();
        String BlockBreakTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        BlockBreakLTTF(BlockBreakPlayerName, BlockBreakBlockType, BlockBreakLocation, BlockBreakWorld, BlockBreakTimestamp);

    }

    private void BlockBreakLTTF(String BlockBreakPlayerName, String BlockBreakBlockType, String BlockBreakLocation, String BlockBreakWorld, String BlockBreakTimestamp) {
        if (config.getBoolean("block-break") == true) {

            File logFile = new File("block_break.log");

            try {
                if (!logFile.exists()) {
                    logFile.createNewFile();

                }

                FileWriter writer = new FileWriter(logFile, true);
                writer.write("[" + BlockBreakTimestamp + "]" + "BLOCKBREAK: " + "Broken by: " + BlockBreakPlayerName + "; Type: " + BlockBreakBlockType + "; Location: " + BlockBreakLocation + "; World: " + BlockBreakWorld + ".");
                writer.close();

            } catch (IOException e) {
                getLogger().severe("An error occurred while logging block breaking: " + e.getMessage());
            }
        }

    }


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {

        String BlockPlacePlayerName = event.getPlayer().getName();
        String BlockPlaceBlockType = event.getBlock().getType().toString();
        String BlockPlaceLocation = event.getBlock().getLocation().toString();
        String BlockPlaceWorld = event.getBlock().getWorld().getName();
        String BlockPlaceTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        BlockPlaceLTTF(BlockPlacePlayerName, BlockPlaceBlockType, BlockPlaceLocation, BlockPlaceWorld, BlockPlaceTimestamp);

    }

    private void BlockPlaceLTTF(String BlockPlacePlayerName, String BlockPlaceBlockType, String BlockPlaceLocation, String BlockPlaceWorld, String BlockPlaceTimestamp) {
        if (config.getBoolean("block-place") == true) {

            File logFile = new File("block_place.log");

            try {
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }

                FileWriter writer = new FileWriter(logFile, true);
                writer.write("[" + BlockPlaceTimestamp + "]" + "BLOCKPLACE: " + "Placed by: " + BlockPlacePlayerName + "; Type: " + BlockPlaceBlockType + "; Location: " + BlockPlaceLocation + "; World: " + BlockPlaceWorld + ".");
                writer.close();

            } catch (IOException e) {
                getLogger().severe("An error occurred while logging block placement: " + e.getMessage());
            }
        }

    }
}
