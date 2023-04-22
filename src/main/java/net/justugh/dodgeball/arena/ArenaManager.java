package net.justugh.dodgeball.arena;

import net.justugh.dodgeball.Dodgeball;
import net.justugh.dodgeball.arena.data.Arena;
import net.justugh.dodgeball.util.FileUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class ArenaManager {

    private final int X_INCREMENT_AMOUNT = 10000;

    private final Dodgeball plugin;
    private final List<Arena> loadedArenas = new ArrayList<>();
    private int currentX;

    private World arenasWorld;

    public ArenaManager(Dodgeball plugin) {
        this.plugin = plugin;

        loadArenaWorld();
        loadArenas();
    }

    private void loadArenaWorld() {
        File arenaWorldFile = new File("arenas");

        if (arenaWorldFile.exists()) {
            Bukkit.unloadWorld("arenas", false);
            FileUtils.deleteDirectory(arenaWorldFile);
        }

        generateNewArenaWorld();
    }

    private void generateNewArenaWorld() {
        WorldCreator creator = new WorldCreator("arenas")
                .type(WorldType.FLAT)
                .environment(World.Environment.NORMAL)
                .generateStructures(false)
                .generatorSettings("{ \"layers\": [], \"biome\":\"plains\"}");

        arenasWorld = creator.createWorld();

        if (arenasWorld == null) {
            plugin.getLogger().log(Level.SEVERE, "Unable to load arena world!");
            return;
        }

        arenasWorld.setTime(6000);
        arenasWorld.setStorm(false);
        arenasWorld.setPVP(true);
        arenasWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        arenasWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        arenasWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        arenasWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
    }

    public void loadArenas() {
        FileConfiguration config = plugin.getArenaConfig();
        loadedArenas.clear();

        ConfigurationSection arenasSection = config.getConfigurationSection("Arenas");

        if (arenasSection == null) {
            plugin.getLogger().log(Level.WARNING, "Unable to load arenas, arenas section is missing.");
            return;
        }

        for (String id : arenasSection.getKeys(false)) {
            String name = arenasSection.getString(id + ".Name");
            String author = arenasSection.getString(id + ".Author");
            File arenaSchematic = new File(plugin.getDataFolder() + "/arenas/" + id + ".schem");

            if (!arenaSchematic.exists()) {
                plugin.getLogger().log(Level.WARNING, "Unable to load arena '{0}', schematic file missing.", id);
                continue;
            }

            loadedArenas.add(new Arena(name, author, arenaSchematic));
        }
    }

    public Arena getRandomArena() {
        return loadedArenas.isEmpty() ? null : loadedArenas.get(ThreadLocalRandom.current().nextInt(loadedArenas.size()));
    }

    public Location getNextViableArenaLocation() {
        if (arenasWorld == null) {
            return null;
        }

        return new Location(arenasWorld, currentX += X_INCREMENT_AMOUNT, 60, 0);
    }

}
